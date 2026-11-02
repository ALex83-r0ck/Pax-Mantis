package com.alexander.paxmentis.data

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alexander.paxmentis.security.DatabaseKeyProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import net.zetetic.database.sqlcipher.SQLiteDatabase
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.security.KeyStore
import java.security.SecureRandom

/**
 * Prüft die Umschlüsselung bestehender Datenbanken auf den Keystore-Schlüssel.
 *
 * Läuft mit eigenen Datei-, Prefs- und Keystore-Namen, damit echte Sorgenstuhl-Einträge
 * auf dem Testgerät nicht angefasst werden.
 */
@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val opened = mutableListOf<PaxMentisDatabase>()

    @Before
    fun setUp() {
        System.loadLibrary("sqlcipher")
        cleanUp()
    }

    @After
    fun tearDown() {
        opened.forEach { it.close() }
        val leftovers = dbDir().listFiles()!!.map { it.name }
            .filter { it.contains(".migration-probe") || it.contains(".pre-migration") }
        cleanUp()
        assertEquals("Hilfsdateien der Migration übrig", emptyList<String>(), leftovers)
    }

    @Test
    fun fixedLegacyKey_entriesSurvive() {
        createOldDatabase(DatabaseKeyProvider.LEGACY_FIXED_PASSPHRASE, listOf("alt eins", "alt zwei"))

        assertEquals(listOf("alt zwei", "alt eins"), readWorries(newProvider()))
        assertTrue(newProvider().isLegacyMigrationDone())
    }

    @Test
    fun interimEncryptedPrefsKey_entriesSurvive() {
        val key = randomKey()
        @Suppress("DEPRECATION")
        val interimPrefs = EncryptedSharedPreferences.create(
            context,
            INTERIM_PREFS,
            MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        assertTrue(interimPrefs.edit().putString(INTERIM_KEY, Base64.encodeToString(key, Base64.NO_WRAP)).commit())
        createOldDatabase(key, listOf("zwischenstand eins", "zwischenstand zwei"), interimSchema = true)

        assertEquals(listOf("zwischenstand zwei", "zwischenstand eins"), readWorries(newProvider()))
    }

    @Test
    fun interimSchema_entriesKeepIdsAndWorryBecomesNullable() = runBlocking {
        createOldDatabase(DatabaseKeyProvider.LEGACY_FIXED_PASSPHRASE, listOf("eins", "zwei"), interimSchema = true)
        val dao = open(newProvider()).worryEntryDao()
        val before = dao.getAll().first()

        dao.update(before.first().copy(worry = null))
        dao.insert(WorryEntry(date = 99, worry = "neu"))

        val after = dao.getAll().first()
        assertEquals(listOf(99L to "neu", 1L to null, 0L to "eins"), after.map { it.date to it.worry })
        assertEquals(before.map { it.id }.toSet(), after.filter { it.date != 99L }.map { it.id }.toSet())
    }

    @Test
    fun interimPlainPrefsFallbackKey_entriesSurvive() {
        val key = randomKey()
        assertTrue(
            context.getSharedPreferences(INTERIM_PREFS, Context.MODE_PRIVATE).edit()
                .putString(INTERIM_KEY, Base64.encodeToString(key, Base64.NO_WRAP)).commit()
        )
        createOldDatabase(key, listOf("fallback"))

        assertEquals(listOf("fallback"), readWorries(newProvider()))
    }

    @Test
    fun secondStartAfterMigration_entriesStillThere() {
        createOldDatabase(DatabaseKeyProvider.LEGACY_FIXED_PASSPHRASE, listOf("bleibt"))
        readWorries(newProvider())
        closeAll()

        assertEquals(listOf("bleibt"), readWorries(newProvider()))
    }

    @Test
    fun unknownKey_databaseIsSetAsideUnchangedIncludingWal() {
        createOldDatabase(randomKey(), listOf("unbekannt"))
        val before = dbFiles().associate { it.name.removePrefix(TEST_DB) to it.readBytes() }

        assertTrue(readWorries(newProvider()).isEmpty())

        val setAside = setAsideFiles(".unknown-key-")
            .associate { it.name.replace(Regex("""\.unknown-key-\d+"""), "").removePrefix(TEST_DB) to it.readBytes() }
        assertEquals(before.keys, setAside.keys)
        before.keys.forEach { assertArrayEquals(it, before[it], setAside[it]) }
    }

    @Test
    fun keyLostBeforeMigration_legacyDatabaseIsMigratedNotQuarantined() {
        createOldDatabase(DatabaseKeyProvider.LEGACY_FIXED_PASSPHRASE, listOf("noch alt"))
        // Schlüssel wurde schon erzeugt, Migration aber nie abgeschlossen; dann fehlt der Keystore-Eintrag.
        newProvider().getOrCreatePassphrase()
        deleteKeystoreKey()

        assertEquals(listOf("noch alt"), readWorries(newProvider()))
        assertTrue(quarantinedFiles().isEmpty())
    }

    @Test
    fun keyLostAfterMigration_fileAndKeyAreSetAsideNotDeleted() {
        createOldDatabase(DatabaseKeyProvider.LEGACY_FIXED_PASSPHRASE, listOf("weg?"))
        readWorries(newProvider())
        closeAll()
        val wrappedBefore = securePrefs().getString(DatabaseKeyProvider.KEY_WRAPPED_PASSPHRASE, null)
        deleteKeystoreKey()

        assertTrue(readWorries(newProvider()).isEmpty())
        assertTrue(quarantinedFiles().any { !it.name.endsWith("-wal") && !it.name.endsWith("-shm") })
        val setAside = securePrefs().all.filterKeys {
            it.startsWith(DatabaseKeyProvider.KEY_WRAPPED_PASSPHRASE + ".unreadable-")
        }
        assertEquals(listOf(wrappedBefore), setAside.values.toList())
    }

    // --- Hilfsfunktionen ---

    /**
     * Erzeugt eine Datenbank wie ein früherer App-Stand: Room-Version 1 im WAL-Modus, Daten noch im WAL
     * (Dateien werden kopiert, bevor beim Schließen checkpointet wird – wie nach einem App-Kill).
     *
     * [interimSchema]: Der lokale Zwischenstand hatte `worry` als NOT NULL, der committete Stand nullable.
     */
    private fun createOldDatabase(passphrase: ByteArray, worries: List<String>, interimSchema: Boolean = false) {
        val sourceFile = context.getDatabasePath(SOURCE_DB)
        sourceFile.parentFile!!.mkdirs()
        val source = SQLiteDatabase.openDatabase(
            sourceFile.path, passphrase.copyOf(), null,
            SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY, null
        )
        source.enableWriteAheadLogging()
        val worryColumn = if (interimSchema) "`worry` TEXT NOT NULL" else "`worry` TEXT"
        source.execSQL(
            "CREATE TABLE IF NOT EXISTS `worry_entries` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`date` INTEGER NOT NULL, $worryColumn, `reason` TEXT, `worstCase` TEXT, `solution` TEXT)"
        )
        source.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        source.execSQL(
            "INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, ?)",
            arrayOf<Any?>(if (interimSchema) INTERIM_IDENTITY_HASH else V1_IDENTITY_HASH)
        )
        worries.forEachIndexed { i, worry ->
            source.execSQL("INSERT INTO worry_entries (date, worry) VALUES (?, ?)", arrayOf<Any?>(i.toLong(), worry))
        }
        source.version = 1

        assertTrue("Testaufbau: Daten sollen im WAL stehen", File(sourceFile.path + "-wal").length() > 0)
        listOf("", "-wal", "-shm").forEach { ext ->
            File(sourceFile.path + ext).copyTo(File(context.getDatabasePath(TEST_DB).path + ext), overwrite = true)
        }
        source.close()
        context.deleteDatabase(SOURCE_DB)
    }

    private fun newProvider() = DatabaseKeyProvider(context, TEST_PREFS, TEST_KEY_ALIAS, INTERIM_PREFS)

    private fun open(provider: DatabaseKeyProvider) =
        PaxMentisDatabase.build(context, provider, TEST_DB).also { opened += it }

    private fun readWorries(provider: DatabaseKeyProvider): List<String?> = runBlocking {
        open(provider).worryEntryDao().getAll().first().map { it.worry }
    }

    private fun closeAll() {
        opened.forEach { it.close() }
        opened.clear()
    }

    private fun randomKey() = ByteArray(32).also { SecureRandom().nextBytes(it) }

    private fun securePrefs() = context.getSharedPreferences(TEST_PREFS, Context.MODE_PRIVATE)

    private fun dbDir() = context.getDatabasePath(TEST_DB).parentFile!!

    private fun dbFiles() = dbDir().listFiles()!!
        .filter { it.name == TEST_DB || it.name.startsWith("$TEST_DB-") }

    private fun setAsideFiles(marker: String) = dbDir().listFiles()!!
        .filter { it.name.startsWith("$TEST_DB$marker") }

    private fun quarantinedFiles() = setAsideFiles(".unreadable-")

    private fun deleteKeystoreKey() {
        KeyStore.getInstance("AndroidKeyStore").apply { load(null) }.deleteEntry(TEST_KEY_ALIAS)
    }

    private fun cleanUp() {
        context.getDatabasePath(TEST_DB).parentFile?.listFiles()
            ?.filter { it.name.startsWith(TEST_DB) || it.name.startsWith(SOURCE_DB) }
            ?.forEach { it.delete() }
        listOf(TEST_PREFS, INTERIM_PREFS).forEach { name ->
            context.getSharedPreferences(name, Context.MODE_PRIVATE).edit().clear().commit()
            context.deleteSharedPreferences(name)
        }
        deleteKeystoreKey()
    }

    private companion object {
        const val TEST_DB = "migration_test_db"
        const val SOURCE_DB = "migration_test_source_db"
        const val TEST_PREFS = "migration_test_secure"
        const val TEST_KEY_ALIAS = "migration_test_db_key"
        const val INTERIM_PREFS = "migration_test_interim"
        const val INTERIM_KEY = DatabaseKeyProvider.INTERIM_KEY_PASSPHRASE

        /** Identity-Hashes von Version 1, wie Room sie geschrieben hat (Zwischenstand: aus dem Absturz-Log). */
        const val V1_IDENTITY_HASH = "dee82715a75381377b0fbd4e7bcfbf79"
        const val INTERIM_IDENTITY_HASH = "b7e0320e8fbab4111d49944ddd38f191"
    }
}

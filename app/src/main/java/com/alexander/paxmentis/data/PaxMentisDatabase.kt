package com.alexander.paxmentis.data

import android.content.Context
import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.alexander.paxmentis.security.DatabaseKeyProvider
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import java.io.File

@Database(entities = [WorryEntry::class, CrisisPlanItem::class], version = 3)
abstract class PaxMentisDatabase : RoomDatabase() {
    abstract fun worryEntryDao(): WorryEntryDao
    abstract fun crisisPlanDao(): CrisisPlanDao

    companion object {
        /**
         * Version 1 gab es mit zwei Schemata: `worry` nullable (committeter Stand) und `worry NOT NULL`
         * (lokaler Zwischenstand). Beide werden auf das aktuelle Schema gebracht, alle Einträge samt ID bleiben.
         */
        internal val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE `worry_entries_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`date` INTEGER NOT NULL, `worry` TEXT, `reason` TEXT, `worstCase` TEXT, `solution` TEXT)"
                )
                db.execSQL(
                    "INSERT INTO `worry_entries_new` (`id`, `date`, `worry`, `reason`, `worstCase`, `solution`) " +
                        "SELECT `id`, `date`, `worry`, `reason`, `worstCase`, `solution` FROM `worry_entries`"
                )
                db.execSQL("DROP TABLE `worry_entries`")
                db.execSQL("ALTER TABLE `worry_entries_new` RENAME TO `worry_entries`")
            }
        }

        /** Version 3: Krisenplan. */
        internal val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `crisis_plan_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`section` TEXT NOT NULL, `text` TEXT NOT NULL, `phone` TEXT, `position` INTEGER NOT NULL)"
                )
            }
        }

        private const val DB_NAME = "paxmentis_db"
        private const val TAG = "PaxMentisDatabase"
        private const val PROBE_SUFFIX = ".migration-probe"
        private const val PRE_MIGRATION_SUFFIX = ".pre-migration"
        private val DB_FILE_EXTENSIONS = listOf("", "-wal", "-shm", "-journal")

        @Volatile
        private var INSTANCE: PaxMentisDatabase? = null

        fun getInstance(context: Context, keyProvider: DatabaseKeyProvider): PaxMentisDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: build(context.applicationContext, keyProvider, DB_NAME).also { INSTANCE = it }
            }

        @VisibleForTesting
        internal fun build(context: Context, keyProvider: DatabaseKeyProvider, dbName: String): PaxMentisDatabase {
            val file = context.getDatabasePath(dbName)
            val passphrase = try {
                keyProvider.getOrCreatePassphrase()
            } catch (e: DatabaseKeyProvider.KeyLostException) {
                // Ohne Schlüssel ist eine bereits umgeschlüsselte Datenbank nicht mehr lesbar. Statt bei
                // jedem Öffnen abzustürzen: Datei UND alten Schlüssel beiseitelegen (nichts löschen).
                Log.e(TAG, "Datenbank-Schlüssel verloren", e)
                val suffix = ".unreadable-${System.currentTimeMillis()}"
                keyProvider.setAsidePassphrase(suffix)
                // Noch nicht umgeschlüsselt und mit einem alten Schlüssel lesbar? Dann bleibt sie liegen
                // und wird unten ganz normal migriert.
                val stillLegacy = !keyProvider.isLegacyMigrationDone() &&
                    keyProvider.legacyPassphrases().any { opensWith(file, it) }
                if (!stillLegacy) quarantineDatabase(file, suffix)
                keyProvider.getOrCreatePassphrase()
            }
            migrateFromLegacyKey(file, passphrase, keyProvider)

            return Room.databaseBuilder(context, PaxMentisDatabase::class.java, dbName)
                .openHelperFactory(SupportOpenHelperFactory(passphrase))
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
        }

        /**
         * Bestehende Installationen haben eine mit einem früheren Schlüssel verschlüsselte Datenbank
         * (siehe [DatabaseKeyProvider.legacyPassphrases]). Diese wird einmalig auf die neue
         * Passphrase umgeschlüsselt, ohne Datenverlust.
         *
         * Die Originaldateien werden dabei nie mit einem Schlüsselkandidaten geöffnet: Ein Öffnen mit
         * falschem Schlüssel kann das WAL entfernen – und dort stehen oft noch die Einträge. Jeder
         * Versuch läuft auf einer Kopie; erst die erfolgreich umgeschlüsselte Kopie ersetzt das Original.
         *
         * Bei einem Absturz mittendrin wird die Migration beim nächsten Start erneut versucht.
         * Passt kein bekannter Schlüssel, wird die Datenbank beiseitegelegt (nicht gelöscht).
         */
        private fun migrateFromLegacyKey(
            file: File,
            newPassphrase: ByteArray,
            keyProvider: DatabaseKeyProvider
        ) {
            if (keyProvider.isLegacyMigrationDone()) return

            val original = File(file.path + PRE_MIGRATION_SUFFIX)
            // Absturz zwischen den beiden Umbenennungen beim Austausch: Original zurückholen.
            if (!file.exists() && original.exists()) moveDatabase(original, file)

            if (!file.exists()) {
                keyProvider.markLegacyMigrationDone()
                return
            }

            // Absturz nach dem Austausch, aber vor dem Markieren: schon umgeschlüsselt.
            val migrated = opensWith(file, newPassphrase) ||
                keyProvider.legacyPassphrases().any { old -> rekeyViaCopy(file, old, newPassphrase) }

            if (!migrated) {
                Log.e(TAG, "Datenbank mit keinem bekannten Schlüssel lesbar, wird beiseitegelegt")
                quarantineDatabase(file, ".unknown-key-${System.currentTimeMillis()}")
            }
            keyProvider.markLegacyMigrationDone()
            deleteDatabaseFiles(original)
        }

        /** Schlüsselt eine Kopie um und tauscht sie erst dann gegen das Original. */
        private fun rekeyViaCopy(file: File, oldPassphrase: ByteArray, newPassphrase: ByteArray): Boolean {
            val probe = copyToProbe(file)
            try {
                openAndCheck(probe, oldPassphrase).use { db ->
                    // Rekey ist im WAL-Modus problematisch; das schreibt das WAL in die Kopie zurück.
                    // Room schaltet WAL beim nächsten Öffnen wieder ein.
                    db.rawQuery("PRAGMA journal_mode = DELETE").use { it.moveToFirst() }
                    db.changePassword(newPassphrase)
                }
                openAndCheck(probe, newPassphrase).close()
            } catch (e: SQLiteException) {
                deleteDatabaseFiles(probe)
                return false
            }
            val original = File(file.path + PRE_MIGRATION_SUFFIX)
            moveDatabase(file, original)
            moveDatabase(probe, file)
            return true
        }

        /** Prüft den Schlüssel an einer Kopie, damit das Original unangetastet bleibt. */
        private fun opensWith(file: File, passphrase: ByteArray): Boolean {
            if (!file.exists()) return false
            val probe = copyToProbe(file)
            return try {
                openAndCheck(probe, passphrase).close()
                true
            } catch (e: SQLiteException) {
                false
            } finally {
                deleteDatabaseFiles(probe)
            }
        }

        private fun copyToProbe(file: File): File {
            val probe = File(file.path + PROBE_SUFFIX)
            deleteDatabaseFiles(probe)
            DB_FILE_EXTENSIONS.forEach { ext ->
                val source = File(file.path + ext)
                if (source.exists()) source.copyTo(File(probe.path + ext))
            }
            return probe
        }

        private fun moveDatabase(from: File, to: File) {
            DB_FILE_EXTENSIONS.forEach { ext ->
                val source = File(from.path + ext)
                if (source.exists()) check(source.renameTo(File(to.path + ext))) { "Umbenennen fehlgeschlagen: $source" }
            }
        }

        private fun deleteDatabaseFiles(db: File) {
            DB_FILE_EXTENSIONS.forEach { ext -> File(db.path + ext).delete() }
        }

        private fun quarantineDatabase(db: File, suffix: String) {
            moveDatabase(db, File(db.path + suffix))
        }

        private fun openAndCheck(file: File, passphrase: ByteArray): SQLiteDatabase {
            val db = SQLiteDatabase.openDatabase(file.absolutePath, passphrase, null, SQLiteDatabase.OPEN_READWRITE, null)
            try {
                // Ein falscher Schlüssel fällt erst beim ersten Lesen auf.
                db.rawQuery("SELECT count(*) FROM sqlite_master").use { it.moveToFirst() }
            } catch (e: SQLiteException) {
                db.close()
                throw e
            }
            return db
        }
    }
}

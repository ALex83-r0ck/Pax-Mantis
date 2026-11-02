package com.alexander.paxmentis.data

import android.content.Context
import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.alexander.paxmentis.security.DatabaseKeyProvider
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Database(entities = [WorryEntry::class], version = 1)
abstract class PaxMentisDatabase : RoomDatabase() {
    abstract fun worryEntryDao(): WorryEntryDao

    companion object {
        private const val DB_NAME = "paxmentis_db"
        private const val TAG = "PaxMentisDatabase"

        /** Früher fest im Code hinterlegter Schlüssel – nur noch für die einmalige Umschlüsselung. */
        private val LEGACY_PASSPHRASE = "secure_passphrase".toByteArray(Charsets.UTF_8)

        @Volatile
        private var INSTANCE: PaxMentisDatabase? = null

        fun getInstance(context: Context, keyProvider: DatabaseKeyProvider): PaxMentisDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: build(context.applicationContext, keyProvider).also { INSTANCE = it }
            }

        private fun build(context: Context, keyProvider: DatabaseKeyProvider): PaxMentisDatabase {
            val passphrase = try {
                keyProvider.getOrCreatePassphrase()
            } catch (e: DatabaseKeyProvider.KeyLostException) {
                // Ohne Schlüssel ist die alte Datenbank nicht mehr lesbar. Statt bei jedem
                // Öffnen abzustürzen: Datei beiseitelegen (nicht löschen) und neu beginnen.
                Log.e(TAG, "Datenbank-Schlüssel verloren, alte Datenbank wird beiseitegelegt", e)
                quarantineDatabase(context)
                keyProvider.discardPassphrase()
                keyProvider.getOrCreatePassphrase()
            }
            migrateFromLegacyKey(context, passphrase, keyProvider)

            return Room.databaseBuilder(context, PaxMentisDatabase::class.java, DB_NAME)
                .openHelperFactory(SupportOpenHelperFactory(passphrase))
                .build()
        }

        /**
         * Bestehende Installationen haben eine mit [LEGACY_PASSPHRASE] verschlüsselte Datenbank.
         * Diese wird einmalig auf die neue Passphrase umgeschlüsselt, ohne Datenverlust.
         *
         * Die Migration gilt nur dann als erledigt, wenn sicher ist, dass die Datei mit dem
         * neuen Schlüssel lesbar ist (oder gar nicht existiert). Bei einem Absturz mittendrin
         * wird sie beim nächsten Start einfach erneut versucht.
         */
        private fun migrateFromLegacyKey(
            context: Context,
            newPassphrase: ByteArray,
            keyProvider: DatabaseKeyProvider
        ) {
            if (keyProvider.isLegacyMigrationDone()) return

            val file = context.getDatabasePath(DB_NAME)
            if (!file.exists()) {
                keyProvider.markLegacyMigrationDone()
                return
            }

            val rekeyed = try {
                openAndCheck(file.absolutePath, LEGACY_PASSPHRASE).use { db ->
                    // Rekey ist im WAL-Modus problematisch; Room schaltet WAL beim nächsten Öffnen wieder ein.
                    db.rawQuery("PRAGMA journal_mode = DELETE").use { it.moveToFirst() }
                    db.changePassword(newPassphrase)
                }
                true
            } catch (e: SQLiteException) {
                false
            }

            if (rekeyed || canOpen(file.absolutePath, newPassphrase)) {
                keyProvider.markLegacyMigrationDone()
            }
        }

        private fun quarantineDatabase(context: Context) {
            val db = context.getDatabasePath(DB_NAME)
            val suffix = ".unreadable-${System.currentTimeMillis()}"
            listOf("", "-wal", "-shm", "-journal").forEach { ext ->
                val file = java.io.File(db.path + ext)
                if (file.exists()) file.renameTo(java.io.File(db.path + suffix + ext))
            }
        }

        private fun openAndCheck(path: String, passphrase: ByteArray): SQLiteDatabase {
            val db = SQLiteDatabase.openDatabase(path, passphrase, null, SQLiteDatabase.OPEN_READWRITE, null)
            try {
                // Ein falscher Schlüssel fällt erst beim ersten Lesen auf.
                db.rawQuery("SELECT count(*) FROM sqlite_master").use { it.moveToFirst() }
            } catch (e: SQLiteException) {
                db.close()
                throw e
            }
            return db
        }

        private fun canOpen(path: String, passphrase: ByteArray): Boolean = try {
            openAndCheck(path, passphrase).close()
            true
        } catch (e: SQLiteException) {
            false
        }
    }
}

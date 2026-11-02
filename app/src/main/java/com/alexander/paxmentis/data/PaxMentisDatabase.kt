package com.alexander.paxmentis.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import java.security.SecureRandom
import android.util.Base64

@Database(entities = [WorryEntry::class], version = 1, exportSchema = true)
abstract class PaxMentisDatabase : RoomDatabase() {
    abstract fun worryEntryDao(): WorryEntryDao

    companion object {
        @Volatile
        private var INSTANCE: PaxMentisDatabase? = null

        private const val SECURE_PREFS_NAME = "pax_mentis_secure_db_prefs"
        private const val KEY_DB_PASSPHRASE = "db_passphrase_key"

        fun getInstance(context: Context): PaxMentisDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): PaxMentisDatabase {
            val passphrase = getOrCreateSecurePassphrase(context)
            val factory = SupportOpenHelperFactory(passphrase)

            return Room.databaseBuilder(
                context.applicationContext,
                PaxMentisDatabase::class.java,
                "paxmentis_db"
            )
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration()
                .build()
        }

        /**
         * Generiert beim ersten Start einen sicheren Zufallsschlüssel und speichert
         * ihn in den EncryptedSharedPreferences (abgesichert durch den Android KeyStore).
         */
        private fun getOrCreateSecurePassphrase(context: Context): ByteArray {
            val encryptedPrefs = try {
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

                EncryptedSharedPreferences.create(
                    context,
                    SECURE_PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (e: Exception) {
                // Fallback, falls das KeyStore-System auf einem spezifischen Gerät streikt
                context.getSharedPreferences(SECURE_PREFS_NAME, Context.MODE_PRIVATE)
            }

            var base64Key = encryptedPrefs.getString(KEY_DB_PASSPHRASE, null)

            if (base64Key == null) {
                // Generiere 256 Bit (32 Byte) sicheren Zufallsschlüssel
                val randomBytes = ByteArray(32)
                SecureRandom().nextBytes(randomBytes)
                base64Key = Base64.encodeToString(randomBytes, Base64.NO_WRAP)

                encryptedPrefs.edit().putString(KEY_DB_PASSPHRASE, base64Key).apply()
            }

            return Base64.decode(base64Key, Base64.NO_WRAP)
        }
    }
}
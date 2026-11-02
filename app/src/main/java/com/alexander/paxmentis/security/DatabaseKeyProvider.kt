package com.alexander.paxmentis.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Liefert die Passphrase für die SQLCipher-Datenbank.
 *
 * Die Passphrase wird einmalig zufällig erzeugt und nur verschlüsselt gespeichert.
 * Der Schlüssel, der sie verschlüsselt, liegt im Android Keystore (wenn vorhanden
 * hardwaregestützt) und kann das Gerät nicht verlassen. Eine kopierte Datenbankdatei
 * samt SharedPreferences ist damit ohne dieses Gerät nicht lesbar.
 *
 * Bewusst OHNE Nutzer-Authentifizierung am Keystore-Schlüssel: Sonst wäre die Datenbank
 * z. B. aus dem Widget oder nach einem abgebrochenen Prompt nicht mehr erreichbar.
 */
@Singleton
class DatabaseKeyProvider internal constructor(
    private val context: Context,
    prefsName: String,
    private val keyAlias: String,
    private val interimPrefsName: String
) {
    @Inject
    constructor(@ApplicationContext context: Context) :
        this(context, PREFS_NAME, KEY_ALIAS, INTERIM_PREFS_NAME)

    private val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    /**
     * Der gespeicherte Schlüssel ist endgültig nicht mehr entschlüsselbar (Keystore-Eintrag fehlt
     * oder passt nicht). Vorübergehende Keystore-Fehler fallen bewusst NICHT hierunter – sie werden
     * durchgereicht, damit nichts beiseitegelegt wird, was beim nächsten Start wieder lesbar wäre.
     */
    class KeyLostException(cause: Throwable) : Exception(cause)

    @Synchronized
    fun getOrCreatePassphrase(): ByteArray {
        prefs.getString(KEY_WRAPPED_PASSPHRASE, null)?.let { stored -> return unwrap(stored) }

        val random = ByteArray(32).also { SecureRandom().nextBytes(it) }
        // Hex-String statt Rohbytes: SQLCipher leitet daraus per KDF den Schlüssel ab,
        // und dieselben Bytes funktionieren identisch für open und changePassword.
        val passphrase = random.joinToString("") { "%02x".format(it) }.toByteArray(Charsets.UTF_8)

        // commit() statt apply(): Geht die Passphrase verloren, ist die Datenbank unlesbar.
        check(prefs.edit().putString(KEY_WRAPPED_PASSPHRASE, wrap(passphrase)).commit()) {
            "Datenbank-Schlüssel konnte nicht gespeichert werden"
        }
        return passphrase
    }

    /**
     * Legt den nicht mehr lesbaren Schlüssel unter [KEY_WRAPPED_PASSPHRASE] + [suffix] beiseite,
     * statt ihn zu löschen. Danach erzeugt [getOrCreatePassphrase] einen neuen.
     */
    @Synchronized
    fun setAsidePassphrase(suffix: String) {
        val stored = prefs.getString(KEY_WRAPPED_PASSPHRASE, null) ?: return
        check(
            prefs.edit()
                .putString(KEY_WRAPPED_PASSPHRASE + suffix, stored)
                .remove(KEY_WRAPPED_PASSPHRASE)
                .commit()
        ) { "Datenbank-Schlüssel konnte nicht beiseitegelegt werden" }
    }

    fun isLegacyMigrationDone(): Boolean = prefs.getBoolean(KEY_LEGACY_MIGRATED, false)

    fun markLegacyMigrationDone() {
        prefs.edit().putBoolean(KEY_LEGACY_MIGRATED, true).commit()
    }

    /**
     * Schlüssel früherer App-Stände – nur für die einmalige Umschlüsselung:
     * 1. der früher fest im Code hinterlegte Schlüssel,
     * 2. der Zwischenstand mit Zufallsschlüssel in EncryptedSharedPreferences
     *    (bzw. in unverschlüsselten Prefs gleichen Namens, falls der Keystore dort streikte).
     */
    fun legacyPassphrases(): List<ByteArray> = listOf(LEGACY_FIXED_PASSPHRASE.copyOf()) + interimPassphrases()

    @Suppress("DEPRECATION")
    private fun interimPassphrases(): List<ByteArray> {
        // Nur lesen, wenn es den Zwischenstand gab: EncryptedSharedPreferences.create legt sonst
        // Datei und Master-Key neu an.
        val file = File(context.applicationInfo.dataDir, "shared_prefs/$interimPrefsName.xml")
        if (!file.exists()) return emptyList()

        val encrypted = try {
            EncryptedSharedPreferences.create(
                context,
                interimPrefsName,
                MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            ).getString(INTERIM_KEY_PASSPHRASE, null)
        } catch (e: Exception) {
            // Tink wirft je nach Fehler GeneralSecurityException, IOException oder RuntimeExceptions.
            null
        }
        val plain = context.getSharedPreferences(interimPrefsName, Context.MODE_PRIVATE)
            .getString(INTERIM_KEY_PASSPHRASE, null)

        return listOfNotNull(encrypted, plain).distinct().mapNotNull { base64 ->
            try {
                Base64.decode(base64, Base64.NO_WRAP)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    private fun wrap(plain: ByteArray): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKeystoreKey())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plain)
        return Base64.encodeToString(iv + encrypted, Base64.NO_WRAP)
    }

    private fun unwrap(stored: String): ByteArray {
        val bytes = try {
            Base64.decode(stored, Base64.NO_WRAP)
        } catch (e: IllegalArgumentException) {
            throw KeyLostException(e)
        }
        if (bytes.size <= IV_LENGTH) throw KeyLostException(IllegalStateException("Gespeicherter Schlüssel zu kurz"))

        // Hier keinen neuen Keystore-Schlüssel erzeugen: Fehlt er, ist der alte endgültig weg.
        val key = loadKeystoreKey()
            ?: throw KeyLostException(IllegalStateException("Keystore-Eintrag fehlt"))

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, bytes, 0, IV_LENGTH))
        return try {
            cipher.doFinal(bytes, IV_LENGTH, bytes.size - IV_LENGTH)
        } catch (e: AEADBadTagException) {
            // Der Keystore-Schlüssel passt nicht zum gespeicherten Wert.
            throw KeyLostException(e)
        }
    }

    private fun loadKeystoreKey(): SecretKey? {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        return keyStore.getKey(keyAlias, null) as? SecretKey
    }

    private fun getOrCreateKeystoreKey(): SecretKey {
        loadKeystoreKey()?.let { return it }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }

    internal companion object {
        const val PREFS_NAME = "pax_mentis_secure"
        const val KEY_WRAPPED_PASSPHRASE = "db_passphrase_wrapped"
        const val KEY_LEGACY_MIGRATED = "db_legacy_key_migrated"
        const val KEY_ALIAS = "paxmentis_db_key"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_LENGTH = 12

        /** Früher fest im Code hinterlegter Schlüssel. */
        val LEGACY_FIXED_PASSPHRASE = "secure_passphrase".toByteArray(Charsets.UTF_8)

        /** Zwischenstand: Zufallsschlüssel (Base64, Rohbytes) in EncryptedSharedPreferences. */
        const val INTERIM_PREFS_NAME = "pax_mentis_secure_db_prefs"
        const val INTERIM_KEY_PASSPHRASE = "db_passphrase_key"
    }
}

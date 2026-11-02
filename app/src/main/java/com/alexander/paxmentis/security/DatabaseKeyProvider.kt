package com.alexander.paxmentis.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import java.security.SecureRandom
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
class DatabaseKeyProvider @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    @Synchronized
    fun getOrCreatePassphrase(): ByteArray {
        prefs.getString(KEY_WRAPPED_PASSPHRASE, null)?.let { return unwrap(it) }

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

    fun isLegacyMigrationDone(): Boolean = prefs.getBoolean(KEY_LEGACY_MIGRATED, false)

    fun markLegacyMigrationDone() {
        prefs.edit().putBoolean(KEY_LEGACY_MIGRATED, true).commit()
    }

    private fun wrap(plain: ByteArray): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKeystoreKey())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plain)
        return Base64.encodeToString(iv + encrypted, Base64.NO_WRAP)
    }

    private fun unwrap(stored: String): ByteArray {
        val bytes = Base64.decode(stored, Base64.NO_WRAP)
        val iv = bytes.copyOfRange(0, IV_LENGTH)
        val encrypted = bytes.copyOfRange(IV_LENGTH, bytes.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKeystoreKey(), GCMParameterSpec(128, iv))
        return cipher.doFinal(encrypted)
    }

    private fun getOrCreateKeystoreKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }

    private companion object {
        const val PREFS_NAME = "pax_mentis_secure"
        const val KEY_WRAPPED_PASSPHRASE = "db_passphrase_wrapped"
        const val KEY_LEGACY_MIGRATED = "db_legacy_key_migrated"
        const val KEY_ALIAS = "paxmentis_db_key"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_LENGTH = 12
    }
}

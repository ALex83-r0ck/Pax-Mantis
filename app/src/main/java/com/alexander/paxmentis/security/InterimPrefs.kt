package com.alexander.paxmentis.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.File

/**
 * Liest Werte aus dem lokalen Zwischenstand der App. Der hat Einstellungen und den DB-Schlüssel in
 * EncryptedSharedPreferences abgelegt – bzw. in unverschlüsselten Prefs gleichen Namens, wenn der
 * Keystore dort streikte. Nur für die einmalige Übernahme gedacht.
 */
internal object InterimPrefs {

    /**
     * @property byKey alle gefundenen Werte je Schlüssel, verschlüsselter Wert zuerst.
     * @property complete false, wenn es den Zwischenstand gibt, er sich aber gerade nicht entschlüsseln
     *   ließ (z. B. vorübergehender Keystore-Fehler). Dann später erneut versuchen.
     */
    class Values(val byKey: Map<String, List<String>>, val complete: Boolean)

    @Suppress("DEPRECATION")
    fun readStrings(context: Context, prefsName: String, keys: List<String>): Values {
        // Nur lesen, wenn es den Zwischenstand gab: EncryptedSharedPreferences.create legt sonst
        // Datei und Master-Key neu an.
        val file = File(context.applicationInfo.dataDir, "shared_prefs/$prefsName.xml")
        if (!file.exists()) return Values(emptyMap(), complete = true)

        val encrypted = try {
            EncryptedSharedPreferences.create(
                context,
                prefsName,
                MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Tink wirft je nach Fehler GeneralSecurityException, IOException oder RuntimeExceptions.
            null
        }
        val plain = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

        var complete = encrypted != null
        val byKey = keys.associateWith { key ->
            val fromEncrypted = try {
                encrypted?.getString(key, null)
            } catch (e: Exception) {
                complete = false
                null
            }
            val fromPlain = try {
                plain.getString(key, null)
            } catch (e: ClassCastException) {
                null
            }
            listOfNotNull(fromEncrypted, fromPlain).distinct()
        }
        return Values(byKey, complete)
    }
}

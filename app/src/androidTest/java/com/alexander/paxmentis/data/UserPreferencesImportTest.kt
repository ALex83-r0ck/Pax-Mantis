package com.alexander.paxmentis.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Übernahme von Nickname und Notfallkontakt aus dem lokalen Zwischenstand.
 * Eigene Prefs-Namen, damit die echten Einstellungen auf dem Testgerät unberührt bleiben.
 */
@RunWith(AndroidJUnit4::class)
class UserPreferencesImportTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() = cleanUp()

    @After
    fun tearDown() = cleanUp()

    @Test
    fun encryptedInterimSettings_areImported() {
        writeInterimEncrypted(nickname = "Kim", contact = "Mama", number = "+491701234567")

        val prefs = newPrefs()

        assertEquals("Kim", prefs.getNickname())
        assertEquals("Mama", prefs.getEmergencyContact())
        assertEquals("+491701234567", prefs.getEmergencyNumber())
    }

    @Test
    fun plainInterimFallback_isImported() {
        context.getSharedPreferences(INTERIM_PREFS, Context.MODE_PRIVATE).edit()
            .putString(UserPreferences.KEY_EMERGENCY_CONTACT, "Papa").commit()

        assertEquals("Papa", newPrefs().getEmergencyContact())
    }

    @Test
    fun existingValues_areNotOverwritten() {
        context.getSharedPreferences(TEST_PREFS, Context.MODE_PRIVATE).edit()
            .putString(UserPreferences.KEY_NICKNAME, "schon gesetzt").commit()
        writeInterimEncrypted(nickname = "Kim", contact = "Mama", number = "")

        val prefs = newPrefs()

        assertEquals("schon gesetzt", prefs.getNickname())
        assertEquals("Mama", prefs.getEmergencyContact())
    }

    @Test
    fun placeholderNumber_isNotImported() {
        writeInterimEncrypted(nickname = "Kim", contact = "Mama", number = UserPreferences.LEGACY_PLACEHOLDER_NUMBER)

        newPrefs()

        assertTrue(
            !context.getSharedPreferences(TEST_PREFS, Context.MODE_PRIVATE)
                .contains(UserPreferences.KEY_EMERGENCY_NUMBER)
        )
    }

    @Test
    fun importRunsOnlyOnce_laterChangesStay() {
        writeInterimEncrypted(nickname = "Kim", contact = "Mama", number = "")
        newPrefs().setEmergencyContact("")

        // Ein leerer Kontakt ist eine bewusste Entscheidung und darf nicht wieder überschrieben werden.
        assertEquals("", newPrefs().getEmergencyContact())
    }

    @Test
    fun noInterimState_nothingCreated() {
        newPrefs()

        assertTrue(!interimFile().exists())
        assertEquals("Mäusel", newPrefs().getNickname())
    }

    private fun newPrefs() = UserPreferences(context, TEST_PREFS, INTERIM_PREFS)

    @Suppress("DEPRECATION")
    private fun writeInterimEncrypted(nickname: String, contact: String, number: String) {
        val interim = EncryptedSharedPreferences.create(
            context,
            INTERIM_PREFS,
            MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        assertTrue(
            interim.edit()
                .putString(UserPreferences.KEY_NICKNAME, nickname)
                .putString(UserPreferences.KEY_EMERGENCY_CONTACT, contact)
                .putString(UserPreferences.KEY_EMERGENCY_NUMBER, number)
                .putFloat("speech_rate", 1.2f)
                .commit()
        )
    }

    private fun interimFile() =
        java.io.File(context.applicationInfo.dataDir, "shared_prefs/$INTERIM_PREFS.xml")

    private fun cleanUp() {
        listOf(TEST_PREFS, INTERIM_PREFS).forEach { name ->
            context.getSharedPreferences(name, Context.MODE_PRIVATE).edit().clear().commit()
            context.deleteSharedPreferences(name)
        }
    }

    private companion object {
        const val TEST_PREFS = "prefs_import_test"
        const val INTERIM_PREFS = "prefs_import_test_interim"
    }
}

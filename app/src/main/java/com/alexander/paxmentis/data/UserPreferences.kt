package com.alexander.paxmentis.data

import android.content.Context
import android.content.SharedPreferences
import com.alexander.paxmentis.security.InterimPrefs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferences internal constructor(
    context: Context,
    prefsName: String,
    interimPrefsName: String
) {
    @Inject
    constructor(@ApplicationContext context: Context) : this(context, PREFS_NAME, INTERIM_PREFS_NAME)

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    // Muss vor den Flows laufen, damit diese schon die übernommenen Werte sehen.
    init {
        importInterimSettings(context, interimPrefsName)
    }

    private val _nickname = MutableStateFlow(getNickname())
    val nicknameFlow: StateFlow<String> = _nickname.asStateFlow()

    private val _emergencyContact = MutableStateFlow(getEmergencyContact())
    val emergencyContactFlow: StateFlow<String> = _emergencyContact.asStateFlow()

    private val _emergencyNumber = MutableStateFlow(getEmergencyNumber())
    val emergencyNumberFlow: StateFlow<String> = _emergencyNumber.asStateFlow()

    fun getNickname(): String {
        return sharedPreferences.getString(KEY_NICKNAME, "Mäusel") ?: "Mäusel"
    }

    fun setNickname(nickname: String) {
        sharedPreferences.edit().putString(KEY_NICKNAME, nickname).apply()
        _nickname.value = nickname
    }

    // Kein Platzhalter-Name mehr: leer = noch nicht eingerichtet.
    fun getEmergencyContact(): String {
        return sharedPreferences.getString(KEY_EMERGENCY_CONTACT, "") ?: ""
    }

    fun setEmergencyContact(contact: String) {
        sharedPreferences.edit().putString(KEY_EMERGENCY_CONTACT, contact).apply()
        _emergencyContact.value = contact
    }

    // Früher war "+49123456789" als Default gesetzt – eine Fantasienummer, die im Notfall
    // gewählt worden wäre. Leer = noch nicht eingerichtet; der Panik-Screen weist darauf hin.
    fun getEmergencyNumber(): String {
        val stored = sharedPreferences.getString(KEY_EMERGENCY_NUMBER, "") ?: ""
        return if (stored == LEGACY_PLACEHOLDER_NUMBER) "" else stored
    }

    fun setEmergencyNumber(number: String) {
        sharedPreferences.edit().putString(KEY_EMERGENCY_NUMBER, number).apply()
        _emergencyNumber.value = number
    }

    /**
     * Der lokale Zwischenstand hat Nickname und Notfallkontakt verschlüsselt unter einem anderen
     * Namen gespeichert. Einmalig übernehmen, ohne bereits gesetzte Werte zu überschreiben.
     * Lässt sich der Zwischenstand gerade nicht entschlüsseln, beim nächsten Start erneut versuchen.
     */
    private fun importInterimSettings(context: Context, interimPrefsName: String) {
        if (sharedPreferences.getBoolean(KEY_INTERIM_IMPORTED, false)) return

        val interim = InterimPrefs.readStrings(context, interimPrefsName, IMPORTED_KEYS)
        val editor = sharedPreferences.edit()
        interim.byKey.forEach { (key, values) ->
            val value = values.firstOrNull { it.isNotBlank() && it != LEGACY_PLACEHOLDER_NUMBER }
            if (value != null && !sharedPreferences.contains(key)) editor.putString(key, value)
        }
        if (interim.complete) editor.putBoolean(KEY_INTERIM_IMPORTED, true)
        editor.commit()
    }

    internal companion object {
        const val PREFS_NAME = "pax_mentis_prefs"
        const val INTERIM_PREFS_NAME = "pax_mentis_secure_prefs"
        const val KEY_NICKNAME = "nickname"
        const val KEY_EMERGENCY_CONTACT = "emergency_contact"
        const val KEY_EMERGENCY_NUMBER = "emergency_number"
        const val KEY_INTERIM_IMPORTED = "interim_settings_imported"
        const val LEGACY_PLACEHOLDER_NUMBER = "+49123456789"
        val IMPORTED_KEYS = listOf(KEY_NICKNAME, KEY_EMERGENCY_CONTACT, KEY_EMERGENCY_NUMBER)
    }
}

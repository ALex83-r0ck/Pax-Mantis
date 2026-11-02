package com.alexander.paxmentis.data

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("pax_mentis_prefs", Context.MODE_PRIVATE)

    private val _nickname = MutableStateFlow(getNickname())
    val nicknameFlow: StateFlow<String> = _nickname.asStateFlow()

    private val _emergencyContact = MutableStateFlow(getEmergencyContact())
    val emergencyContactFlow: StateFlow<String> = _emergencyContact.asStateFlow()

    private val _emergencyNumber = MutableStateFlow(getEmergencyNumber())
    val emergencyNumberFlow: StateFlow<String> = _emergencyNumber.asStateFlow()

    fun getNickname(): String {
        return sharedPreferences.getString("nickname", "Mäusel") ?: "Mäusel"
    }

    fun setNickname(nickname: String) {
        sharedPreferences.edit().putString("nickname", nickname).apply()
        _nickname.value = nickname
    }

    // Kein Platzhalter-Name mehr: leer = noch nicht eingerichtet.
    fun getEmergencyContact(): String {
        return sharedPreferences.getString("emergency_contact", "") ?: ""
    }

    fun setEmergencyContact(contact: String) {
        sharedPreferences.edit().putString("emergency_contact", contact).apply()
        _emergencyContact.value = contact
    }

    // Früher war "+49123456789" als Default gesetzt – eine Fantasienummer, die im Notfall
    // gewählt worden wäre. Leer = noch nicht eingerichtet; der Panik-Screen weist darauf hin.
    fun getEmergencyNumber(): String {
        val stored = sharedPreferences.getString("emergency_number", "") ?: ""
        return if (stored == LEGACY_PLACEHOLDER_NUMBER) "" else stored
    }

    fun setEmergencyNumber(number: String) {
        sharedPreferences.edit().putString("emergency_number", number).apply()
        _emergencyNumber.value = number
    }

    private companion object {
        const val LEGACY_PLACEHOLDER_NUMBER = "+49123456789"
    }
}

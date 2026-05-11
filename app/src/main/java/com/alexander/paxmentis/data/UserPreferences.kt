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

    private val _speechRate = MutableStateFlow(getSpeechRate())
    val speechRateFlow: StateFlow<Float> = _speechRate.asStateFlow()

    fun getNickname(): String {
        return sharedPreferences.getString("nickname", "Mäusel") ?: "Mäusel"
    }

    fun setNickname(nickname: String) {
        sharedPreferences.edit().putString("nickname", nickname).apply()
        _nickname.value = nickname
    }

    fun getEmergencyContact(): String {
        return sharedPreferences.getString("emergency_contact", "Alexander") ?: "Alexander"
    }

    fun setEmergencyContact(contact: String) {
        sharedPreferences.edit().putString("emergency_contact", contact).apply()
        _emergencyContact.value = contact
    }

    fun getEmergencyNumber(): String {
        return sharedPreferences.getString("emergency_number", "+49123456789") ?: "+49123456789"
    }

    fun setEmergencyNumber(number: String) {
        sharedPreferences.edit().putString("emergency_number", number).apply()
        _emergencyNumber.value = number
    }

    fun getSpeechRate(): Float {
        return sharedPreferences.getFloat("speech_rate", 1.0f)
    }

    fun setSpeechRate(rate: Float) {
        sharedPreferences.edit().putFloat("speech_rate", rate).apply()
        _speechRate.value = rate
    }
}

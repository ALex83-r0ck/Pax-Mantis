package com.alexander.paxmentis.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
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
    companion object {
        private const val PREFS_NAME = "pax_mentis_secure_prefs"
        private const val KEY_NICKNAME = "nickname"
        private const val KEY_EMERGENCY_CONTACT = "emergency_contact"
        private const val KEY_EMERGENCY_NUMBER = "emergency_number"
        private const val KEY_SPEECH_RATE = "speech_rate"

        private const val DEFAULT_NICKNAME = "Mäusel"
        private const val DEFAULT_CONTACT = "Alexander"
        private const val DEFAULT_NUMBER = "+49123456789"
        private const val DEFAULT_SPEECH_RATE = 1.0f
    }

    private val sharedPreferences = run {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback auf normale SharedPreferences falls das Schlüsselsystem fehlschlägt
            // (verhindert harte App-Crashes bei KeyStore-Fehlern auf bestimmten Geräten)
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    private val _nickname = MutableStateFlow(getNickname())
    val nicknameFlow: StateFlow<String> = _nickname.asStateFlow()

    private val _emergencyContact = MutableStateFlow(getEmergencyContact())
    val emergencyContactFlow: StateFlow<String> = _emergencyContact.asStateFlow()

    private val _emergencyNumber = MutableStateFlow(getEmergencyNumber())
    val emergencyNumberFlow: StateFlow<String> = _emergencyNumber.asStateFlow()

    private val _speechRate = MutableStateFlow(getSpeechRate())
    val speechRateFlow: StateFlow<Float> = _speechRate.asStateFlow()

    fun getNickname(): String {
        return sharedPreferences.getString(KEY_NICKNAME, DEFAULT_NICKNAME) ?: DEFAULT_NICKNAME
    }

    fun setNickname(nickname: String) {
        sharedPreferences.edit().putString(KEY_NICKNAME, nickname).apply()
        _nickname.value = nickname
    }

    fun getEmergencyContact(): String {
        return sharedPreferences.getString(KEY_EMERGENCY_CONTACT, DEFAULT_CONTACT) ?: DEFAULT_CONTACT
    }

    fun setEmergencyContact(contact: String) {
        sharedPreferences.edit().putString(KEY_EMERGENCY_CONTACT, contact).apply()
        _emergencyContact.value = contact
    }

    fun getEmergencyNumber(): String {
        return sharedPreferences.getString(KEY_EMERGENCY_NUMBER, DEFAULT_NUMBER) ?: DEFAULT_NUMBER
    }

    fun setEmergencyNumber(number: String) {
        sharedPreferences.edit().putString(KEY_EMERGENCY_NUMBER, number).apply()
        _emergencyNumber.value = number
    }

    fun getSpeechRate(): Float {
        return sharedPreferences.getFloat(KEY_SPEECH_RATE, DEFAULT_SPEECH_RATE)
    }

    fun setSpeechRate(rate: Float) {
        sharedPreferences.edit().putFloat(KEY_SPEECH_RATE, rate).apply()
        _speechRate.value = rate
    }
}
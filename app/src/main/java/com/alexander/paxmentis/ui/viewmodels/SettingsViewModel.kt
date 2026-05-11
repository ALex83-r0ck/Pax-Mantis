package com.alexander.paxmentis.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.alexander.paxmentis.data.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    val nickname: StateFlow<String> = userPreferences.nicknameFlow
    val emergencyContact: StateFlow<String> = userPreferences.emergencyContactFlow
    val emergencyNumber: StateFlow<String> = userPreferences.emergencyNumberFlow
    val speechRate: StateFlow<Float> = userPreferences.speechRateFlow

    fun setNickname(name: String) {
        userPreferences.setNickname(name)
    }

    fun setEmergencyContact(contact: String) {
        userPreferences.setEmergencyContact(contact)
    }

    fun setEmergencyNumber(number: String) {
        userPreferences.setEmergencyNumber(number)
    }

    fun setSpeechRate(rate: Float) {
        userPreferences.setSpeechRate(rate)
    }
}

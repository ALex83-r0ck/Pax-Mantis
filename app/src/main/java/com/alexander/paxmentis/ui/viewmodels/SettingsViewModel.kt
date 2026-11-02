package com.alexander.paxmentis.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.alexander.paxmentis.data.UserPreferences
import com.alexander.paxmentis.ui.theme.ThemeMood
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    val nickname: StateFlow<String> = userPreferences.nicknameFlow
    val emergencyContact: StateFlow<String> = userPreferences.emergencyContactFlow
    val emergencyNumber: StateFlow<String> = userPreferences.emergencyNumberFlow
    val themeMood: StateFlow<ThemeMood> = userPreferences.themeMoodFlow
        .map(ThemeMood::fromStored)
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMood.fromStored(userPreferences.themeMoodFlow.value))

    fun setThemeMood(mood: ThemeMood) {
        userPreferences.setThemeMood(mood.name)
    }

    fun setNickname(name: String) {
        userPreferences.setNickname(name)
    }

    fun setEmergencyContact(contact: String) {
        userPreferences.setEmergencyContact(contact)
    }

    fun setEmergencyNumber(number: String) {
        userPreferences.setEmergencyNumber(number)
    }
}

package com.alexander.paxmentis.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexander.paxmentis.data.UserPreferences
import com.alexander.paxmentis.data.WorryEntry
import com.alexander.paxmentis.data.repository.WorryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: WorryRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    val nickname: StateFlow<String> = userPreferences.nicknameFlow
    val emergencyContact: StateFlow<String> = userPreferences.emergencyContactFlow
    val emergencyNumber: StateFlow<String> = userPreferences.emergencyNumberFlow

    fun addQuickEntry(onResult: (saved: Boolean) -> Unit) {
        viewModelScope.launch {
            val entry = WorryEntry(
                date = System.currentTimeMillis(),
                worry = "Schnelle Erfassung: Panik/Angst",
                reason = "Vom Dashboard aus gemeldet"
            )
            onResult(repository.insertEntry(entry))
        }
    }
}

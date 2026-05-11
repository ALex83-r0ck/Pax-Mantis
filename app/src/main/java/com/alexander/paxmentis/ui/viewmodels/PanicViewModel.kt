package com.alexander.paxmentis.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.alexander.paxmentis.data.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PanicViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {
    val emergencyContact: StateFlow<String> = userPreferences.emergencyContactFlow
    val emergencyNumber: StateFlow<String> = userPreferences.emergencyNumberFlow
}

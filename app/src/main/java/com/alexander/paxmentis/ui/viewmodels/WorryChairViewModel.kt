package com.alexander.paxmentis.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexander.paxmentis.data.UserPreferences
import com.alexander.paxmentis.data.WorryEntry
import com.alexander.paxmentis.data.repository.WorryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorryChairViewModel @Inject constructor(
    private val repository: WorryRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    val entries: StateFlow<List<WorryEntry>> = repository.getAllEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** true, wenn die Datenbank gerade nicht erreichbar ist – die Einträge sind dann nicht verloren. */
    val unavailable: StateFlow<Boolean> = repository.unavailable

    val nickname: StateFlow<String> = userPreferences.nicknameFlow

    fun addEntry(
        worry: String,
        reason: String?,
        worstCase: String?,
        solution: String?,
        onResult: (saved: Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            val entry = WorryEntry(
                date = System.currentTimeMillis(),
                worry = worry,
                reason = reason,
                worstCase = worstCase,
                solution = solution
            )
            onResult(repository.insertEntry(entry))
        }
    }

    fun updateEntry(entry: WorryEntry) {
        viewModelScope.launch {
            repository.updateEntry(entry)
        }
    }

    fun deleteEntry(entry: WorryEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
        }
    }
}

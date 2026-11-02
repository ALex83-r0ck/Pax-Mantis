package com.alexander.paxmentis.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexander.paxmentis.data.CrisisPlanItem
import com.alexander.paxmentis.data.UserPreferences
import com.alexander.paxmentis.data.repository.CrisisPlanRepository
import com.alexander.paxmentis.domain.CrisisPlanSection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CrisisPlanViewModel @Inject constructor(
    private val repository: CrisisPlanRepository,
    userPreferences: UserPreferences
) : ViewModel() {

    /** Einträge je Abschnitt; null, solange noch geladen wird (damit kein leerer Plan aufblitzt). */
    val plan: StateFlow<Map<CrisisPlanSection, List<CrisisPlanItem>>?> = repository.getAllItems()
        .map { items ->
            items.mapNotNull { item -> CrisisPlanSection.fromStored(item.section)?.let { it to item } }
                .groupBy({ it.first }, { it.second })
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val unavailable: StateFlow<Boolean> = repository.unavailable
    val emergencyContact: StateFlow<String> = userPreferences.emergencyContactFlow
    val emergencyNumber: StateFlow<String> = userPreferences.emergencyNumberFlow

    fun addItem(section: CrisisPlanSection, text: String, phone: String?, onResult: (saved: Boolean) -> Unit) {
        val position = (plan.value?.get(section)?.maxOfOrNull { it.position } ?: -1) + 1
        viewModelScope.launch {
            onResult(
                repository.insertItem(
                    CrisisPlanItem(section = section.name, text = text.trim(), phone = phone.normalized(), position = position)
                )
            )
        }
    }

    fun updateItem(item: CrisisPlanItem, text: String, phone: String?, onResult: (saved: Boolean) -> Unit) {
        viewModelScope.launch {
            onResult(repository.updateItem(item.copy(text = text.trim(), phone = phone.normalized())))
        }
    }

    fun deleteItem(item: CrisisPlanItem) {
        viewModelScope.launch { repository.deleteItem(item) }
    }

    private fun String?.normalized(): String? = this?.trim()?.ifBlank { null }
}

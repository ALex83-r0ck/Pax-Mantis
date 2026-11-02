package com.alexander.paxmentis.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexander.paxmentis.data.WorryEntry
import com.alexander.paxmentis.data.repository.WorryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Speichert den Entscheidungs-Check im (geschützten) Sorgenstuhl – inklusive des Zeitpunkts,
 * ab dem die Entscheidung frühestens getroffen werden soll. So wird aus der "24-Stunden-Regel"
 * eine nachlesbare Notiz statt eines bloßen Textes auf dem Bildschirm.
 */
@HiltViewModel
class AmbivalenceViewModel @Inject constructor(
    private val repository: WorryRepository
) : ViewModel() {

    fun saveDecisionCheck(decision: String, pros: String, cons: String, onResult: (saved: Boolean) -> Unit) {
        val now = System.currentTimeMillis()
        val earliest = SimpleDateFormat("EEEE, dd.MM. 'um' HH:mm 'Uhr'", Locale.GERMAN)
            .format(Date(now + 24 * 60 * 60 * 1000L))
        viewModelScope.launch {
            val saved = repository.insertEntry(
                WorryEntry(
                    date = now,
                    worry = "Entscheidung: ${decision.ifBlank { "(ohne Titel)" }}",
                    reason = pros.ifBlank { null }?.let { "Dafür: $it" },
                    worstCase = cons.ifBlank { null }?.let { "Dagegen: $it" },
                    solution = "Frühestens entscheiden am $earliest."
                )
            )
            onResult(saved)
        }
    }
}

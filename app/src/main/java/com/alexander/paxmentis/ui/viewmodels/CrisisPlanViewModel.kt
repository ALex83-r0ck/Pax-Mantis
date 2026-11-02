package com.alexander.paxmentis.ui.viewmodels

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexander.paxmentis.data.CrisisPlanItem
import com.alexander.paxmentis.data.UserPreferences
import com.alexander.paxmentis.data.repository.CrisisPlanRepository
import com.alexander.paxmentis.domain.CrisisPlanSection
import com.alexander.paxmentis.domain.PlanEntry
import com.alexander.paxmentis.domain.exportCrisisPlan
import com.alexander.paxmentis.domain.newEntries
import com.alexander.paxmentis.domain.parseCrisisPlan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private const val MAX_IMPORT_BYTES = 1_000_000

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

    /** Der Plan als lesbarer, wieder einlesbarer Text; null, solange er noch lädt. */
    fun exportText(): String? = plan.value?.let { exportCrisisPlan(it.toEntries(), today()) }

    fun exportTo(resolver: ContentResolver, uri: Uri, onResult: (message: String) -> Unit) {
        val text = exportText() ?: return onResult("Dein Plan lädt noch. Bitte gleich noch einmal versuchen.")
        viewModelScope.launch {
            val written = withContext(Dispatchers.IO) {
                runCatching {
                    resolver.openOutputStream(uri, "wt")?.use { it.write(text.toByteArray(Charsets.UTF_8)) } != null
                }.getOrDefault(false)
            }
            onResult(
                if (written) "Krisenplan gesichert. Die Datei ist nicht verschlüsselt – bewahr sie gut auf."
                else "Die Datei konnte nicht geschrieben werden."
            )
        }
    }

    fun importFrom(resolver: ContentResolver, uri: Uri, onResult: (message: String) -> Unit) {
        viewModelScope.launch {
            val content = withContext(Dispatchers.IO) {
                runCatching {
                    resolver.openInputStream(uri)?.use { input ->
                        val bytes = input.readNBytesCompat(MAX_IMPORT_BYTES + 1)
                        if (bytes.size > MAX_IMPORT_BYTES) null else bytes.toString(Charsets.UTF_8)
                    }
                }.getOrNull()
            }
            val current = plan.value
            val parsed = content?.let(::parseCrisisPlan)
            when {
                content == null -> onResult("Die Datei konnte nicht gelesen werden.")
                parsed == null -> onResult("Das ist keine Krisenplan-Datei von PaxMentis.")
                current == null -> onResult("Dein Plan lädt noch. Bitte gleich noch einmal versuchen.")
                else -> {
                    val items = newEntries(current.toEntries(), parsed)
                        .groupBy { it.section }
                        .flatMap { (section, entries) ->
                            val start = (current[section]?.maxOfOrNull { it.position } ?: -1) + 1
                            entries.mapIndexed { i, entry ->
                                CrisisPlanItem(section = section.name, text = entry.text, phone = entry.phone, position = start + i)
                            }
                        }
                    when {
                        items.isEmpty() -> onResult("Alles aus der Datei steht schon in deinem Plan.")
                        repository.insertItems(items) -> onResult("${items.size} Einträge übernommen.")
                        else -> onResult("Konnte gerade nicht gespeichert werden. Dein Plan ist unverändert.")
                    }
                }
            }
        }
    }

    private fun Map<CrisisPlanSection, List<CrisisPlanItem>>.toEntries(): List<PlanEntry> =
        flatMap { (section, items) -> items.map { PlanEntry(section, it.text, it.phone) } }

    private fun today(): String = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(Date())

    private fun String?.normalized(): String? = this?.trim()?.ifBlank { null }
}

/** InputStream.readNBytes gibt es erst ab API 33. */
private fun java.io.InputStream.readNBytesCompat(limit: Int): ByteArray {
    val out = java.io.ByteArrayOutputStream()
    val buffer = ByteArray(8192)
    while (out.size() < limit) {
        val read = read(buffer, 0, minOf(buffer.size, limit - out.size()))
        if (read < 0) break
        out.write(buffer, 0, read)
    }
    return out.toByteArray()
}

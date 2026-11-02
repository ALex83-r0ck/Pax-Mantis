package com.alexander.paxmentis.data.repository

import android.util.Log
import com.alexander.paxmentis.data.WorryEntry
import com.alexander.paxmentis.data.WorryEntryDao
import dagger.Lazy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Die Datenbank (inkl. Keystore-Zugriff und ggf. Umschlüsselung) wird erst bei der ersten
 * echten Nutzung und im IO-Thread geöffnet. So hängen Dashboard und Notfall-Screens nie
 * an der Datenbank.
 *
 * Jeder Datenbankfehler wird hier abgefangen: Ein Problem im Sorgenstuhl darf nie die ganze
 * App – und damit die Notfall-Screens – abstürzen lassen. Stattdessen wird [unavailable] gesetzt,
 * und Schreibzugriffe melden false. Die Einträge bleiben auf dem Gerät.
 */
@Singleton
class WorryRepository @Inject constructor(
    private val worryEntryDao: Lazy<WorryEntryDao>
) {
    private val _unavailable = MutableStateFlow(false)
    val unavailable: StateFlow<Boolean> = _unavailable.asStateFlow()

    /** Bei einem Fehler endet der Flow ohne weitere Werte; ein erneutes Abonnieren versucht es neu. */
    fun getAllEntries(): Flow<List<WorryEntry>> =
        flow { emitAll(worryEntryDao.get().getAll()) }
            .flowOn(Dispatchers.IO)
            .onEach { _unavailable.value = false }
            .catch { e -> report(e) }

    suspend fun insertEntry(entry: WorryEntry): Boolean = guarded { it.insert(entry) }

    suspend fun updateEntry(entry: WorryEntry): Boolean = guarded { it.update(entry) }

    suspend fun deleteEntry(entry: WorryEntry): Boolean = guarded { it.delete(entry) }

    private suspend fun guarded(block: suspend (WorryEntryDao) -> Unit): Boolean =
        withContext(Dispatchers.IO) {
            try {
                block(worryEntryDao.get())
                true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                report(e)
                false
            }
        }

    private fun report(e: Throwable) {
        Log.e(TAG, "Sorgenstuhl-Datenbank nicht verfügbar", e)
        _unavailable.value = true
    }

    private companion object {
        const val TAG = "WorryRepository"
    }
}

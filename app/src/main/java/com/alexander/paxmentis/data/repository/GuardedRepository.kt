package com.alexander.paxmentis.data.repository

import android.util.Log
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

/**
 * Gemeinsame Fehlerbehandlung für alles, was auf die Datenbank zugreift.
 *
 * Die Datenbank (inkl. Keystore-Zugriff und ggf. Umschlüsselung) wird erst bei der ersten
 * echten Nutzung und im IO-Thread geöffnet. Jeder Fehler wird abgefangen: Ein Datenbankproblem
 * darf nie die ganze App – und damit die Notfall-Screens – abstürzen lassen. Stattdessen wird
 * [unavailable] gesetzt, und Schreibzugriffe melden false. Die Daten bleiben auf dem Gerät.
 */
abstract class GuardedRepository(private val tag: String) {

    private val _unavailable = MutableStateFlow(false)
    val unavailable: StateFlow<Boolean> = _unavailable.asStateFlow()

    /** Bei einem Fehler endet der Flow ohne weitere Werte; ein erneutes Abonnieren versucht es neu. */
    protected fun <T> guardedFlow(source: () -> Flow<T>): Flow<T> =
        flow { emitAll(source()) }
            .flowOn(Dispatchers.IO)
            .onEach { _unavailable.value = false }
            .catch { e -> report(e) }

    protected suspend fun guarded(block: suspend () -> Unit): Boolean =
        withContext(Dispatchers.IO) {
            try {
                block()
                true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                report(e)
                false
            }
        }

    private fun report(e: Throwable) {
        Log.e(tag, "Datenbank nicht verfügbar", e)
        _unavailable.value = true
    }
}

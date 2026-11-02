package com.alexander.paxmentis.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Entsperr-Zustand für die geschützten Bereiche (Sorgenstuhl, Einstellungen).
 * Prozessweit, damit eine Bildschirmdrehung nicht erneut sperrt. Gesperrt wird,
 * sobald die App in den Hintergrund geht (siehe MainActivity.onStop).
 */
@Singleton
class SessionLock @Inject constructor() {
    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    fun unlock() {
        _isUnlocked.value = true
    }

    fun lock() {
        _isUnlocked.value = false
    }
}

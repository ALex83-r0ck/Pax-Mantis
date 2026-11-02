package com.alexander.paxmentis.security

import android.os.Build
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

class BiometricPromptManager(
    private val activity: FragmentActivity
) {
    fun showBiometricPrompt(
        title: String,
        description: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val manager = BiometricManager.from(activity)

        // WICHTIG: Ab Android 11 (API 30) dürfen BIOMETRIC_STRONG und DEVICE_CREDENTIAL
        // nicht mehr mit ODER (or) kombiniert werden, da dies zu einer IllegalArgumentException führt.
        // Wir nutzen hier primär Biometrie. Wenn Geräte-Credentials gewünscht sind,
        // sollte setAllowedAuthenticators(DEVICE_CREDENTIAL) separat verwendet werden.
        val authenticators = BIOMETRIC_STRONG

        when (manager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                val executor = ContextCompat.getMainExecutor(activity)
                val prompt = BiometricPrompt(
                    activity,
                    executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            onSuccess()
                        }

                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            super.onAuthenticationError(errorCode, errString)
                            onError(errString.toString())
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            // Optional: Wird aufgerufen, wenn der Fingerabdruck nicht erkannt wurde,
                            // aber der Dialog offen bleibt für weitere Versuche.
                        }
                    }
                )

                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(title)
                    .setDescription(description)
                    .setAllowedAuthenticators(authenticators)
                    // WICHTIG: Wenn negativeButton text gesetzt wird, darf DEVICE_CREDENTIAL nicht aktiv sein.
                    .setNegativeButtonText("Abbrechen")
                    .build()

                prompt.authenticate(promptInfo)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                onError("Dieses Gerät unterstützt keine biometrische Authentifizierung.")
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                onError("Es sind keine biometrischen Daten auf diesem Gerät hinterlegt.")
            }
            else -> {
                // Sicherheits-Fix: Kein stiller Bypass mehr über onSuccess(),
                // stattdessen sauberer Fehler-Callback, damit ungesicherte Geräte nicht unbemerkt passieren.
                onError("Biometrische Authentifizierung ist aktuell nicht verfügbar.")
            }
        }
    }
}
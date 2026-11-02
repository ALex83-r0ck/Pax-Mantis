package com.alexander.paxmentis.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Muss in onCreate() der Activity erzeugt werden, damit Ergebnisse auch nach einer
 * Bildschirmdrehung zugestellt werden.
 *
 * Erlaubt Biometrie ODER Geräte-PIN/Muster/Passwort. Diese Kombination funktioniert auf
 * allen API-Levels ab minSdk 24, und in einer Krise ist ein PIN-Fallback wichtig,
 * falls der Fingerabdruck mit feuchten oder zitternden Händen nicht erkannt wird.
 *
 * Ist gar keine Displaysperre eingerichtet, wird NICHT mehr automatisch entsperrt
 * (früheres Verhalten), sondern [AuthResult.Unavailable] gemeldet.
 */
class BiometricPromptManager(
    private val activity: FragmentActivity,
    private val onResult: (AuthResult) -> Unit
) {
    sealed interface AuthResult {
        data object Success : AuthResult
        data class Error(val message: String) : AuthResult
        data class Unavailable(val message: String) : AuthResult
    }

    private val authenticators = BIOMETRIC_WEAK or DEVICE_CREDENTIAL

    private val prompt = BiometricPrompt(
        activity,
        ContextCompat.getMainExecutor(activity),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onResult(AuthResult.Success)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                val message = when (errorCode) {
                    BiometricPrompt.ERROR_USER_CANCELED,
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                    BiometricPrompt.ERROR_CANCELED -> "Entsperren abgebrochen."
                    else -> errString.toString()
                }
                onResult(AuthResult.Error(message))
            }
            // onAuthenticationFailed (Finger nicht erkannt) wird vom System-Dialog selbst angezeigt.
        }
    )

    fun authenticate(title: String, description: String) {
        when (BiometricManager.from(activity).canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(title)
                    .setDescription(description)
                    .setAllowedAuthenticators(authenticators)
                    .build()
                prompt.authenticate(promptInfo)
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> onResult(
                AuthResult.Unavailable(
                    "Auf diesem Gerät ist keine Displaysperre eingerichtet. " +
                        "Richte in den Android-Einstellungen eine PIN, ein Muster oder einen Fingerabdruck ein, " +
                        "um diesen Bereich zu schützen."
                )
            )
            else -> onResult(
                AuthResult.Unavailable("Entsperren ist auf diesem Gerät gerade nicht möglich.")
            )
        }
    }
}

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
        val authenticators = if (Build.VERSION.SDK_INT >= 30) {
            BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        } else BIOMETRIC_STRONG

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
                    }
                )

                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(title)
                    .setDescription(description)
                    .setAllowedAuthenticators(authenticators)
                    .build()

                prompt.authenticate(promptInfo)
            }
            else -> {
                // Biometrics not available, bypass or show error
                onSuccess() 
            }
        }
    }
}

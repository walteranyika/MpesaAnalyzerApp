package com.walter.mbogo.security
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity


class BiometricHelper(
    private val activity: FragmentActivity,
    private val onAuthSuccess: () -> Unit,
    private val onAuthError: (String) -> Unit
) {

    private val executor = ContextCompat.getMainExecutor(activity)

    private val callback = @RequiresApi(Build.VERSION_CODES.P)
    object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            onAuthSuccess()
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            onAuthError(errString.toString())
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            onAuthError("Authentication failed.")
        }
    }

    private val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Biometric Login")
        .setSubtitle("Use your fingerprint to continue")
        .setNegativeButtonText("Cancel")
        .build()

    @RequiresApi(Build.VERSION_CODES.P)
    fun authenticate() {
        val biometricPrompt = BiometricPrompt(activity, executor, callback)
        biometricPrompt.authenticate(promptInfo)
    }
}

package com.example

import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

class BiometricInterface(val activity: AppCompatActivity, val webView: WebView) {

    @JavascriptInterface
    fun authenticateBiometrics(action: String) {
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    webView.post {
                        webView.evaluateJavascript(
                            "window.omyraOnAndroidBiometricSuccess?.('FINGERPRINT_MATCH')",
                            null
                        )
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    webView.post {
                        webView.evaluateJavascript(
                            "window.omyraOnAndroidBiometricError?.('$errString')",
                            null
                        )
                    }
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("OMYRA Pay Biometric Authorization")
            .setSubtitle("Confirm transaction with fingerprint")
            .setNegativeButtonText("Cancel")
            .build()

        activity.runOnUiThread { biometricPrompt.authenticate(promptInfo) }
    }
}

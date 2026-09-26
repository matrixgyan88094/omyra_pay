package com.example

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import com.example.security.QuantumSecurityManager
import com.example.ui.OfflineView
import com.example.ui.SplashScreenView
import com.example.ui.UpdateOverlay
import com.example.ui.theme.MyApplicationTheme
import com.example.updater.UpdateDownloadState
import com.example.updater.UpdateInfo
import com.example.updater.UpdateManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Block screenshots, screen recording, and Recent Apps task-switcher preview
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        // Hardware-backed quantum key and device integrity initialization
        QuantumSecurityManager.verifyEnvironmentIntegrity(applicationContext)

        // Initialize WebView
        initWebView()

        // Handle system back navigation inside WebView history
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (::webView.isInitialized && webView.canGoBack()) {
                    webView.goBack()
                } else {
                    finish()
                }
            }
        })

        setContent {
            MyApplicationTheme {
                MainContent(webView = webView)
            }
        }
    }

    private fun isEmulator(): Boolean {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        webView = WebView(this).apply {
            // In virtual/emulator container environments lacking DRM rendernodes (/dev/dri/renderD*),
            // software layer eliminates Mesa EGL rendernode access warnings and renderer instability.
            if (isEmulator()) {
                setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
            }

            // Configure Cookie Manager for persistent session handling
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(this, true)

            // Web settings security & optimization
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                cacheMode = WebSettings.LOAD_DEFAULT
                loadsImagesAutomatically = true
                mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                allowFileAccess = false
                allowContentAccess = false
                useWideViewPort = true
                loadWithOverviewMode = true
                setSupportZoom(false)
            }

            // Add official Biometric Interface bridge
            addJavascriptInterface(
                BiometricInterface(this@MainActivity, this),
                "AndroidBiometrics"
            )
        }
    }

    override fun onDestroy() {
        if (::webView.isInitialized) {
            webView.destroy()
        }
        super.onDestroy()
    }
}

@Composable
fun MainContent(webView: WebView) {
    val context = webView.context
    val coroutineScope = rememberCoroutineScope()

    var showSplash by remember { mutableStateOf(true) }
    var isOffline by remember { mutableStateOf(false) }
    var webProgress by remember { mutableFloatStateOf(0f) }
    var isPageFinished by remember { mutableStateOf(false) }

    // In-app GitHub update states
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var downloadState by remember { mutableStateOf<UpdateDownloadState>(UpdateDownloadState.Idle) }

    // Attach WebView clients
    LaunchedEffect(webView) {
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                webProgress = newProgress / 100f
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                isOffline = false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                isPageFinished = true
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                if (request?.isForMainFrame == true) {
                    isOffline = true
                }
            }

            override fun onRenderProcessGone(
                view: WebView?,
                detail: RenderProcessGoneDetail?
            ): Boolean {
                val didCrash = detail?.didCrash() == true
                Log.w("MainActivity", "WebView render process exited (didCrash=$didCrash). Recovering gracefully.")
                view?.let {
                    try {
                        it.destroy()
                    } catch (e: Throwable) {
                        // Safe cleanup
                    }
                }
                return true
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false
                val uri = Uri.parse(url)

                // Allow pay.omyra.org and https web traffic
                if (uri.scheme == "https" && (uri.host?.endsWith("omyra.org") == true || uri.host?.contains("omyra") == true)) {
                    return false
                }

                // Handle external Web3 / Payment / Wallet schemes
                val externalSchemes = listOf("wc", "ethereum", "solana", "bitcoin", "mailto", "tel")
                if (uri.scheme in externalSchemes) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                        return true
                    } catch (e: Exception) {
                        return true
                    }
                }

                // Allow other https navigation normally
                if (uri.scheme == "https") {
                    return false
                }

                return true
            }
        }

        webView.loadUrl("https://pay.omyra.org")
    }

    // Splash display timer (minimum duration for clean animated transition)
    LaunchedEffect(isPageFinished) {
        if (isPageFinished) {
            delay(2400)
            showSplash = false
        }
    }

    // Background GitHub release update verification
    LaunchedEffect(Unit) {
        delay(3000)
        try {
            val update = UpdateManager.checkLatestRelease(
                owner = BuildConfig.GITHUB_OWNER,
                repo = BuildConfig.GITHUB_REPO,
                currentVersion = BuildConfig.VERSION_NAME
            )
            if (update != null) {
                updateInfo = update
            }
        } catch (e: Exception) {
            // Silently continue if repository is not yet published or unreachable
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
    ) {
        // Main WebView display
        AndroidView(
            factory = { webView },
            modifier = Modifier.fillMaxSize()
        )

        // Web Loading indicator line
        if (webProgress in 0.01f..0.99f && !showSplash) {
            LinearProgressIndicator(
                progress = { webProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .statusBarsPadding(),
                color = Color(0xFFFF5F1F),
                trackColor = Color.Transparent
            )
        }

        // Offline screen overlay if connection fails
        if (isOffline) {
            OfflineView(
                onRetry = {
                    isOffline = false
                    webView.reload()
                }
            )
        }

        // Animated Splash Screen overlay
        AnimatedVisibility(
            visible = showSplash,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            SplashScreenView()
        }

        // In-App GitHub OTA Update Overlay
        val info = updateInfo
        if (info != null && !showSplash) {
            UpdateOverlay(
                updateInfo = info,
                downloadState = downloadState,
                onStartUpdate = {
                    coroutineScope.launch {
                        downloadState = UpdateDownloadState.Downloading(0f, 0L, info.apkSize)
                        try {
                            val apkFile = UpdateManager.downloadApk(
                                context = context,
                                apkUrl = info.apkDownloadUrl,
                                onProgress = { progress, downloaded, total ->
                                    downloadState = UpdateDownloadState.Downloading(
                                        progress = progress,
                                        downloadedBytes = downloaded,
                                        totalBytes = total
                                    )
                                }
                            )
                            downloadState = UpdateDownloadState.ReadyToInstall(apkFile)
                            delay(500)
                            UpdateManager.installApk(context, apkFile)
                        } catch (e: Exception) {
                            downloadState = UpdateDownloadState.Error(
                                e.message ?: "Unknown download error"
                            )
                        }
                    }
                },
                onDismiss = {
                    updateInfo = null
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

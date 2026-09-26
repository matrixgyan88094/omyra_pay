package com.example.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

object UpdateManager {

    private const val TAG = "UpdateManager"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Queries GitHub Releases API to verify if a newer release is published.
     */
    suspend fun checkLatestRelease(
        owner: String,
        repo: String,
        currentVersion: String
    ): UpdateInfo? = withContext(Dispatchers.IO) {
        val url = "https://api.github.com/repos/$owner/$repo/releases/latest"
        try {
            val request = Request.Builder()
                .url(url)
                .addHeader("Accept", "application/vnd.github.v3+json")
                .addHeader("User-Agent", "OMYRA-Pay-Android")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.d(TAG, "GitHub releases API returned code: ${response.code}")
                    return@withContext null
                }

                val bodyString = response.body?.string() ?: return@withContext null
                val json = JSONObject(bodyString)

                val tagName = json.optString("tag_name", "").trim()
                val cleanTag = tagName.removePrefix("v").trim()
                val cleanCurrent = currentVersion.removePrefix("v").trim()

                if (isNewerVersion(cleanTag, cleanCurrent)) {
                    val title = json.optString("name", "OMYRA Pay $tagName")
                    val notes = json.optString("body", "Security updates and performance enhancements.")

                    // Search for .apk asset
                    var apkUrl: String? = null
                    var apkSize: Long = 0L

                    val assets = json.optJSONArray("assets")
                    if (assets != null) {
                        for (i in 0 until assets.length()) {
                            val asset = assets.getJSONObject(i)
                            val name = asset.optString("name", "")
                            if (name.endsWith(".apk", ignoreCase = true)) {
                                apkUrl = asset.optString("browser_download_url", "")
                                apkSize = asset.optLong("size", 0L)
                                break
                            }
                        }
                    }

                    if (apkUrl != null) {
                        return@withContext UpdateInfo(
                            versionName = tagName,
                            title = title,
                            releaseNotes = notes,
                            apkDownloadUrl = apkUrl,
                            apkSize = apkSize
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking GitHub updates: ${e.message}")
        }
        null
    }

    /**
     * Compares semantic versions (e.g. 1.0.1 > 1.0.0).
     */
    fun isNewerVersion(remote: String, current: String): Boolean {
        if (remote.isBlank() || current.isBlank()) return false
        try {
            val remoteParts = remote.split(".").map { it.filter { char -> char.isDigit() }.toIntOrNull() ?: 0 }
            val currentParts = current.split(".").map { it.filter { char -> char.isDigit() }.toIntOrNull() ?: 0 }

            val maxLen = maxOf(remoteParts.size, currentParts.size)
            for (i in 0 until maxLen) {
                val r = remoteParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (r > c) return true
                if (r < c) return false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Version parse error: ${e.message}")
        }
        return false
    }

    /**
     * Downloads the APK file with real-time byte progress streaming.
     */
    suspend fun downloadApk(
        context: Context,
        apkUrl: String,
        onProgress: (progress: Float, downloadedBytes: Long, totalBytes: Long) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(apkUrl)
            .addHeader("User-Agent", "OMYRA-Pay-Android")
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IllegalStateException("Download failed with HTTP ${response.code}")
        }

        val body = response.body ?: throw IllegalStateException("Empty response body")
        val totalBytes = body.contentLength()

        val downloadDir = File(context.cacheDir, "updates").apply { mkdirs() }
        val apkFile = File(downloadDir, "omyra_pay_update.apk")
        if (apkFile.exists()) apkFile.delete()

        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null

        try {
            inputStream = body.byteStream()
            outputStream = FileOutputStream(apkFile)

            val buffer = ByteArray(8192)
            var downloadedBytes = 0L
            var read: Int

            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
                downloadedBytes += read
                val progress = if (totalBytes > 0) downloadedBytes.toFloat() / totalBytes else 0f
                onProgress(progress.coerceIn(0f, 1f), downloadedBytes, totalBytes)
            }
            outputStream.flush()
        } finally {
            inputStream?.close()
            outputStream?.close()
            response.close()
        }

        apkFile
    }

    /**
     * Launches the native Android package installer to install the downloaded APK.
     */
    fun installApk(context: Context, apkFile: File) {
        if (!apkFile.exists()) {
            Log.e(TAG, "APK file does not exist: ${apkFile.absolutePath}")
            return
        }

        // On Android 8.0+ (API 26), verify if app has permission to request package installs
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                return
            }
        }

        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        context.startActivity(installIntent)
    }
}

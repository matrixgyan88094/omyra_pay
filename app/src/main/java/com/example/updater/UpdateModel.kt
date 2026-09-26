package com.example.updater

import java.io.File

data class UpdateInfo(
    val versionName: String,
    val title: String,
    val releaseNotes: String,
    val apkDownloadUrl: String,
    val apkSize: Long
)

sealed interface UpdateDownloadState {
    data object Idle : UpdateDownloadState
    data class Downloading(
        val progress: Float, // 0.0f to 1.0f
        val downloadedBytes: Long,
        val totalBytes: Long
    ) : UpdateDownloadState
    data class ReadyToInstall(val apkFile: File) : UpdateDownloadState
    data class Error(val message: String) : UpdateDownloadState
}

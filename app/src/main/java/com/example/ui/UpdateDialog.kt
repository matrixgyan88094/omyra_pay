package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.updater.UpdateDownloadState
import com.example.updater.UpdateInfo
import java.util.Locale

@Composable
fun UpdateOverlay(
    updateInfo: UpdateInfo,
    downloadState: UpdateDownloadState,
    onStartUpdate: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF111827))
                .border(
                    width = 1.5.dp,
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFFFF5F1F), Color(0xFFFF8C42))
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF5F1F))
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Update Available",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Version Tag Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1F2937))
                            .border(1.dp, Color(0xFFFF5F1F).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = updateInfo.versionName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFFF8C42)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Release title / description
                Text(
                    text = updateInfo.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFE5E7EB)
                )

                if (updateInfo.releaseNotes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = updateInfo.releaseNotes.take(120),
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF),
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content depending on state
                when (downloadState) {
                    is UpdateDownloadState.Idle -> {
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF9CA3AF)
                                ),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = Brush.horizontalGradient(listOf(Color(0xFF374151), Color(0xFF374151)))
                                )
                            ) {
                                Text("Later")
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Button(
                                onClick = onStartUpdate,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF5F1F)
                                )
                            ) {
                                Text("Update Now", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    is UpdateDownloadState.Downloading -> {
                        val percent = (downloadState.progress * 100).toInt()
                        val downloadedMb = downloadState.downloadedBytes / (1024f * 1024f)
                        val totalMb = downloadState.totalBytes / (1024f * 1024f)

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Downloading update...",
                                    fontSize = 12.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                                Text(
                                    text = "$percent%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF5F1F)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { downloadState.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFFFF5F1F),
                                trackColor = Color(0xFF1F2937),
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            if (downloadState.totalBytes > 0) {
                                Text(
                                    text = String.format(
                                        Locale.US,
                                        "%.1f MB / %.1f MB",
                                        downloadedMb,
                                        totalMb
                                    ),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }
                    }

                    is UpdateDownloadState.ReadyToInstall -> {
                        Text(
                            text = "Download complete! Launching package installer...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF10B981)
                        )
                    }

                    is UpdateDownloadState.Error -> {
                        Column {
                            Text(
                                text = "Update failed: ${downloadState.message}",
                                fontSize = 12.sp,
                                color = Color(0xFFEF4444)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onStartUpdate,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF5F1F)
                                )
                            ) {
                                Text("Retry Update", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

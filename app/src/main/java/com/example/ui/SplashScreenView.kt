package com.example.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreenView(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "quantum_rings")

    // Rotation of outer subtle orbit ring
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_rotate"
    )

    // Pulse animation of glowing core
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "core_pulse"
    )

    // Animated status messages
    var statusText by remember { mutableStateOf("Initializing Security Engine...") }

    LaunchedEffect(Unit) {
        delay(700)
        statusText = "Hardware KeyStore Vault Connected"
        delay(800)
        statusText = "Quantum TLS 1.3 Handshake Verified"
        delay(800)
        statusText = "Connecting to OMYRA Pay Gateway..."
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFFAFBFD),
                        Color(0xFFF1F5F9)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .statusBarsPadding()
        ) {
            // Animated Logo Centerpiece with light elevation
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                // Outermost subtle rotating orange ring
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .scale(pulseScale)
                        .border(
                            width = 2.dp,
                            brush = Brush.sweepGradient(
                                listOf(
                                    Color(0xFFFF5F1F).copy(alpha = 0.1f),
                                    Color(0xFFFF5F1F).copy(alpha = 0.6f),
                                    Color(0xFFFF8C42).copy(alpha = 0.8f),
                                    Color(0xFFFF5F1F).copy(alpha = 0.1f)
                                )
                            ),
                            shape = CircleShape
                        )
                        .rotate(rotation)
                )

                // Soft warm glow halo
                Box(
                    modifier = Modifier
                        .size(145.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFFFF5F1F).copy(alpha = 0.18f),
                                    Color(0x00FFFFFF)
                                )
                            )
                        )
                )

                // Elevated Crisp Logo Card
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(116.dp)
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(28.dp),
                            spotColor = Color(0x33FF5F1F),
                            ambientColor = Color(0x1A000000)
                        )
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color.White)
                        .border(1.5.dp, Color(0xFFF1F5F9), RoundedCornerShape(28.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_omyra_logo),
                        contentDescription = "OMYRA Pay Official Logo",
                        modifier = Modifier
                            .size(116.dp)
                            .clip(RoundedCornerShape(28.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Brand Title
            Text(
                text = "OMYRA PAY",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle
            Text(
                text = "Official Web3 & Crypto Payment Gateway",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Status Badge (Modern Light Pill)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Pulsing status dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(Color(0xFFFF5F1F))
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = statusText,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF334155)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom Security Note
            Text(
                text = "Quantum Cryptographic Shield Active • TLS 1.3",
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

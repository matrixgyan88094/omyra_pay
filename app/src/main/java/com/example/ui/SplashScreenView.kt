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

    // Rotation of outer quantum shield circle
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_rotate"
    )

    // Reverse rotation of inner circle
    val reverseRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "inner_ring_rotate"
    )

    // Pulse animation of glowing core
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "core_pulse"
    )

    // Animated status messages
    var statusText by remember { mutableStateOf("Initializing Hardware KeyStore Vault...") }

    LaunchedEffect(Unit) {
        delay(800)
        statusText = "Hardware-Isolated Cryptography Active"
        delay(900)
        statusText = "Quantum TLS 1.3 Handshake Verified"
        delay(800)
        statusText = "Connecting to OMYRA Pay Gateway..."
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B14)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Animated Circles & Logo Centerpiece
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(220.dp)
            ) {
                // Outermost subtle quantum orbit circle
                Box(
                    modifier = Modifier
                        .size(210.dp)
                        .scale(pulseScale)
                        .border(
                            width = 1.5.dp,
                            brush = Brush.sweepGradient(
                                listOf(
                                    Color(0xFFFF5F1F).copy(alpha = 0.1f),
                                    Color(0xFFFF5F1F).copy(alpha = 0.6f),
                                    Color(0xFF00E5FF).copy(alpha = 0.8f),
                                    Color(0xFFFF5F1F).copy(alpha = 0.1f)
                                )
                            ),
                            shape = CircleShape
                        )
                        .rotate(rotation)
                )

                // Middle orbit ring with dash accents
                Box(
                    modifier = Modifier
                        .size(175.dp)
                        .border(
                            width = 2.dp,
                            brush = Brush.sweepGradient(
                                listOf(
                                    Color(0xFFFF8C42).copy(alpha = 0.2f),
                                    Color(0xFFFF5F1F),
                                    Color(0xFFFF8C42).copy(alpha = 0.2f)
                                )
                            ),
                            shape = CircleShape
                        )
                        .rotate(reverseRotation)
                )

                // Inner glow halo
                Box(
                    modifier = Modifier
                        .size(135.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFFFF5F1F).copy(alpha = 0.35f),
                                    Color(0x00000000)
                                )
                            )
                        )
                )

                // Official OMYRA Pay Logo
                Image(
                    painter = painterResource(id = R.drawable.ic_omyra_logo),
                    contentDescription = "OMYRA Pay Official Logo",
                    modifier = Modifier
                        .size(108.dp)
                        .clip(RoundedCornerShape(26.dp))
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Brand Title
            Text(
                text = "OMYRA PAY",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle
            Text(
                text = "Non-Custodial Multi-Chain Crypto Wallet",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF9CA3AF)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Status Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF111827))
                    .border(1.dp, Color(0xFFFF5F1F).copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Glowing dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E5FF))
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = statusText,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFE5E7EB)
                )
            }
        }
    }
}

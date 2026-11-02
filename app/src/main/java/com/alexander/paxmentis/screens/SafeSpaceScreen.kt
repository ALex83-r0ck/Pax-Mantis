package com.alexander.paxmentis.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexander.paxmentis.ui.tts.rememberTts

@Composable
fun SafeSpaceScreen() {
    val tts = rememberTts(speechRate = 0.7f, pitch = 0.85f)
    var isStarted by remember { mutableStateOf(false) }

    val pulse = rememberInfiniteTransition()
    val alpha by pulse.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val text = "Schließe deine Augen, wenn du magst. Stelle dir einen Ort vor, an dem du absolut sicher bist. Vielleicht ein Wald, ein Strand oder ein gemütliches Zimmer. Atme die Ruhe dieses Ortes ein. Du bist hier geborgen. Nichts kann dir hier etwas anhaben."

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD),
                        Color(0xFFBBDEFB)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                Icons.Default.Home,
                contentDescription = null,
                modifier = Modifier.size(64.dp).alpha(alpha),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "Dein Sicherer Ort",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (!isStarted) {
                Button(onClick = { 
                    isStarted = true
                    tts.speak(text)
                }) {
                    Text("Reise beginnen")
                }
            } else {
                Text(
                    text = text,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp,
                    // Fester Hintergrund (hellblau) -> feste dunkle Schrift statt Theme-Farbe.
                    color = Color(0xFF1A2B3C)
                )
                
                Spacer(modifier = Modifier.height(40.dp))
                
                Button(onClick = { 
                    tts.stop()
                    isStarted = false 
                }) {
                    Text("Beenden")
                }
            }
        }
    }
}

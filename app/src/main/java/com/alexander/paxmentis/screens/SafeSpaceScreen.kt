package com.alexander.paxmentis.screens

import android.speech.tts.TextToSpeech
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexander.paxmentis.data.UserPreferences
import java.util.Locale

@Composable
fun SafeSpaceScreen(
    userPreferences: UserPreferences
) {
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isStarted by remember { mutableStateOf(false) }
    val speechRate by userPreferences.speechRateFlow.collectAsState()

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

    DisposableEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.GERMAN
                tts?.setSpeechRate(speechRate * 0.7f) // Relativ (Basis 0.7f)
                tts?.setPitch(0.85f)
            }
        }
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    LaunchedEffect(speechRate) {
        tts?.setSpeechRate(speechRate * 0.7f)
    }

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
                    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                }) {
                    Text("Reise beginnen")
                }
            } else {
                Text(
                    text = text,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(40.dp))
                
                Button(onClick = { 
                    tts?.stop()
                    isStarted = false 
                }) {
                    Text("Beenden")
                }
            }
        }
    }
}

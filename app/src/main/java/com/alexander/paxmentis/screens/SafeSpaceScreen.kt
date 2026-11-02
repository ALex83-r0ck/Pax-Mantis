package com.alexander.paxmentis.screens

import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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

    val pulse = rememberInfiniteTransition(label = "SafeSpacePulse")
    val alpha by pulse.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "SafeSpaceAlpha"
    )

    val text = "Schließe deine Augen, wenn du magst. Stelle dir einen Ort vor, an dem du absolut sicher bist. Vielleicht ein Wald, ein Strand oder ein gemütliches Zimmer. Atme die Ruhe dieses Ortes ein. Du bist hier geborgen. Nichts kann dir hier etwas anhaben."

    DisposableEffect(context) {
        var textToSpeechInstance: TextToSpeech? = null
        textToSpeechInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeechInstance?.language = Locale.GERMAN
                textToSpeechInstance?.setSpeechRate(speechRate * 0.7f) // Langsam & ruhig
                textToSpeechInstance?.setPitch(0.85f)
            }
        }
        tts = textToSpeechInstance

        onDispose {
            textToSpeechInstance?.stop()
            textToSpeechInstance?.shutdown()
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
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .alpha(alpha),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Dein Sicherer Ort",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (!isStarted) {
                Button(
                    onClick = {
                        isStarted = true
                        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                    },
                    modifier = Modifier.fillMaxWidth(0.7f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "Reise beginnen",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f)),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.3f,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        tts?.stop()
                        isStarted = false
                    },
                    modifier = Modifier.fillMaxWidth(0.7f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "Beenden",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
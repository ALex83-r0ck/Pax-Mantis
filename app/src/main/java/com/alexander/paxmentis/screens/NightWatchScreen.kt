package com.alexander.paxmentis.screens

import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alexander.paxmentis.data.UserPreferences
import java.util.*

@Composable
fun NightWatchScreen(
    userPreferences: UserPreferences
) {
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    val speechRate by userPreferences.speechRateFlow.collectAsState()

    val pulse = rememberInfiniteTransition(label = "NightPulse")
    val glow by pulse.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "NightGlow"
    )

    DisposableEffect(context) {
        var textToSpeechInstance: TextToSpeech? = null
        textToSpeechInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeechInstance?.language = Locale.GERMAN
                textToSpeechInstance?.setSpeechRate(speechRate * 0.5f) // Sehr langsam
                textToSpeechInstance?.setPitch(0.7f) // Sehr tief / beruhigend
            }
        }
        tts = textToSpeechInstance
        onDispose {
            textToSpeechInstance?.stop()
            textToSpeechInstance?.shutdown()
        }
    }

    LaunchedEffect(speechRate) {
        tts?.setSpeechRate(speechRate * 0.5f)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF02040F), Color(0xFF05082E))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Bedtime,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color(0xFF5D737E).copy(alpha = glow)
            )

            Text(
                text = "Nachtwache",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE0E1DD)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {
                Text(
                    text = "Die Nacht ist die schwerste Zeit. Die Welt schläft, aber deine Gedanken sind wach. Das ist okay. Wir halten gemeinsam Wache bis zum Morgen.",
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFE0E1DD),
                    textAlign = TextAlign.Center,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.3f,
                    fontStyle = FontStyle.Italic
                )
            }

            Button(
                onClick = {
                    tts?.speak("Du musst jetzt nichts lösen. Alles darf warten bis die Sonne aufgeht. Ich bin hier bei dir.", TextToSpeech.QUEUE_FLUSH, null, null)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B263B)),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "Flüstern hören",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }

            Text(
                text = "In ${timeUntilMorning()} geht die Sonne auf.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF778DA9)
            )
        }
    }
}

fun timeUntilMorning(): String {
    val calendar = Calendar.getInstance()
    val nowHour = calendar.get(Calendar.HOUR_OF_DAY)
    val morningHour = 6

    val diff = if (nowHour >= morningHour) {
        (24 - nowHour) + morningHour
    } else {
        morningHour - nowHour
    }

    return "$diff Stunden"
}
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
import androidx.compose.ui.unit.sp
import java.util.*

@Composable
fun NightWatchScreen() {
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    
    val pulse = rememberInfiniteTransition()
    val glow by pulse.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    DisposableEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.GERMAN
                tts?.setSpeechRate(0.5f) // Extrem langsam
                tts?.setPitch(0.7f) // Sehr tief/beruhigend
            }
        }
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
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
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Icon(
                Icons.Default.Bedtime,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color(0xFF5D737E).copy(alpha = glow)
            )

            Text(
                "Nachtwache",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE0E1DD)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Die Nacht ist die schwerste Zeit. Die Welt schläft, aber deine Gedanken sind wach. Das ist okay. Wir halten gemeinsam Wache bis zum Morgen.",
                    modifier = Modifier.padding(24.dp),
                    color = Color(0xFFE0E1DD),
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp,
                    fontStyle = FontStyle.Italic
                )
            }

            Button(
                onClick = { 
                    tts?.speak("Du musst jetzt nichts lösen. Alles darf warten bis die Sonne aufgeht. Ich bin hier bei dir.", TextToSpeech.QUEUE_FLUSH, null, null)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B263B))
            ) {
                Text("Flüstern hören", color = Color.White)
            }

            Text(
                "In ${timeUntilMorning()} geht die Sonne auf.",
                fontSize = 14.sp,
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

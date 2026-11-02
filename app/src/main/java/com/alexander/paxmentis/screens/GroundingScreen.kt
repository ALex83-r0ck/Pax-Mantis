package com.alexander.paxmentis.screens

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alexander.paxmentis.data.UserPreferences
import java.util.Locale

@Composable
fun GroundingScreen(
    userPreferences: UserPreferences
) {
    var step by remember { mutableIntStateOf(5) }
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    val speechRate by userPreferences.speechRateFlow.collectAsState()

    val instructions = mapOf(
        5 to "Nenne 5 Dinge, die du gerade SIEHST.",
        4 to "Nenne 4 Dinge, die du gerade FÜHLST (z.B. Kleidung, Boden).",
        3 to "Nenne 3 Dinge, die du gerade HÖRST.",
        2 to "Nenne 2 Dinge, die du gerade RIECHST.",
        1 to "Nenne 1 Ding, das du gerade SCHMECKST."
    )

    val icons = mapOf(
        5 to Icons.Default.Visibility,
        4 to Icons.Default.TouchApp,
        3 to Icons.Default.Hearing,
        2 to Icons.Default.Air,
        1 to Icons.Default.Restaurant
    )

    DisposableEffect(context) {
        var textToSpeechInstance: TextToSpeech? = null
        textToSpeechInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeechInstance?.language = Locale.GERMAN
                textToSpeechInstance?.setSpeechRate(speechRate)
                textToSpeechInstance?.setPitch(0.9f) // Beruhigendere Stimmlage
                // Sofortigen Start für Schritt 5 triggern
                textToSpeechInstance?.speak(instructions[5], TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
        tts = textToSpeechInstance

        onDispose {
            textToSpeechInstance?.stop()
            textToSpeechInstance?.shutdown()
        }
    }

    LaunchedEffect(speechRate) {
        tts?.setSpeechRate(speechRate)
    }

    // Sprachausgabe bei jedem Schrittwechsel
    LaunchedEffect(step) {
        if (step in 1..5) {
            tts?.speak(instructions[step], TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.background
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
            Text(
                text = "Erdung (5-4-3-2-1)",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Icon(
                imageVector = icons[step] ?: Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.tertiary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = instructions[step] ?: "Fertig!",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                lineHeight = MaterialTheme.typography.headlineSmall.lineHeight * 1.2f
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (step > 1) {
                Button(
                    onClick = { step-- },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text(
                        text = "Erledigt / Nächster Schritt",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Gut gemacht. Du bist jetzt hier im Moment.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Button(
                        onClick = { step = 5 },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Noch einmal von vorn",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}
package com.alexander.paxmentis.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.saveable.rememberSaveable
import com.alexander.paxmentis.ui.tts.rememberTts

@Composable
fun GroundingScreen() {
    // 5..1 = aktuelle Aufgabe, 0 = fertig. Bisher wurden bei Schritt 1 Aufgabe und
    // "Gut gemacht" gleichzeitig angezeigt, und der letzte Schritt war nie bestätigbar.
    var step by rememberSaveable { mutableIntStateOf(5) }
    val tts = rememberTts(speechRate = 0.75f, pitch = 0.9f)

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

    LaunchedEffect(step) {
        val text = instructions[step]
        if (text != null) tts.speak(text) else tts.speak("Gut gemacht. Du bist jetzt hier im Moment.")
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
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                "Erdung (5-4-3-2-1)",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )
            
            Spacer(modifier = Modifier.height(40.dp))

            Icon(
                icons[step] ?: Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.tertiary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = instructions[step] ?: "Fertig!",
                fontSize = 22.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            if (step >= 1) {
                Button(
                    onClick = { step-- },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text(if (step == 1) "Erledigt" else "Erledigt / Nächster Schritt")
                }
            } else {
                Text(
                    "Gut gemacht. Du bist jetzt hier im Moment.",
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { step = 5 }) {
                    Text("Noch einmal von vorn")
                }
            }
        }
    }
}

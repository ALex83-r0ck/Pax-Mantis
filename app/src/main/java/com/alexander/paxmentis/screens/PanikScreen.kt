package com.alexander.paxmentis.screens

import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun PanicScreen(hasCallPermission: Boolean) {
    var step by remember { mutableIntStateOf(0) }
    var useTTS by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val tts = remember { mutableStateOf<TextToSpeech?>(null) }
    val steps = listOf(
        "Atme tief ein und zähle bis 4",
        "Halte den Atem für 4 Sekunden",
        "Atme langsam aus und zähle bis 8",
        "Fokussiere dich auf einen Punkt in deiner Umgebung",
        "Wiederhole die Atmung, bis du ruhiger bist"
    )

    // TTS-Initialisierung
    DisposableEffect(Unit) {
        tts.value = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.value?.language = Locale.GERMAN
                tts.value?.setSpeechRate(0.85f) // Langsamere, klarere Stimme
                tts.value?.setPitch(1.1f) // Freundlichere Tonlage
            }
        }
        onDispose {
            tts.value?.stop()
            tts.value?.shutdown()
        }
    }

    // Schrittwechsel mit TTS
    LaunchedEffect(step) {
        if (step < steps.size && useTTS && tts.value?.isSpeaking == false) {
            tts.value?.speak(steps[step], TextToSpeech.QUEUE_FLUSH, null, null)
        }
        if (step < steps.size - 1) {
            delay(10000) // 10 Sekunden pro Schritt
            step += 1
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Notfallhilfe",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (step < steps.size) {
                Text(
                    text = steps[step],
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Button(onClick = { step = (step + 1).coerceAtMost(steps.size - 1) }) {
                    Text("Nächster Schritt", fontSize = 18.sp)
                }
            } else {
                Text(
                    text = "Wenn es nicht besser wird, rufe Hilfe an:",
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (hasCallPermission) {
                    Button(onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:+49911313144") // Arzt-Nummer anpassen
                        }
                        context.startActivity(intent)
                    }) {
                        Text("Arzt anrufen", fontSize = 18.sp)
                    }
                    Button(onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:+49987654321") // Deine Nummer anpassen
                        }
                        context.startActivity(intent)
                    }) {
                        Text("Alexander anrufen", fontSize = 18.sp)
                    }
                } else {
                    Text("Berechtigung zum Anrufen fehlt.", fontSize = 16.sp, color = MaterialTheme.colorScheme.error)
                }
            }
            Button(onClick = { step = 0 }) {
                Text("Zurücksetzen", fontSize = 18.sp)
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("TTS Ansagen", fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                Switch(checked = useTTS, onCheckedChange = { useTTS = it })
            }
        }
    }
}
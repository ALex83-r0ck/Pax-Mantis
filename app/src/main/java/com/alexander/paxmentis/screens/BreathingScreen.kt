package com.alexander.paxmentis.screens

import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun BreathingScreen() {
    var isAnimating by remember { mutableStateOf(false) }
    var currentPhase by remember { mutableStateOf("Atem-Übung starten") }
    var inhaleTime by remember { mutableFloatStateOf(6f) }
    var holdTime by remember { mutableFloatStateOf(4f) }
    var exhaleTime by remember { mutableFloatStateOf(8f) }
    var useTTS by remember { mutableStateOf(true) }
    var showSettings by remember { mutableStateOf(false) }
    val scale = remember { Animatable(1f) }
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    val scope = rememberCoroutineScope()

    // Initialisiere Text-to-Speech mit Stimmenauswahl (falls verfügbar)
    DisposableEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.GERMAN
                tts?.setSpeechRate(1.0f) // Normale Geschwindigkeit
                tts?.setPitch(.95f) // Normale Tonlage
                // Versuche, eine andere Stimme zu setzen (abhängig vom Gerät)
                tts?.voices?.firstOrNull { it.name.contains("female", ignoreCase = true) }?.let { voice ->
                    tts?.voice = voice
                }
            }
        }
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    // Phasensteuerung mit Countdown
    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            // Countdown (3-2-1)
            currentPhase = "3"
            if (useTTS && tts?.isSpeaking == false) tts?.speak("Drei", TextToSpeech.QUEUE_FLUSH, null, null)
            delay(1000L)
            currentPhase = "2"
            if (useTTS && tts?.isSpeaking == false) tts?.speak("Zwei", TextToSpeech.QUEUE_FLUSH, null, null)
            delay(1000L)
            currentPhase = "1"
            if (useTTS && tts?.isSpeaking == false) tts?.speak("Eins", TextToSpeech.QUEUE_FLUSH, null, null)
            delay(1000L)

            while (isAnimating) {
                // Einatmen
                currentPhase = "Einatmen"
                if (useTTS && tts?.isSpeaking == false) tts?.speak("Einatmen", TextToSpeech.QUEUE_FLUSH, null, null)
                scope.launch { scale.animateTo(1.5f, animationSpec = tween((inhaleTime * 1000).toInt(), easing = LinearEasing)) }
                delay((inhaleTime * 1000 / 2).toLong()) // Halbiertes Delay für besseres Timing

                // Halten
                currentPhase = "Halten"
                if (useTTS && tts?.isSpeaking == false) tts?.speak("Halten", TextToSpeech.QUEUE_FLUSH, null, null)
                delay((holdTime * 1000 / 2).toLong())

                // Ausatmen
                currentPhase = "Ausatmen"
                if (useTTS && tts?.isSpeaking == false) tts?.speak("Ausatmen", TextToSpeech.QUEUE_FLUSH, null, null)
                scope.launch { scale.animateTo(1f, animationSpec = tween((exhaleTime * 1000).toInt(), easing = LinearEasing)) }
                delay((exhaleTime * 1000 / 2).toLong())
            }
            // Zurücksetzen bei Stop
            scope.launch { scale.animateTo(1f, animationSpec = tween(1000, easing = LinearEasing)) }
            currentPhase = "Atem-Übung starten"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        Color(0xFFE6F0FA)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Animierte Bubble
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .clip(CircleShape)
                    .scale(scale.value)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentPhase,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Klappbares Einstellungsmenü
            Button(onClick = { showSettings = !showSettings }) {
                Text(if (showSettings) "Einstellungen ausblenden" else "Einstellungen anzeigen")
            }
            AnimatedVisibility(
                visible = showSettings,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Einatmen: ${inhaleTime.toInt()}s", fontSize = 16.sp)
                    Slider(
                        value = inhaleTime,
                        onValueChange = { inhaleTime = it },
                        valueRange = 2f..10f,
                        steps = 7
                    )
                    Text("Halten: ${holdTime.toInt()}s", fontSize = 16.sp)
                    Slider(
                        value = holdTime,
                        onValueChange = { holdTime = it },
                        valueRange = 2f..10f,
                        steps = 7
                    )
                    Text("Ausatmen: ${exhaleTime.toInt()}s", fontSize = 16.sp)
                    Slider(
                        value = exhaleTime,
                        onValueChange = { exhaleTime = it },
                        valueRange = 2f..10f,
                        steps = 7
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("TTS Ansagen", fontSize = 16.sp)
                        Switch(checked = useTTS, onCheckedChange = { useTTS = it })
                    }
                }
            }

            // Start/Stop-Button
            Button(
                onClick = { isAnimating = !isAnimating },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = if (isAnimating) "Stop" else "Start",
                    fontSize = 18.sp
                )
            }
        }
    }
}
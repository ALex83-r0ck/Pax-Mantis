package com.alexander.paxmentis.screens

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alexander.paxmentis.data.UserPreferences
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun BreathingScreen(
    userPreferences: UserPreferences
) {
    var isAnimating by remember { mutableStateOf(false) }
    var currentPhase by remember { mutableStateOf("Bereit?") }

    // Werte als Float states
    var inhaleTime by remember { mutableFloatStateOf(4f) }
    var holdTime by remember { mutableFloatStateOf(4f) }
    var exhaleTime by remember { mutableFloatStateOf(6f) } // Psychologisch optimierter Start (längeres Ausatmen)

    var useTTS by remember { mutableStateOf(true) }
    var useHaptics by remember { mutableStateOf(true) }
    var showSettings by remember { mutableStateOf(false) }

    val scale = remember { Animatable(1f) }
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    val speechRate by userPreferences.speechRateFlow.collectAsState()

        // Sicherer Vibrator-Abruf
        val vibrator = remember {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(VibratorManager::class.java)?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Vibrator::class.java)
            }
        }

        DisposableEffect(context) {
            var textToSpeechInstance: TextToSpeech? = null
            textToSpeechInstance = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeechInstance?.language = Locale.GERMAN
                    textToSpeechInstance?.setSpeechRate(speechRate)
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

        // Präzise Atem-Schleife mit State-Abhängigkeit bei Timing-Änderungen
        LaunchedEffect(isAnimating, inhaleTime, holdTime, exhaleTime) {
            if (isAnimating) {
                while (isAnimating) {
                    // Einatmen (Sympathikus-Aktivierung / Fokus)
                    currentPhase = "Einatmen"
                    speak(tts, "Einatmen", useTTS)
                    vibrate(vibrator, useHaptics)

                    scale.animateTo(
                        targetValue = 1.5f,
                        animationSpec = tween((inhaleTime * 1000).toInt(), easing = LinearEasing)
                    )

                    // Halten (Integration)
                    if (holdTime > 0f) {
                        currentPhase = "Halten"
                        speak(tts, "Halten", useTTS)
                        delay((holdTime * 1000).toLong())
                    }

                    // Ausatmen (Parasympathikus / Vagusnerv-Aktivierung)
                    currentPhase = "Ausatmen"
                    speak(tts, "Ausatmen", useTTS)
                    vibrate(vibrator, useHaptics)

                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = tween((exhaleTime * 1000).toInt(), easing = LinearEasing)
                    )
                }
            } else {
                scale.snapTo(1f)
                currentPhase = "Bereit?"
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Animierter Atem-Kreis
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .scale(scale.value)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentPhase,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Start / Stop Button
                Button(
                    onClick = { isAnimating = !isAnimating },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAnimating) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text(
                        text = if (isAnimating) "Stop" else "Start",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = { showSettings = !showSettings }) {
                    Text(if (showSettings) "Einstellungen ausblenden" else "Einstellungen anpassen")
                }

                AnimatedVisibility(visible = showSettings) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Einatmen: ${inhaleTime.toInt()}s", style = MaterialTheme.typography.bodyMedium)
                            Slider(value = inhaleTime, onValueChange = { inhaleTime = it }, valueRange = 2f..10f)

                            Text("Halten: ${holdTime.toInt()}s", style = MaterialTheme.typography.bodyMedium)
                            Slider(value = holdTime, onValueChange = { holdTime = it }, valueRange = 0f..10f)

                            Text("Ausatmen: ${exhaleTime.toInt()}s", style = MaterialTheme.typography.bodyMedium)
                            Slider(value = exhaleTime, onValueChange = { exhaleTime = it }, valueRange = 2f..10f)

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = useTTS, onCheckedChange = { useTTS = it })
                                    Text("Sprache", style = MaterialTheme.typography.bodyMedium)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = useHaptics, onCheckedChange = { useHaptics = it })
                                    Text("Vibration", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun speak(tts: TextToSpeech?, text: String, enabled: Boolean) {
        if (enabled && tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    private fun vibrate(vibrator: Vibrator?, enabled: Boolean) {
        if (enabled && vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(80)
            }
        }
    }
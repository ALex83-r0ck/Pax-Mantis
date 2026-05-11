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
import androidx.compose.ui.unit.sp
import com.alexander.paxmentis.data.UserPreferences
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun BreathingScreen(
    userPreferences: UserPreferences
) {
    var isAnimating by remember { mutableStateOf(false) }
    var currentPhase by remember { mutableStateOf("Bereit?") }
    var inhaleTime by remember { mutableFloatStateOf(4f) }
    var holdTime by remember { mutableFloatStateOf(4f) }
    var exhaleTime by remember { mutableFloatStateOf(4f) }
    var useTTS by remember { mutableStateOf(true) }
    var useHaptics by remember { mutableStateOf(true) }
    var showSettings by remember { mutableStateOf(false) }
    val scale = remember { Animatable(1f) }
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    val speechRate by userPreferences.speechRateFlow.collectAsState()

    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(VibratorManager::class.java)
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Vibrator::class.java)
        }
    }

    DisposableEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.GERMAN
                tts?.setSpeechRate(speechRate)
            }
        }
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    LaunchedEffect(speechRate) {
        tts?.setSpeechRate(speechRate)
    }

    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            while (isAnimating) {
                // Einatmen
                currentPhase = "Einatmen"
                if (useTTS) tts?.speak("Einatmen", TextToSpeech.QUEUE_FLUSH, null, null)
                if (useHaptics) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(100)
                    }
                }
                
                scale.animateTo(
                    targetValue = 1.5f,
                    animationSpec = tween((inhaleTime * 1000).toInt(), easing = LinearEasing)
                )

                // Halten
                currentPhase = "Halten"
                if (useTTS) tts?.speak("Halten", TextToSpeech.QUEUE_FLUSH, null, null)
                delay((holdTime * 1000).toLong())

                // Ausatmen
                currentPhase = "Ausatmen"
                if (useTTS) tts?.speak("Ausatmen", TextToSpeech.QUEUE_FLUSH, null, null)
                if (useHaptics) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(100)
                    }
                }
                
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
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
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
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { isAnimating = !isAnimating },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAnimating) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(if (isAnimating) "Stop" else "Start", fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { showSettings = !showSettings }) {
                Text(if (showSettings) "Einstellungen ausblenden" else "Einstellungen anpassen")
            }

            AnimatedVisibility(visible = showSettings) {
                Card(
                    modifier = Modifier.padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Einatmen: ${inhaleTime.toInt()}s")
                        Slider(value = inhaleTime, onValueChange = { inhaleTime = it }, valueRange = 2f..10f)
                        
                        Text("Halten: ${holdTime.toInt()}s")
                        Slider(value = holdTime, onValueChange = { holdTime = it }, valueRange = 0f..10f)
                        
                        Text("Ausatmen: ${exhaleTime.toInt()}s")
                        Slider(value = exhaleTime, onValueChange = { exhaleTime = it }, valueRange = 2f..10f)

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = useTTS, onCheckedChange = { useTTS = it })
                            Text("Sprachausgabe")
                            Spacer(Modifier.width(16.dp))
                            Checkbox(checked = useHaptics, onCheckedChange = { useHaptics = it })
                            Text("Vibration")
                        }
                    }
                }
            }
        }
    }
}

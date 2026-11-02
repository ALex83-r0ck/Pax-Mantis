package com.alexander.paxmentis.screens

import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alexander.paxmentis.R
import com.alexander.paxmentis.data.UserPreferences
import kotlin.random.Random
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun SleepAidScreen(
    userPreferences: UserPreferences
) {
    var isMeditating by remember { mutableStateOf(false) }
    var isPlayingMusic by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val mediaPlayer = remember { mutableStateOf<MediaPlayer?>(null) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    val starAlpha = remember { Animatable(0f) }
    val speechRate by userPreferences.speechRateFlow.collectAsState()

    val affirmations = listOf(
        "Du bist sicher. Du bist nicht allein.",
        "Alles, was du jetzt fühlst, darf da sein – und wird vorbeiziehen.",
        "Mit jedem Atemzug kommt mehr Ruhe in dich.",
        "Du musst nichts leisten – es ist okay, einfach nur zu sein.",
        "Auch wenn es stürmt: In dir gibt es einen ruhigen Kern.",
        "Dein Körper weiß, wie er sich entspannen kann. Lass ihn machen.",
        "Morgen ist ein neuer Tag. Heute darfst du einfach ruhen."
    )

    DisposableEffect(context) {
        var textToSpeechInstance: TextToSpeech? = null
        textToSpeechInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeechInstance?.language = Locale.GERMAN
                textToSpeechInstance?.setSpeechRate(speechRate * 0.65f) // Sehr ruhig & langsam
                textToSpeechInstance?.setPitch(0.8f) // Tiefer, beruhigender Pitch
            }
        }
        tts = textToSpeechInstance

        onDispose {
            textToSpeechInstance?.stop()
            textToSpeechInstance?.shutdown()
        }
    }

    LaunchedEffect(speechRate) {
        tts?.setSpeechRate(speechRate * 0.65f)
    }

    DisposableEffect(context) {
        val player = MediaPlayer.create(context, R.raw.ambientpian).apply {
            isLooping = true
            setVolume(0.3f, 0.3f)
        }
        mediaPlayer.value = player

        onDispose {
            player.stop()
            player.release()
        }
    }

    LaunchedEffect(isMeditating) {
        if (isMeditating) {
            while (isMeditating) {
                val text = affirmations.random()
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                delay(15000) // Alle 15 Sekunden eine Affirmation
            }
        }
    }

    LaunchedEffect(Unit) {
        starAlpha.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D1B2A), Color(0xFF1B263B), Color(0xFF415A77))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val random = Random(42) // Konsistente Sternen-Positionen
            repeat(100) {
                val x = random.nextFloat() * size.width
                val y = random.nextFloat() * size.height
                drawCircle(
                    color = Color.White.copy(alpha = starAlpha.value * random.nextFloat()),
                    radius = random.nextFloat() * 3f,
                    center = Offset(x, y)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.NightsStay,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color(0xFFFFD166)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Zeit für Ruhe",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Lass den Tag sanft ausklingen.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f),
                    fontStyle = FontStyle.Italic
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Meditation Control
                    Button(
                        onClick = { isMeditating = !isMeditating },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isMeditating) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(
                            imageVector = if (isMeditating) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = if (isMeditating) "Meditation stoppen" else "Sanfte Affirmationen starten",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    // Music Control
                    OutlinedButton(
                        onClick = {
                            isPlayingMusic = !isPlayingMusic
                            if (isPlayingMusic) mediaPlayer.value?.start() else mediaPlayer.value?.pause()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(
                            imageVector = if (isPlayingMusic) Icons.Default.MusicOff else Icons.Default.MusicNote,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = if (isPlayingMusic) "Musik aus" else "Beruhigende Klänge an",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            if (isMeditating) {
                Text(
                    text = "Atme ganz ruhig ein und aus...",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}
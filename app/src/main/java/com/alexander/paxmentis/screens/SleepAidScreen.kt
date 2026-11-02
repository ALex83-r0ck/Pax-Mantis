package com.alexander.paxmentis.screens

import android.media.MediaPlayer
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
import androidx.compose.ui.unit.sp
import com.alexander.paxmentis.R
import kotlinx.coroutines.delay
import com.alexander.paxmentis.ui.tts.rememberTts
import kotlin.random.Random

@Composable
fun SleepAidScreen() {
    var isMeditating by remember { mutableStateOf(false) }
    var isPlayingMusic by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val mediaPlayer = remember { mutableStateOf<MediaPlayer?>(null) }
    val tts = rememberTts(speechRate = 0.65f, pitch = 0.8f)
    val starAlpha = remember { Animatable(0f) }

    val affirmations = listOf(
        "Du bist sicher. Du bist nicht allein.",
        "Alles, was du jetzt fühlst, darf da sein – und wird vorbeiziehen.",
        "Mit jedem Atemzug kommt mehr Ruhe in dich.",
        "Du musst nichts leisten – es ist okay, einfach nur zu sein.",
        "Auch wenn es stürmt: In dir gibt es einen ruhigen Kern.",
        "Dein Körper weiß, wie er sich entspannen kann. Lass ihn machen.",
        "Morgen ist ein neuer Tag. Heute darfst du einfach ruhen."
    )

    DisposableEffect(Unit) {
        // MediaPlayer.create() liefert null, wenn die Datei nicht geladen werden kann.
        val player: MediaPlayer? = MediaPlayer.create(context, R.raw.ambientpian)
        player?.isLooping = true
        player?.setVolume(0.3f, 0.3f)
        mediaPlayer.value = player
        onDispose {
            player?.stop()
            player?.release()
            mediaPlayer.value = null
        }
    }

    LaunchedEffect(isMeditating) {
        if (isMeditating) {
            var last: String? = null
            while (true) {
                // Nicht zweimal dieselbe Affirmation hintereinander.
                val text = affirmations.filter { it != last }.random()
                last = text
                tts.speak(text)
                delay(15000) // Alle 15 Sekunden eine Affirmation
            }
        } else {
            tts.stop()
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
            val random = Random(42) // Konsistente Sterne
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
                Icons.Default.NightsStay,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color(0xFFFFD166)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Zeit für Ruhe",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Lass den Tag sanft ausklingen.",
                    fontSize = 16.sp,
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
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isMeditating) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(if (isMeditating) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (isMeditating) "Meditation stoppen" else "Sanfte Affirmationen starten")
                    }

                    // Music Control
                    OutlinedButton(
                        onClick = {
                            isPlayingMusic = !isPlayingMusic
                            if (isPlayingMusic) mediaPlayer.value?.start() else mediaPlayer.value?.pause()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(if (isPlayingMusic) Icons.Default.MusicOff else Icons.Default.MusicNote, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (isPlayingMusic) "Musik aus" else "Beruhigende Klänge an")
                    }
                }
            }

            if (isMeditating) {
                Text(
                    "Atme ganz ruhig ein und aus...",
                    color = Color.White,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

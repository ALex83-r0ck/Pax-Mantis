package com.alexander.paxmentis.screens

import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexander.paxmentis.R
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.random.Random

@Composable
fun SleepAidScreen() {
    var isMeditating by remember { mutableStateOf(false) }
    var meditationTime by remember { mutableIntStateOf(0) }
    var useTTS by remember { mutableStateOf(true) }
    var isPlayingMusic by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val mediaPlayer = remember { mutableStateOf<MediaPlayer?>(null) }
    val tts = remember { mutableStateOf<TextToSpeech?>(null) }
    val starAlpha = remember { Animatable(0f) }
    listOf(
        "Du bist sicher. Du bist nicht allein.",
        "Alles, was du jetzt fühlst, darf da sein – und wird vorbeiziehen.",
        "Mit jedem Atemzug kommt mehr Ruhe in dich.",
        "Du musst nichts leisten – es ist okay, einfach nur zu sein.",
        "Du bist stärker, als du denkst.",
        "Es ist in Ordnung, Hilfe anzunehmen. Du darfst dich tragen lassen.",
        "In dir ist ein Ort der Ruhe – und du findest langsam dorthin.",
        "Dein Körper weiß, wie er sich entspannen kann. Lass ihn machen.",
        "Du darfst loslassen – Stück für Stück, ohne Druck.",
        "Auch wenn es stürmt: In dir gibt es einen ruhigen Kern."
    )
    listOf(
        "Spüre deine Füße. Spanne sie kurz an – halte – und lass sie wieder los.",
        "Nun sind die Bein dran. Atme tief ein und entspanne sie beim Ausatmen.",
        "Lenke den Atem in deinen Bauch. Spanne leicht an, halte den Atem kurz – und lasse los.",
        "Ziehe deine Schultern zu den Ohren – halte – und lass sie fallen, ganz bewusst.",
        "Lass deinen Kiefer locker werden. Vielleicht magst du leicht gähnen.",
        "Spüre deinen Nacken – lass ihn weich und warm werden mit jedem Atemzug.",
        "Lege eine Hand auf dein Herz. Spüre den ruhigen Kontakt. Du bist da.",
        "Atme ruhig. Stell dir vor, wie dein Atem Ruhe in deinen ganzen Körper bringt.",
        "Bleibe noch einen Moment hier – nichts muss passieren. Alles ist gut so, wie es gerade ist."
    )
    // TTS Init
    DisposableEffect(Unit) {
        tts.value = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.value?.language = Locale.GERMAN
            }
        }
        onDispose {
            tts.value?.stop()
            tts.value?.shutdown()
        }
    }

    // Player Init
    DisposableEffect(Unit) {
        mediaPlayer.value = MediaPlayer.create(context, R.raw.ambientpian).apply {
            isLooping = true
        }
        onDispose {
            mediaPlayer.value?.stop()
            mediaPlayer.value?.release()
        }
    }

    // Timer Meditation
    LaunchedEffect(isMeditating) {
        if (isMeditating) {
            while (isMeditating) {
                delay(1000)
                meditationTime += 1
                if (meditationTime % 60 == 0 && useTTS) {
                    tts.value?.speak("Atme tief und entspanne dich", TextToSpeech.QUEUE_FLUSH,
                        null, null)
                }
            }
        }
    }

    // Star animation
    LaunchedEffect(Unit) {
        starAlpha.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D1B2A),
                        Color(0xFF1B263B)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Sternen-Hintergrund
        Canvas(modifier = Modifier.fillMaxSize()) {
            repeat(50) {
                val x = Random.nextFloat() * size.width
                val y = Random.nextFloat() * size.height
                drawCircle(
                    color = Color.White.copy(alpha = starAlpha.value * Random.nextFloat()),
                    radius = 2f,
                    center = Offset(x, y)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Schlafhilfe",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Zeit: ${meditationTime / 60}:${(meditationTime % 60).toString().padStart(2, '0')}",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(onClick = { isMeditating = !isMeditating }) {
                    Text(
                        text = if (isMeditating) "Stop" else "Start",
                        fontSize = 18.sp
                    )
                }
                Button(onClick = {
                    isPlayingMusic = !isPlayingMusic
                    if (isPlayingMusic) mediaPlayer.value?.start() else mediaPlayer.value?.pause()
                }) {
                    Text(
                        text = if (isPlayingMusic) "Musik pausieren" else "Musik abspielen",
                        fontSize = 18.sp
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("TTS Anleitung", fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                Switch(checked = useTTS, onCheckedChange = { useTTS = it })
            }
        }
    }
}
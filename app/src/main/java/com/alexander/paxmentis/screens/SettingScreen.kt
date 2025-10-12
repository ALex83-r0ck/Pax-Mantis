package com.alexander.paxmentis.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen() {
    var ttsSpeed by remember { mutableFloatStateOf(1.0f) }
    var ttsPitch by remember { mutableFloatStateOf(1.0f) }
    var musicEnabled by remember { mutableStateOf(true) }
    var musicVolume by remember { mutableFloatStateOf(0.5f) }
    var meditationDuration by remember { mutableIntStateOf(10) } // In Minuten
    var language by remember { mutableStateOf("Deutsch") }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Einstellungen", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        // TTS Geschwindigkeit
        Text("TTS Geschwindigkeit: ${ttsSpeed.toInt()}x", fontSize = 18.sp)
        Slider(
            value = ttsSpeed,
            onValueChange = { ttsSpeed = it },
            valueRange = 0.5f..2.0f,
            steps = 15
        )

        // TTS Tonlage
        Text("TTS Tonlage: ${ttsPitch.toInt()}x", fontSize = 18.sp)
        Slider(
            value = ttsPitch,
            onValueChange = { ttsPitch = it },
            valueRange = 0.5f..2.0f,
            steps = 15
        )

        // Musik aktiviert
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Musik aktiviert", fontSize = 18.sp)
            Switch(checked = musicEnabled, onCheckedChange = { musicEnabled = it })
        }

        // Musik Lautstärke
        Text("Musik Lautstärke: ${(musicVolume * 100).toInt()}%", fontSize = 18.sp)
        Slider(
            value = musicVolume,
            onValueChange = { musicVolume = it },
            valueRange = 0f..1f,
            steps = 10
        )

        // Meditationsdauer
        Text("Meditationsdauer: $meditationDuration Minuten", fontSize = 18.sp)
        Slider(
            value = meditationDuration.toFloat(),
            onValueChange = { meditationDuration = it.toInt() },
            valueRange = 5f..30f,
            steps = 25
        )

        // Sprache
        Row(
            modifier = Modifier.fillMaxWidth(), 
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Sprache", fontSize = 18.sp)
            Button(onClick = {
                language = if (language == "Deutsch") "English" else "Deutsch"
                // Hier könnte die App-Sprache dynamisch geändert werden (z. B. mit Locale)
            }) {
                Text(language)
            }
        }

        // Hinweis: Diese Werte müssen in den jeweiligen Screens angewendet werden
        // (z. B. über ViewModel oder SharedPreferences)
    }
}
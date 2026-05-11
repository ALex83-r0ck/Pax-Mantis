package com.alexander.paxmentis.screens

import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alexander.paxmentis.data.UserPreferences
import com.alexander.paxmentis.ui.viewmodels.PanicViewModel
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun PanicScreen(
    hasCallPermission: Boolean,
    userPreferences: UserPreferences,
    viewModel: PanicViewModel = hiltViewModel()
) {
    var step by remember { mutableIntStateOf(0) }
    var useTTS by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val emergencyContact by viewModel.emergencyContact.collectAsState()
    val emergencyNumber by viewModel.emergencyNumber.collectAsState()
    val speechRate by userPreferences.speechRateFlow.collectAsState()
    
    val tts = remember { mutableStateOf<TextToSpeech?>(null) }
    val steps = listOf(
        "Atme tief ein und zähle bis 4",
        "Halte den Atem für 4 Sekunden",
        "Atme langsam aus und zähle bis 8",
        "Fokussiere dich auf einen Punkt in deiner Umgebung",
        "Wiederhole die Atmung, bis du ruhiger bist"
    )

    DisposableEffect(Unit) {
        tts.value = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.value?.language = Locale.GERMAN
                tts.value?.setSpeechRate(speechRate)
            }
        }
        onDispose {
            tts.value?.stop()
            tts.value?.shutdown()
        }
    }

    LaunchedEffect(speechRate) {
        tts.value?.setSpeechRate(speechRate)
    }

    LaunchedEffect(step) {
        if (step < steps.size && useTTS) {
            tts.value?.speak(steps[step], TextToSpeech.QUEUE_FLUSH, null, null)
        }
        if (step < steps.size - 1) {
            delay(10000)
            step += 1
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Notfallhilfe",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            if (step < steps.size) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = steps[step],
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp),
                        lineHeight = 30.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                LinearProgressIndicator(
                    progress = { (step + 1).toFloat() / steps.size },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                Button(onClick = { step = (step + 1).coerceAtMost(steps.size - 1) }) {
                    Text("Nächster Schritt")
                }
            } else {
                Text(
                    text = "Gefühl der Panik noch da? \nHilfe ist nur einen Klick entfernt.",
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:$emergencyNumber")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("$emergencyContact anrufen", fontSize = 18.sp)
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:116117")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ärztlicher Bereitschaftsdienst", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = useTTS, onCheckedChange = { useTTS = it })
                Text("Sprachführung")
                Spacer(modifier = Modifier.width(16.dp))
                TextButton(onClick = { step = 0 }) {
                    Text("Neustart")
                }
            }
        }
    }
}

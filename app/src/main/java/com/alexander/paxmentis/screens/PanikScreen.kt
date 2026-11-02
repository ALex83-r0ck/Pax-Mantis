package com.alexander.paxmentis.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alexander.paxmentis.ui.viewmodels.PanicViewModel
import kotlinx.coroutines.delay
import java.util.Locale

private const val STEP_DURATION_MS = 10_000L

private val PANIC_STEPS = listOf(
    "Atme tief ein und zähle bis 4",
    "Halte den Atem für 4 Sekunden",
    "Atme langsam aus und zähle bis 8",
    "Fokussiere dich auf einen Punkt in deiner Umgebung",
    "Wiederhole die Atmung, bis du ruhiger bist"
)

@Composable
fun PanicScreen(
    viewModel: PanicViewModel = hiltViewModel()
) {
    // step == PANIC_STEPS.size bedeutet: Übung durchlaufen.
    var step by rememberSaveable { mutableIntStateOf(0) }
    var useTTS by rememberSaveable { mutableStateOf(true) }
    var ttsReady by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val emergencyContact by viewModel.emergencyContact.collectAsState()
    val emergencyNumber by viewModel.emergencyNumber.collectAsState()
    val isDone = step >= PANIC_STEPS.size

    val tts = remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(Unit) {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                engine?.language = Locale.GERMAN
                ttsReady = true
            }
        }
        tts.value = engine
        onDispose {
            engine?.stop()
            engine?.shutdown()
        }
    }

    // Sprechen erst, wenn die Sprachausgabe bereit ist – sonst ging bisher der erste Schritt verloren.
    LaunchedEffect(step, ttsReady, useTTS) {
        if (!useTTS) {
            tts.value?.stop()
        } else if (!isDone && ttsReady) {
            tts.value?.speak(PANIC_STEPS[step], TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    // Automatisch weiter – inklusive Übergang in den "Fertig"-Zustand.
    LaunchedEffect(step) {
        if (step < PANIC_STEPS.size) {
            delay(STEP_DURATION_MS)
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
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                text = "Notfallhilfe",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (!isDone) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = PANIC_STEPS[step],
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp),
                        lineHeight = 30.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                LinearProgressIndicator(
                    progress = { (step + 1).toFloat() / PANIC_STEPS.size },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(onClick = { step += 1 }) {
                    Text(if (step == PANIC_STEPS.size - 1) "Fertig" else "Nächster Schritt")
                }
            } else {
                Text(
                    text = "Gefühl der Panik noch da?\nDu musst da nicht allein durch.",
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(onClick = { step = 0 }) {
                    Text("Übung wiederholen")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Hilfe ist IMMER sichtbar – nicht erst nach Ende der Übung.
            EmergencyActions(
                context = context,
                contactName = emergencyContact,
                contactNumber = emergencyNumber
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = useTTS, onCheckedChange = { useTTS = it })
                Text("Sprachführung")
                if (!isDone) {
                    Spacer(modifier = Modifier.width(16.dp))
                    TextButton(onClick = { step = 0 }) {
                        Text("Neustart")
                    }
                }
            }
        }
    }
}

@Composable
private fun EmergencyActions(
    context: Context,
    contactName: String,
    contactNumber: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Sofort Hilfe holen", fontWeight = FontWeight.Bold, fontSize = 16.sp)

        if (contactNumber.isNotBlank()) {
            Button(
                onClick = { dial(context, contactNumber) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("${contactName.ifBlank { "Vertrauensperson" }} anrufen", fontSize = 18.sp)
            }
        } else {
            Text(
                "Noch keine Vertrauensperson hinterlegt. Du kannst sie in den Einstellungen eintragen.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.error
            )
        }

        OutlinedButton(
            onClick = { dial(context, "08001110111") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Telefonseelsorge 0800 111 0 111 (kostenlos, 24/7)", textAlign = TextAlign.Center)
        }

        OutlinedButton(
            onClick = { dial(context, "116117") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ärztlicher Bereitschaftsdienst 116117")
        }

        OutlinedButton(
            onClick = { dial(context, "112") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Notruf 112", fontWeight = FontWeight.Bold)
        }
    }
}

/** Öffnet die Wähl-App mit der Nummer. Braucht keine CALL_PHONE-Berechtigung. */
private fun dial(context: Context, number: String) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null))
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // z. B. Tablet ohne Telefonfunktion – Nummer wenigstens anzeigen.
        Toast.makeText(context, "Kein Telefon verfügbar. Nummer: $number", Toast.LENGTH_LONG).show()
    }
}

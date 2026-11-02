package com.alexander.paxmentis.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alexander.paxmentis.domain.PANIC_STEPS
import com.alexander.paxmentis.domain.isPanicDone
import com.alexander.paxmentis.domain.nextPanicStep
import com.alexander.paxmentis.ui.components.EmergencyHelp
import com.alexander.paxmentis.ui.tts.rememberTts
import com.alexander.paxmentis.ui.viewmodels.PanicViewModel
import kotlinx.coroutines.delay

private const val STEP_DURATION_MS = 10_000L

@Composable
fun PanicScreen(
    viewModel: PanicViewModel = hiltViewModel(),
    onOpenCrisisPlan: (() -> Unit)? = null
) {
    // step == PANIC_STEPS.size bedeutet: Übung durchlaufen.
    var step by rememberSaveable { mutableIntStateOf(0) }
    var useTTS by rememberSaveable { mutableStateOf(true) }
    val tts = rememberTts()
    val emergencyContact by viewModel.emergencyContact.collectAsState()
    val emergencyNumber by viewModel.emergencyNumber.collectAsState()
    val isDone = isPanicDone(step)

    LaunchedEffect(step, useTTS) {
        if (!useTTS) {
            tts.stop()
        } else if (!isDone) {
            tts.speak(PANIC_STEPS[step])
        }
    }

    // Automatisch weiter – inklusive Übergang in den "Fertig"-Zustand.
    LaunchedEffect(step) {
        if (!isPanicDone(step)) {
            delay(STEP_DURATION_MS)
            step = nextPanicStep(step)
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

                Button(onClick = { step = nextPanicStep(step) }) {
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
            EmergencyHelp(
                contactName = emergencyContact,
                contactNumber = emergencyNumber
            )

            if (onOpenCrisisPlan != null) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = onOpenCrisisPlan, modifier = Modifier.fillMaxWidth()) {
                    Text("Meinen Krisenplan öffnen")
                }
            }

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

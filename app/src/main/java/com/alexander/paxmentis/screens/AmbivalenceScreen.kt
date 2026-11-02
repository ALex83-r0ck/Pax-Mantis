package com.alexander.paxmentis.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun AmbivalenceScreen() {
    var isLocked by remember { mutableStateOf(true) }
    var showGroundingStep by remember { mutableStateOf(false) }
    var pros by remember { mutableStateOf("") }
    var cons by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f)
                    )
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Entscheidungs-Check",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        if (isLocked) {
            CrisisLockCard(
                showGrounding = showGroundingStep,
                onToggleGrounding = { showGroundingStep = !showGroundingStep },
                onUnlock = { isLocked = false }
            )
        } else {
            FaktencheckContent(
                pros = pros,
                onProsChange = { pros = it },
                cons = cons,
                onConsChange = { cons = it },
                onResetLock = { isLocked = true; showGroundingStep = false },
                onSave = {
                    // TODO: Logik zum Speichern der Pro/Contra-Liste implementieren
                }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun CrisisLockCard(
    showGrounding: Boolean,
    onToggleGrounding: () -> Unit,
    onUnlock: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "AKUTE AMBIVALENZ / NOTMODUS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Dein Nervensystem meldet aktuell 'Gefahr'. In diesem Zustand übernimmt das emotionale Gehirn (Amygdala) – rationale Entscheidungen sind jetzt biologisch blockiert.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Evidenzbasierter Mikro-Schritt zur Regulation statt harter Blockade
            OutlinedButton(
                onClick = onToggleGrounding,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text(if (showGrounding) "Erdungsübung verbergen" else "Kurze Erdung (3 Atemzüge)")
            }

            AnimatedVisibility(visible = showGrounding) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "1. Spüre deine Füße auf dem Boden.\n2. Atme 4 Sekunden tief ein, 6 Sekunden lang aus.\n3. Erlaube dem Drang, jetzt sofort handeln zu müssen, für 5 Minuten da zu sein, ohne ihm zu folgen.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Start
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Empfehlung: Gib dir 24 Stunden, bevor du endgültig handelst.",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onUnlock,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("Ich habe mich beruhigt & möchte analysieren")
            }
        }
    }
}

@Composable
private fun FaktencheckContent(
    pros: String,
    onProsChange: (String) -> Unit,
    cons: String,
    onConsChange: (String) -> Unit,
    onResetLock: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Lass uns die Fakten sortieren.",
            style = MaterialTheme.typography.bodyLarge,
            fontStyle = FontStyle.Italic
        )
        TextButton(onClick = onResetLock) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Sperre reaktivieren", style = MaterialTheme.typography.labelSmall)
        }
    }

    OutlinedTextField(
        value = pros,
        onValueChange = onProsChange,
        label = { Text("Was spricht DAFÜR? (Langfristiger Nutzen)") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 4
    )

    OutlinedTextField(
        value = cons,
        onValueChange = onConsChange,
        label = { Text("Was spricht DAGEGEN? (Realistische Risiken)") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 4
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Text(
            text = "Wert-Kompass: Dient diese Entscheidung deinem langfristigen Wachstum oder dient sie nur der kurzfristigen Schmerzlinderung (Vermeidung)?",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center
        )
    }

    Button(
        onClick = onSave,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Gedanken strukturiert festhalten")
    }
}
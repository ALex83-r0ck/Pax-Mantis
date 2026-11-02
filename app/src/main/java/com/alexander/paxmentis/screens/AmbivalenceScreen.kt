package com.alexander.paxmentis.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alexander.paxmentis.ui.viewmodels.AmbivalenceViewModel

@Composable
fun AmbivalenceScreen(
    viewModel: AmbivalenceViewModel = hiltViewModel()
) {
    var isLocked by rememberSaveable { mutableStateOf(true) }
    var decision by rememberSaveable { mutableStateOf("") }
    var pros by rememberSaveable { mutableStateOf("") }
    var cons by rememberSaveable { mutableStateOf("") }
    var saved by rememberSaveable { mutableStateOf(false) }
    var saveFailed by remember { mutableStateOf(false) }

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
            "Entscheidungs-Check",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        if (isLocked) {
            // CRISIS LOCK MODE
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "CRISIS-LOCK AKTIV",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Du bist gerade im Überlebensmodus. Dein Gehirn ist auf 'Gefahr' programmiert, nicht auf 'Logik'.",
                        textAlign = TextAlign.Center,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "WIR TREFFEN DIESE ENTSCHEIDUNG ERST IN 24 STUNDEN.",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { isLocked = false }) {
                        Text("Ich bin jetzt ruhig (Sperre lösen)")
                    }
                }
            }
        } else {
            // FAKTEN-CHECK MODE
            Text(
                "Lass uns die Fakten sortieren.",
                fontSize = 16.sp,
                fontStyle = FontStyle.Italic
            )

            OutlinedTextField(
                value = decision,
                onValueChange = { decision = it; saved = false },
                label = { Text("Worum geht es?") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = pros,
                onValueChange = { pros = it; saved = false },
                label = { Text("Was spricht DAFÜR? (Kurzfristig & Langfristig)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )

            OutlinedTextField(
                value = cons,
                onValueChange = { cons = it; saved = false },
                label = { Text("Was spricht DAGEGEN? (Konsequenzen)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Text(
                    "Frage dich: Würde ich diese Entscheidung auch treffen, wenn ich mich gerade sicher und geliebt fühlen würde?",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center
                )
            }
            
            Button(
                onClick = {
                    viewModel.saveDecisionCheck(decision, pros, cons) { ok ->
                        saved = ok
                        saveFailed = !ok
                    }
                },
                enabled = !saved && (decision.isNotBlank() || pros.isNotBlank() || cons.isNotBlank()),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Im Sorgenstuhl festhalten")
            }

            if (saved) {
                Text(
                    "Gespeichert – mit dem Hinweis, frühestens in 24 Stunden zu entscheiden. " +
                        "Du findest es im Sorgenstuhl.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (saveFailed) {
                Text(
                    "Konnte gerade nicht gespeichert werden. Deine Eingaben bleiben stehen – " +
                        "versuche es gleich noch einmal.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

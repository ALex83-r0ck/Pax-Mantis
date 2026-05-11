package com.alexander.paxmentis.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alexander.paxmentis.ui.viewmodels.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val nickname by viewModel.nickname.collectAsState()
    val emergencyContact by viewModel.emergencyContact.collectAsState()
    val emergencyNumber by viewModel.emergencyNumber.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("Einstellungen", fontSize = 32.sp, fontWeight = FontWeight.Bold)

        // Personalisierung
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Personalisierung", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                
                TextField(
                    value = nickname,
                    onValueChange = { viewModel.setNickname(it) },
                    label = { Text("Dein Name / Spitzname") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Notfallkontakt
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Notfallkontakt", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                
                TextField(
                    value = emergencyContact,
                    onValueChange = { viewModel.setEmergencyContact(it) },
                    label = { Text("Name der Vertrauensperson") },
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = emergencyNumber,
                    onValueChange = { viewModel.setEmergencyNumber(it) },
                    label = { Text("Telefonnummer") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // TTS Sprach-Anleitung
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text("Stimme verbessern", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    "Die Stimme klingt zu künstlich? Du kannst in deinen Android-Systemeinstellungen hochwertigere Stimmen aktivieren:",
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                InstructionStep("1. Öffne die Handy-Einstellungen.")
                InstructionStep("2. Suche nach 'Text-zu-Sprache'.")
                InstructionStep("3. Wähle 'Google Sprachausgabe' (Zahnrad).")
                InstructionStep("4. Tippe auf 'Sprachdaten installieren'.")
                InstructionStep("5. Wähle 'Deutsch' und lade ein großes Paket (z.B. 50MB+) herunter.")
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    "Danach klingt die App automatisch viel natürlicher.",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Text(
            "Version 1.2.0\nOffline-Modus aktiv\nVerschlüsselung aktiv",
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 16.dp),
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun InstructionStep(text: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("•", fontWeight = FontWeight.Bold, modifier = Modifier.width(16.dp))
        Text(text, fontSize = 13.sp)
    }
}

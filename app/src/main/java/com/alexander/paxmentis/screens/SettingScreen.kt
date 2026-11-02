package com.alexander.paxmentis.screens

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import com.alexander.paxmentis.ui.theme.ThemeMood
import com.alexander.paxmentis.ui.theme.colorSchemeFor
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
import androidx.compose.ui.platform.LocalContext
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
    val themeMood by viewModel.themeMood.collectAsState()
    val context = LocalContext.current
    val versionName = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
            .getOrNull() ?: "?"
    }

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

                Text("Farbstimmung", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ThemeMood.entries.forEach { mood ->
                        MoodSwatch(mood, selected = mood == themeMood, onClick = { viewModel.setThemeMood(mood) })
                    }
                }
                Text(
                    "Hell oder dunkel richtet sich nach deinem Handy.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Notfallkontakt
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Vertrauensperson", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Wird auf der Startseite, im Notfall-Screen und im Krisenplan direkt zum Anrufen angeboten.",
                    fontSize = 14.sp
                )
                
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
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
            "Version $versionName\nOffline-Modus aktiv\nVerschlüsselung aktiv",
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 16.dp),
            color = MaterialTheme.colorScheme.outline
        )
    }
}

/** Farbkreis einer Stimmung: links helle, rechts dunkle Variante – so sieht man beide Modi. */
@Composable
private fun MoodSwatch(mood: ThemeMood, selected: Boolean, onClick: () -> Unit) {
    val light = colorSchemeFor(mood, dark = false)
    val dark = colorSchemeFor(mood, dark = true)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics { this.selected = selected }
            .padding(6.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .border(
                    width = if (selected) 3.dp else 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                )
        ) {
            Row(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f).fillMaxHeight().background(light.primary))
                Box(Modifier.weight(1f).fillMaxHeight().background(dark.primary))
            }
            if (selected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = light.onPrimary)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(mood.label, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun InstructionStep(text: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("•", fontWeight = FontWeight.Bold, modifier = Modifier.width(16.dp))
        Text(text, fontSize = 13.sp)
    }
}

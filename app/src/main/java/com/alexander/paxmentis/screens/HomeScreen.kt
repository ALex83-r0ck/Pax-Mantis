package com.alexander.paxmentis.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.alexander.paxmentis.ui.viewmodels.HomeViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val nickname by viewModel.nickname.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = if (nickname.isBlank()) "Hallo ♥️" else "Hallo $nickname ♥️",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Deine Werkzeuge für Stabilität.",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.secondary,
            fontStyle = FontStyle.Italic
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        // Krisen-Kompass (Highlight)
        Card(
            onClick = { navController.navigate("crisis_compass") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("KRISEN-KOMPASS", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Text("Soforthilfe bei Panik & Angst", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Hauptmodule Grid
        Text("Alle Module", fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(16.dp))

        // Row 1: Kommunikation & Präsenz
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionButton(
                label = "Neutral-Msg",
                description = "Sachlich bleiben",
                icon = Icons.AutoMirrored.Filled.Chat,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.secondaryContainer,
                onClick = { navController.navigate("neutral_messaging") }
            )
            QuickActionButton(
                label = "Shared Reality",
                description = "Präsenz zeigen",
                icon = Icons.Default.Security,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.primaryContainer,
                onClick = { navController.navigate("shared_reality") }
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))

        // Row 2: Entspannung & Fokus
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionButton(
                label = "Atem-Übung",
                description = "Nervensystem beruhigen",
                icon = Icons.Default.Air,
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate("breathing") }
            )
            QuickActionButton(
                label = "Sorgenstuhl",
                description = "Gedanken ablegen",
                icon = Icons.AutoMirrored.Filled.EventNote,
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate("worry_chair") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row 3: Schlaf & System
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionButton(
                label = "Schlafhilfe",
                description = "Sanft zur Ruhe kommen",
                icon = Icons.Default.NightsStay,
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate("sleep_aid") }
            )
            QuickActionButton(
                label = "Einstellungen",
                description = "Profil & Notfallkontakt",
                icon = Icons.Default.Settings,
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate("settings") }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Footer Button
        OutlinedButton(
            onClick = {
                viewModel.addQuickEntry { saved ->
                    val message = if (saved) "Vorfall im Sorgenstuhl vermerkt."
                    else "Konnte gerade nicht im Sorgenstuhl vermerkt werden."
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Einen Vorfall melden", fontSize = 16.sp)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun QuickActionButton(
    label: String,
    description: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(110.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center)
            Text(description, fontSize = 10.sp, textAlign = TextAlign.Center, lineHeight = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        }
    }
}

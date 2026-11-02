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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.alexander.paxmentis.ui.components.TELEFONSEELSORGE_CHAT_URL
import com.alexander.paxmentis.ui.components.TELEFONSEELSORGE_NUMBER
import com.alexander.paxmentis.ui.components.dial
import com.alexander.paxmentis.ui.components.openUrl
import com.alexander.paxmentis.ui.viewmodels.HomeViewModel

/**
 * Startseite: Wer in der Krise ankommt, soll ohne Scrollen und mit einem Tipp Hilfe erreichen.
 * Deshalb steht direkte Hilfe ganz oben, Werkzeuge darunter. Rot ist allein dem Notruf 112 vorbehalten.
 */
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val nickname by viewModel.nickname.collectAsState()
    val contactName by viewModel.emergencyContact.collectAsState()
    val contactNumber by viewModel.emergencyNumber.collectAsState()
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
            .padding(16.dp)
    ) {
        Text(
            text = if (nickname.isBlank()) "Hallo ♥️" else "Hallo $nickname ♥️",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Schön, dass du da bist.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Direkte Hilfe
        Card(
            onClick = { navController.navigate("crisis_compass") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(44.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Mir geht es gerade nicht gut", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Der Krisen-Kompass zeigt dir, was jetzt hilft", fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (contactNumber.isNotBlank()) {
                HelpButton(
                    label = "${contactName.ifBlank { "Vertrauensperson" }} anrufen",
                    icon = Icons.Default.Call,
                    modifier = Modifier.weight(1f),
                    onClick = { dial(context, contactNumber) }
                )
            } else {
                HelpButton(
                    label = "Vertrauensperson eintragen",
                    icon = Icons.Default.PersonAdd,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("settings") }
                )
            }
            HelpButton(
                label = "Mein Krisenplan",
                icon = Icons.Default.Checklist,
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate("crisis_plan") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HelpButton(
                label = "Telefonseelsorge\n0800 111 0 111",
                icon = Icons.Default.SupportAgent,
                modifier = Modifier.weight(1f),
                onClick = { dial(context, TELEFONSEELSORGE_NUMBER) }
            )
            HelpButton(
                label = "Lieber schreiben\n(Chat)",
                icon = Icons.AutoMirrored.Filled.Chat,
                modifier = Modifier.weight(1f),
                onClick = { openUrl(context, TELEFONSEELSORGE_CHAT_URL) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { dial(context, "112") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Lebensgefahr? Notruf 112", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(28.dp))

        SectionTitle("Zur Ruhe kommen")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionButton(
                label = "Atem-Übung",
                description = "Nervensystem beruhigen",
                icon = Icons.Default.Air,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.secondaryContainer,
                onClick = { navController.navigate("breathing") }
            )
            QuickActionButton(
                label = "5-4-3-2-1",
                description = "Im Hier und Jetzt ankommen",
                icon = Icons.Default.Spa,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.secondaryContainer,
                onClick = { navController.navigate("grounding_54321") }
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        SectionTitle("Weitere Werkzeuge")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionButton(
                label = "Sorgenstuhl",
                description = "Gedanken ablegen",
                icon = Icons.AutoMirrored.Filled.EventNote,
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate("worry_chair") }
            )
            QuickActionButton(
                label = "Schlafhilfe",
                description = "Sanft zur Ruhe kommen",
                icon = Icons.Default.NightsStay,
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate("sleep_aid") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionButton(
                label = "Neutral-Msg",
                description = "Sachlich bleiben",
                icon = Icons.AutoMirrored.Filled.Chat,
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate("neutral_messaging") }
            )
            QuickActionButton(
                label = "Shared Reality",
                description = "Präsenz zeigen",
                icon = Icons.Default.Security,
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate("shared_reality") }
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

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
private fun SectionTitle(text: String) {
    Text(text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(12.dp))
}

/** Großer, gut treffbarer Knopf für direkte Hilfe (mind. 72 dp hoch). */
@Composable
private fun HelpButton(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.heightIn(min = 72.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 17.sp)
        }
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
            Text(description, fontSize = 12.sp, textAlign = TextAlign.Center, lineHeight = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
        }
    }
}

package com.alexander.paxmentis.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.alexander.paxmentis.Screen
import java.util.Calendar

@Composable
fun CrisisCompassScreen(navController: NavController) {
    // Dynamische Prüfung auf Nachtzeit (22:00 bis 06:00 Uhr)
    val isNight = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        hour >= 22 || hour < 6
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    )
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Krisen-Kompass",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Was fühlst du gerade?",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.secondary
            )
        }

        // Dynamischer Nachtwache-Eintrag (Fokus auf Einsamkeit & Gedankenkreisen)
        if (isNight) {
            CrisisOptionCard(
                title = "Nachtwache",
                description = "Es ist spät und die Gedanken kreisen.",
                icon = Icons.Default.Bedtime,
                color = Color(0xFF05082E),
                textColor = Color.White,
                onClick = { navController.navigate(Screen.NightWatch.route) }
            )
        }

        CrisisOptionCard(
            title = "Akute Panik / Angst",
            description = "Ich habe Todesangst oder mein Herz rast.",
            icon = Icons.Default.Warning,
            color = MaterialTheme.colorScheme.errorContainer,
            onClick = { navController.navigate(Screen.Panic.route) }
        )

        CrisisOptionCard(
            title = "Impuls / Schwere Wahl",
            description = "Ich will gerade eine radikale Entscheidung treffen.",
            icon = Icons.Default.Lock,
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
            onClick = { navController.navigate(Screen.Ambivalence.route) }
        )

        CrisisOptionCard(
            title = "Dissoziation / Flashbacks",
            description = "Ich fühle mich nicht real oder bin weggetreten.",
            icon = Icons.Default.Visibility,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            onClick = { navController.navigate(Screen.Grounding54321.route) }
        )

        CrisisOptionCard(
            title = "Social Media Drang",
            description = "Ich will mich gerade im Internet verlieren.",
            icon = Icons.Default.Block,
            color = MaterialTheme.colorScheme.surfaceVariant,
            onClick = { navController.navigate(Screen.TriggerBlock.route) }
        )

        CrisisOptionCard(
            title = "Depression / Leere",
            description = "Ich bin am Boden zerstört oder antriebslos.",
            icon = Icons.Default.Cloud,
            color = MaterialTheme.colorScheme.secondaryContainer,
            onClick = { navController.navigate(Screen.DepressionHelp.route) }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun CrisisOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    textColor: Color = Color.Unspecified,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color,
            contentColor = if (textColor != Color.Unspecified) textColor else LocalContentColor.current
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tintColor = if (textColor != Color.Unspecified) textColor else LocalContentColor.current
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = tintColor
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f) // Verhindert Text-Overflow bei langen Beschreibungen
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (textColor != Color.Unspecified) textColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
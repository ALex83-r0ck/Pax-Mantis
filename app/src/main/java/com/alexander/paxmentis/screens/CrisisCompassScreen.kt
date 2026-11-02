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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.alexander.paxmentis.domain.isNightTime
import java.util.Calendar

@Composable
fun CrisisCompassScreen(navController: NavController) {
    val isNight = isNightTime(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))

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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Krisen-Kompass",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Was fühlst du gerade?",
            fontSize = 18.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
            color = MaterialTheme.colorScheme.secondary
        )

        // Ganz oben: Suizidgedanken offen benennen. Direkt danach zu fragen erhöht das Risiko nicht.
        CrisisOptionCard(
            title = "Suizidgedanken",
            description = "Ich denke daran, mir etwas anzutun.",
            icon = Icons.Default.Favorite,
            color = MaterialTheme.colorScheme.primary,
            onClick = { navController.navigate("crisis_plan") }
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (isNight) {
            CrisisOptionCard(
                title = "Nachtwache",
                description = "Es ist spät und die Gedanken kreisen.",
                icon = Icons.Default.Bedtime,
                color = Color(0xFF05082E),
                textColor = Color.White,
                onClick = { navController.navigate("night_watch") }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        CrisisOptionCard(
            title = "Akute Panik / Angst",
            description = "Ich habe Todesangst oder mein Herz rast.",
            icon = Icons.Default.Warning,
            color = MaterialTheme.colorScheme.primaryContainer,
            onClick = { navController.navigate("panic") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        CrisisOptionCard(
            title = "Impuls / Schwere Wahl",
            description = "Ich will gerade eine radikale Entscheidung treffen.",
            icon = Icons.Default.Lock,
            color = MaterialTheme.colorScheme.secondaryContainer,
            onClick = { navController.navigate("ambivalence") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        CrisisOptionCard(
            title = "Dissoziation / Flashbacks",
            description = "Ich fühle mich nicht real oder bin weggetreten.",
            icon = Icons.Default.Visibility,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            onClick = { navController.navigate("grounding_54321") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        CrisisOptionCard(
            title = "Gedankenkreisen",
            description = "Mein Kopf hört nicht auf zu grübeln.",
            icon = Icons.Default.Category,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            onClick = { navController.navigate("find_shapes") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        CrisisOptionCard(
            title = "Überforderung / Unsicherheit",
            description = "Ich brauche einen Ort, an dem ich sicher bin.",
            icon = Icons.Default.Home,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            onClick = { navController.navigate("safe_space") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        CrisisOptionCard(
            title = "Social Media Drang",
            description = "Ich will mich gerade im Internet verlieren.",
            icon = Icons.Default.Block,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            onClick = { navController.navigate("trigger_block") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        CrisisOptionCard(
            title = "Depression / Leere",
            description = "Ich bin am Boden zerstört oder antriebslos.",
            icon = Icons.Default.Cloud,
            color = MaterialTheme.colorScheme.secondaryContainer,
            onClick = { navController.navigate("depression_help") }
        )
        
        Spacer(modifier = Modifier.height(40.dp))
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
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tintColor = if (textColor != Color.Unspecified) textColor else LocalContentColor.current
            Icon(icon, contentDescription = null, modifier = Modifier.size(40.dp), tint = tintColor)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor)
                Text(description, fontSize = 14.sp, color = if (textColor != Color.Unspecified) textColor.copy(alpha = 0.8f) else Color.Unspecified)
            }
        }
    }
}

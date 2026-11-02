package com.alexander.paxmentis.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TriggerBlockScreen(
    onStartBreathing: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)), // Sehr dunkel
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Icon(
                Icons.Default.Block,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.error
            )

            Text(
                "STOPP!",
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                "Willst du gerade wirklich auf Social Media scrollen?",
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                color = Color.White.copy(alpha = 0.8f)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Das endlose Scrollen (Doomscrolling) wird deine Unruhe gerade nur verstärken. Es triggert Vergleiche und Angst.",
                    modifier = Modifier.padding(16.dp),
                    color = Color.White,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }

            Text(
                "Lege das Handy für 5 Minuten weg. Atme. Geh zum Sorgenstuhl.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onStartBreathing,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Okay, ich mache eine Atemübung")
            }
            
            // Autonomie respektieren: kein Bevormunden, einfach zurück.
            TextButton(onClick = onDismiss) {
                Text("Ich weiß was ich tue (Trotzdem weiter)", color = Color.White.copy(alpha = 0.4f))
            }
        }
    }
}

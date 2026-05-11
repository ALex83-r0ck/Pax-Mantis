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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DepressionHelpScreen() {
    val tasks = listOf(
        "Trinke ein Glas Wasser.",
        "Mache dein Bett (auch wenn du gleich wieder reinschlüpfst).",
        "Wasche dir das Gesicht mit kaltem Wasser.",
        "Ziehe dir frische Socken an.",
        "Öffne für 2 Minuten das Fenster.",
        "Schreibe eine Sache auf, die heute okay war."
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Sanfte Hilfe",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            "Alles ist gerade schwer. Das ist okay. Lass uns nur eine kleine Sache tun.",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        tasks.forEach { task ->
            SmallStepCard(task)
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(
                "Du musst heute nicht die Welt retten. Es reicht, wenn du einfach nur da bist. Wir sind stolz auf dich.",
                modifier = Modifier.padding(16.dp),
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SmallStepCard(text: String) {
    var isDone by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDone) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) 
                            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = isDone, onCheckedChange = { isDone = it })
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                fontSize = 16.sp,
                textDecoration = if (isDone) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                color = if (isDone) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

package com.alexander.paxmentis.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.hilt.navigation.compose.hiltViewModel
import com.alexander.paxmentis.ui.viewmodels.PanicViewModel

@Composable
fun SharedRealityScreen(
    viewModel: PanicViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // Sicherer Zugriff auf die Schriftgröße mit Fallback
    val bodyLargeStyle = MaterialTheme.typography.bodyLarge
    val bodyMediumStyle = MaterialTheme.typography.bodyMedium

    val emergencyNumber by viewModel.emergencyNumber.collectAsState()
    val emergencyContact by viewModel.emergencyContact.collectAsState()
    var showSuccess by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Shared Reality",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        // Zeilenhöhe sicher berechnen
        val bodyLargeLineHeight = if (bodyLargeStyle.lineHeight.isSpecified) {
            bodyLargeStyle.lineHeight * 1.2f
        } else {
            androidx.compose.ui.unit.TextUnit.Unspecified
        }

        Text(
            text = "Präsenz signalisieren, ohne zu reden. Kurze Statusmeldung an $emergencyContact senden.",
            style = bodyLargeStyle,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 40.dp),
            color = MaterialTheme.colorScheme.secondary,
            lineHeight = bodyLargeLineHeight
        )

        Button(
            onClick = {
                val message = "Alexander hier. Ich halte die Stellung. Ich bin präsent und sorge für Sicherheit."
                sendWhatsApp(context, emergencyNumber, message)
                showSuccess = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Ich halte die Stellung",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (showSuccess) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Meldung wurde vorbereitet / gesendet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                TextButton(onClick = { showSuccess = false }) {
                    Text(
                        text = "OK",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = MaterialTheme.shapes.large
        ) {
            val bodyMediumLineHeight = if (bodyMediumStyle.lineHeight.isSpecified) {
                bodyMediumStyle.lineHeight * 1.2f
            } else {
                androidx.compose.ui.unit.TextUnit.Unspecified
            }

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Was das bewirkt:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Es signalisiert Stabilität und Präsenz, ohne eine direkte Unterhaltung führen zu müssen. Das nimmt den Druck aus der Situation.",
                    style = bodyMediumStyle,
                    fontStyle = FontStyle.Italic,
                    lineHeight = bodyMediumLineHeight
                )
            }
        }
    }
}

fun sendWhatsApp(context: Context, number: String, message: String) {
    try {
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$number&text=${Uri.encode(message)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback: SMS oder Share
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Status senden via..."))
    }
}
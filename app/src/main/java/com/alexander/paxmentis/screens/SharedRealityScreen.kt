package com.alexander.paxmentis.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alexander.paxmentis.ui.viewmodels.PanicViewModel

@Composable
fun SharedRealityScreen(
    viewModel: PanicViewModel = hiltViewModel()
) {
    val context = LocalContext.current
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
            Icons.Default.Security,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Shared Reality",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Text(
            "Präsenz signalisieren, ohne zu reden. Kurze Statusmeldung an $emergencyContact senden.",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 48.dp),
            color = MaterialTheme.colorScheme.secondary
        )

        if (emergencyNumber.isBlank()) {
            Text(
                "Noch keine Vertrauensperson hinterlegt. Trag sie zuerst in den Einstellungen ein.",
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Button(
            enabled = emergencyNumber.isNotBlank(),
            onClick = {
                val message = "Alexander hier. Ich halte die Stellung. Ich bin präsent und sorge für Sicherheit."
                sendWhatsApp(context, emergencyNumber, message)
                showSuccess = true
            },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text("Ich halte die Stellung", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (showSuccess) {
            Text(
                "Meldung wurde vorbereitet / gesendet.",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            TextButton(onClick = { showSuccess = false }) {
                Text("OK")
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Was das bewirkt:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    "Es signalisiert Stabilität und Präsenz, ohne eine direkte Unterhaltung führen zu müssen. Das nimmt den Druck aus der Situation.",
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic
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

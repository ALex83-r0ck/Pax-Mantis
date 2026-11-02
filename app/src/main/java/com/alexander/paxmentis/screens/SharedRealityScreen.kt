package com.alexander.paxmentis.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import com.alexander.paxmentis.domain.normalizeWhatsAppNumber
import com.alexander.paxmentis.ui.viewmodels.PanicViewModel

@Composable
fun SharedRealityScreen(
    viewModel: PanicViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val emergencyNumber by viewModel.emergencyNumber.collectAsState()
    val emergencyContact by viewModel.emergencyContact.collectAsState()
    val nickname by viewModel.nickname.collectAsState()
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
            "Präsenz signalisieren, ohne zu reden. Kurze Statusmeldung an ${emergencyContact.ifBlank { "deine Vertrauensperson" }} senden.",
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
                showSuccess = sendWhatsApp(context, emergencyNumber, statusMessage(nickname))
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

        OutlinedButton(
            enabled = emergencyNumber.isNotBlank(),
            onClick = { showSuccess = sendSms(context, emergencyNumber, statusMessage(nickname)) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Stattdessen per SMS")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (showSuccess) {
            Text(
                "Nachricht ist vorbereitet – bitte in der geöffneten App auf Senden tippen.",
                textAlign = TextAlign.Center,
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

private fun statusMessage(nickname: String): String {
    val sender = nickname.ifBlank { null }?.let { "$it hier. " } ?: ""
    return "${sender}Ich halte die Stellung. Ich bin präsent und sorge für Sicherheit."
}

/**
 * Öffnet WhatsApp mit vorausgefülltem Text. Ist WhatsApp nicht installiert, öffnet der
 * Link die WhatsApp-Webseite im Browser – deshalb gibt es zusätzlich den SMS-Weg.
 * @return true, wenn eine App geöffnet wurde.
 */
private fun sendWhatsApp(context: Context, number: String, message: String): Boolean {
    val phone = normalizeWhatsAppNumber(number)
    val uri = Uri.parse("https://wa.me/$phone?text=${Uri.encode(message)}")
    return startSafely(context, Intent(Intent.ACTION_VIEW, uri))
}

private fun sendSms(context: Context, number: String, message: String): Boolean {
    val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("smsto", number, null)).apply {
        putExtra("sms_body", message)
    }
    return startSafely(context, intent)
}

private fun startSafely(context: Context, intent: Intent): Boolean = try {
    context.startActivity(intent)
    true
} catch (e: ActivityNotFoundException) {
    Toast.makeText(context, "Keine passende App gefunden.", Toast.LENGTH_LONG).show()
    false
}

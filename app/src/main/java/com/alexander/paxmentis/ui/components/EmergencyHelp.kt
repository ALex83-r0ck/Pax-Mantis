package com.alexander.paxmentis.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val TELEFONSEELSORGE_CHAT_URL = "https://online.telefonseelsorge.de"

/**
 * Notruf-Optionen, die auf Notfall-Screens IMMER sichtbar sind – nicht erst nach einer Übung.
 *
 * @param showAllLines zeigt zusätzlich die weiteren Nummern der Telefonseelsorge (für den Krisenplan).
 */
@Composable
fun EmergencyHelp(
    contactName: String,
    contactNumber: String,
    modifier: Modifier = Modifier,
    showAllLines: Boolean = false
) {
    val context = LocalContext.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Sofort Hilfe holen", fontWeight = FontWeight.Bold, fontSize = 16.sp)

        if (contactNumber.isNotBlank()) {
            Button(
                onClick = { dial(context, contactNumber) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("${contactName.ifBlank { "Vertrauensperson" }} anrufen", fontSize = 18.sp)
            }
        } else {
            Text(
                "Noch keine Vertrauensperson hinterlegt. Du kannst sie in den Einstellungen eintragen.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.error
            )
        }

        OutlinedButton(
            onClick = { dial(context, "08001110111") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Telefonseelsorge 0800 111 0 111 (kostenlos, 24/7)", textAlign = TextAlign.Center)
        }

        if (showAllLines) {
            OutlinedButton(
                onClick = { dial(context, "08001110222") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Telefonseelsorge 0800 111 0 222 (kostenlos, 24/7)", textAlign = TextAlign.Center)
            }
            OutlinedButton(
                onClick = { dial(context, "116123") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Telefonseelsorge 116 123 (kostenlos, 24/7)", textAlign = TextAlign.Center)
            }
        }

        // Für alle, die gerade nicht sprechen können.
        TextButton(
            onClick = { openUrl(context, TELEFONSEELSORGE_CHAT_URL) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Lieber schreiben? Chat der Telefonseelsorge", textAlign = TextAlign.Center)
        }

        OutlinedButton(
            onClick = { dial(context, "116117") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ärztlicher Bereitschaftsdienst 116117")
        }

        OutlinedButton(
            onClick = { dial(context, "112") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Notruf 112", fontWeight = FontWeight.Bold)
        }
    }
}

/** Öffnet die Wähl-App mit der Nummer. Braucht keine CALL_PHONE-Berechtigung. */
fun dial(context: Context, number: String) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null))
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // z. B. Tablet ohne Telefonfunktion – Nummer wenigstens anzeigen.
        Toast.makeText(context, "Kein Telefon verfügbar. Nummer: $number", Toast.LENGTH_LONG).show()
    }
}

private fun openUrl(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "Kein Browser verfügbar. Adresse: $url", Toast.LENGTH_LONG).show()
    }
}

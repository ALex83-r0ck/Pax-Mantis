package com.alexander.paxmentis.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NeutralMessagingScreen() {
    var rawInput by remember { mutableStateOf("") }
    var neutralOutput by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val templates = mapOf(
        "Wut/Vorwurf" to "Ich habe die Situation wahrgenommen und konzentriere mich aktuell auf meine Ruhe.",
        "Rechtfertigung" to "Ich respektiere deine Sichtweise, bleibe aber bei meiner aktuellen Entscheidung zum Schutz meiner Stabilität.",
        "Abgrenzung" to "Ich benötige gerade Zeit für mich und werde mich melden, wenn ich bereit für ein sachliches Gespräch bin.",
        "Trigger-Reaktion" to "Diese Information löst Unruhe aus. Ich nehme sie zur Kenntnis, reagiere aber erst später darauf."
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
                    )
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Neutral-Messaging",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Nimm die Schärfe aus der Nachricht. Bleib sachlich, bleib stoisch.",
            fontSize = 15.sp,
            fontStyle = FontStyle.Italic,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        OutlinedTextField(
            value = rawInput,
            onValueChange = { rawInput = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Was willst du eigentlich sagen? (Emotional)") },
            placeholder = { Text("Z.B. Ich bin total sauer wegen...") },
            minLines = 3
        )

        Button(
            onClick = {
                neutralOutput = when {
                    rawInput.contains("sauer", true) || rawInput.contains("wütend", true) -> templates["Wut/Vorwurf"]!!
                    rawInput.contains("warum", true) || rawInput.contains("aber", true) -> templates["Rechtfertigung"]!!
                    rawInput.contains("lass mich", true) || rawInput.contains("ruhe", true) -> templates["Abgrenzung"]!!
                    else -> "Ich nehme das zur Kenntnis und werde zu einem geeigneten Zeitpunkt sachlich darauf zurückkommen."
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Sync, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("In sachliche Form bringen")
        }

        if (neutralOutput.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Vorschlag:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(neutralOutput, fontSize = 16.sp)
                    Spacer(Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { 
                            clipboardManager.setText(AnnotatedString(neutralOutput))
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Kopieren")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, neutralOutput)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, null)
                            context.startActivity(shareIntent)
                        }) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Senden")
                        }
                    }
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        Text("Schnell-Bausteine", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        
        templates.forEach { (label, text) ->
            OutlinedButton(
                onClick = { neutralOutput = text },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(12.dp)
            ) {
                Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                    Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text, fontSize = 12.sp, fontStyle = FontStyle.Italic)
                }
            }
        }
    }
}

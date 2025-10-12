package com.alexander.paxmentis.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexander.paxmentis.data.PaxMentisDatabase
import com.alexander.paxmentis.data.WorryEntry
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WorryChairScreen() {
    var worry by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var worstCase by remember { mutableStateOf("") }
    var solution by remember { mutableStateOf("") }
    var entries by remember { mutableStateOf<List<WorryEntry>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var editEntry by remember { mutableStateOf<WorryEntry?>(null) }
    var editWorry by remember { mutableStateOf("") }
    var editReason by remember { mutableStateOf("") }
    var editWorstCase by remember { mutableStateOf("") }
    var editSolution by remember { mutableStateOf("") }

    // Nutze die Singleton-Instanz der Datenbank
    val db = PaxMentisDatabase.getInstance(context)

    // Lade Einträge aus der Datenbank
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            db.worryEntryDao().getAll().collectLatest { entries = it }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Titel
        item {
            Text(
                text = "Sorgenstuhl",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        // Eingabefeld: Sorge
        item {
            Text(text = "Was raubt Dir deinen Schlaf?", fontSize = 18.sp)
            TextField(
                value = worry,
                onValueChange = { worry = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium),
                placeholder = { Text("Deine Sorge eingeben") }
            )
        }
        // Eingabefeld: Grund
        item {
            Text(text = "Was denkst du warum Du diese Sorge/Angst hast?", fontSize = 18.sp)
            TextField(
                value = reason,
                onValueChange = { reason = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium),
                placeholder = { Text("Grund eingeben") }
            )
        }
        // Eingabefeld: Schlimmstes Szenario
        item {
            Text(text = "Was meinst du was das schlimmste ist was passieren kann?", fontSize = 18.sp)
            TextField(
                value = worstCase,
                onValueChange = { worstCase = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium),
                placeholder = { Text("Schlimmstes Szenario eingeben") }
            )
        }
        // Eingabefeld: Lösung
        item {
            Text(text = "Was wäre eine mögliche Antwort darauf? Was kannst Du dagegen aktiv tun?", fontSize = 18.sp)
            TextField(
                value = solution,
                onValueChange = { solution = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium),
                placeholder = { Text("Lösung eingeben") }
            )
        }
        // Speichern-Button
        item {
            Button(
                onClick = {
                    coroutineScope.launch {
                        if (worry.isNotBlank()) {
                            try {
                                val newEntry = WorryEntry(
                                    date = System.currentTimeMillis(),
                                    worry = worry,
                                    reason = reason.ifEmpty { null },
                                    worstCase = worstCase.ifEmpty { null },
                                    solution = solution.ifEmpty { null }
                                )
                                db.worryEntryDao().insert(newEntry)
                                worry = ""
                                reason = ""
                                worstCase = ""
                                solution = ""
                                Toast.makeText(context, "Eintrag gespeichert!", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Fehler beim Speichern: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Bitte gib eine Sorge ein!", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Speichern", fontSize = 18.sp)
            }
        }
        // Historie-Überschrift
        item {
            Text(
                text = "Deine kleine Geschichte",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
        }
        // Historie-Einträge mit CRUD
        items(entries) { entry ->
            WorryEntryCard(entry, db) { selectedEntry ->
                editEntry = selectedEntry
                editWorry = selectedEntry.worry!!
                editReason = selectedEntry.reason ?: ""
                editWorstCase = selectedEntry.worstCase ?: ""
                editSolution = selectedEntry.solution ?: ""
            }
        }
    }

    // Bearbeitungs-Dialog
    if (editEntry != null) {
        AlertDialog(
            onDismissRequest = { editEntry = null },
            title = { Text("Eintrag bearbeiten") },
            text = {
                Column {
                    TextField(
                        value = editWorry,
                        onValueChange = { editWorry = it },
                        label = { Text("Sorge") }
                    )
                    TextField(
                        value = editReason,
                        onValueChange = { editReason = it },
                        label = { Text("Grund") }
                    )
                    TextField(
                        value = editWorstCase,
                        onValueChange = { editWorstCase = it },
                        label = { Text("Schlimmstes Szenario") }
                    )
                    TextField(
                        value = editSolution,
                        onValueChange = { editSolution = it },
                        label = { Text("Lösung") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    coroutineScope.launch {
                        editEntry?.let { entry ->
                            val updatedEntry = entry.copy(
                                worry = editWorry,
                                reason = editReason.ifEmpty { null },
                                worstCase = editWorstCase.ifEmpty { null },
                                solution = editSolution.ifEmpty { null }
                            )
                            db.worryEntryDao().update(updatedEntry)
                            editEntry = null
                            Toast.makeText(context, "Eintrag aktualisiert!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    Text("Speichern")
                }
            },
            dismissButton = {
                Button(onClick = { editEntry = null }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

@Composable
fun WorryEntryCard(
    entry: WorryEntry,
    db: PaxMentisDatabase,
    onEdit: (WorryEntry) -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(entry.date))
    val localScope = rememberCoroutineScope() // Lokaler Coroutine-Scope
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = "Datum: $formattedDate", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(text = "Sorge: ${entry.worry}", fontSize = 16.sp)
            Text(text = "Grund: ${entry.reason}", fontSize = 16.sp)
            Text(text = "Schlimmstes Szenario: ${entry.worstCase}", fontSize = 16.sp)
            Text(text = "Lösung: ${entry.solution}", fontSize = 16.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = {
                    localScope.launch {
                        db.worryEntryDao().delete(entry)
                    }
                }) {
                    Text("Löschen")
                }
                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                Button(onClick = { onEdit(entry) }) {
                    Text("Bearbeiten")
                }
            }
        }
    }
}


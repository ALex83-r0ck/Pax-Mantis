package com.alexander.paxmentis.screens

import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alexander.paxmentis.data.UserPreferences
import com.alexander.paxmentis.data.WorryEntry
import com.alexander.paxmentis.ui.viewmodels.WorryChairViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WorryChairScreen(
    userPreferences: UserPreferences,
    viewModel: WorryChairViewModel = hiltViewModel()
) {
    var worry by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var worstCase by remember { mutableStateOf("") }
    var solution by remember { mutableStateOf("") }
    
    val entries by viewModel.entries.collectAsState()
    val nickname by viewModel.nickname.collectAsState()
    val context = LocalContext.current
    
    var editEntry by remember { mutableStateOf<WorryEntry?>(null) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isRecordingMode by remember { mutableStateOf(false) }
    val speechRate by userPreferences.speechRateFlow.collectAsState()

    DisposableEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.GERMAN
                tts?.setPitch(0.9f)
            }
        }
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    LaunchedEffect(speechRate) {
        tts?.setSpeechRate(speechRate * 0.8f)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Dein Sorgenstuhl",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Ein Ort, um Gedanken abzuladen und Klarheit zu finden.",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                ) {
                    Text(
                        "Schreibe auf, was dich belastet. Wir schauen es uns gemeinsam an, Schritt für Schritt.",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic
                    )
                }
            }

            item {
                WorryInputSection(
                    label = "Was belastet dich gerade?",
                    value = worry,
                    onValueChange = { worry = it },
                    placeholder = "Lass es hier raus...",
                    onHelpClick = { tts?.speak("Atme tief durch. Was genau macht dir gerade Sorgen?", TextToSpeech.QUEUE_FLUSH, null, null) }
                )
            }

            item {
                WorryInputSection(
                    label = "Warum ist dieser Gedanke da?",
                    value = reason,
                    onValueChange = { reason = it },
                    placeholder = "Gibt es einen Auslöser?",
                    onHelpClick = { tts?.speak("Versuche den Ursprung zu finden. Gab es eine bestimmte Situation?", TextToSpeech.QUEUE_FLUSH, null, null) }
                )
            }

            item {
                WorryInputSection(
                    label = "Worst Case Szenario?",
                    value = worstCase,
                    onValueChange = { worstCase = it },
                    placeholder = "Was wäre das Schlimmste?",
                    onHelpClick = { tts?.speak("Oft ist das schlimmste Szenario weniger bedrohlich, wenn wir es aussprechen.", TextToSpeech.QUEUE_FLUSH, null, null) }
                )
            }

            item {
                WorryInputSection(
                    label = "Was würde dir jetzt helfen?",
                    value = solution,
                    onValueChange = { solution = it },
                    placeholder = "Eine kleine Lösung...",
                    onHelpClick = { tts?.speak("Denk an eine winzige Sache, die jetzt gut tun würde.", TextToSpeech.QUEUE_FLUSH, null, null) }
                )
            }

            item {
                Button(
                    onClick = {
                        if (worry.isNotBlank()) {
                            viewModel.addEntry(
                                worry = worry,
                                reason = reason.ifEmpty { null },
                                worstCase = worstCase.ifEmpty { null },
                                solution = solution.ifEmpty { null }
                            )
                            worry = ""
                            reason = ""
                            worstCase = ""
                            solution = ""
                            tts?.speak("Danke, dass du das geteilt hast. Du hast es hier abgelegt.", TextToSpeech.QUEUE_FLUSH, null, null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CloudDone, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Gedanken ablegen", fontSize = 18.sp)
                }
            }

            item {
                Text(
                    text = "Vergangene Gedanken",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            items(entries) { entry ->
                WorryEntryCard(
                    entry = entry,
                    onDelete = { viewModel.deleteEntry(it) },
                    onEdit = { editEntry = it }
                )
            }
        }
    }

    if (editEntry != null) {
        EditWorryDialog(
            entry = editEntry!!,
            onDismiss = { editEntry = null },
            onConfirm = { updated ->
                viewModel.updateEntry(updated)
                editEntry = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorryInputSection(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    onHelpClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            IconButton(onClick = onHelpClick) {
                Icon(Icons.Default.HelpOutline, contentDescription = "Hilfe", tint = MaterialTheme.colorScheme.primary)
            }
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder) },
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun WorryEntryCard(
    entry: WorryEntry,
    onDelete: (WorryEntry) -> Unit,
    onEdit: (WorryEntry) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd. MMMM yyyy", Locale.GERMAN) }
    val formattedDate = dateFormat.format(Date(entry.date))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = formattedDate, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                Row {
                    IconButton(onClick = { onEdit(entry) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = { onDelete(entry) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(text = entry.worry ?: "", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            if (!entry.solution.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(4.dp))
                    Text(text = entry.solution!!, fontSize = 14.sp, fontStyle = FontStyle.Italic)
                }
            }
        }
    }
}

@Composable
fun EditWorryDialog(
    entry: WorryEntry,
    onDismiss: () -> Unit,
    onConfirm: (WorryEntry) -> Unit
) {
    var worry by remember { mutableStateOf(entry.worry ?: "") }
    var reason by remember { mutableStateOf(entry.reason ?: "") }
    var worstCase by remember { mutableStateOf(entry.worstCase ?: "") }
    var solution by remember { mutableStateOf(entry.solution ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gedanken bearbeiten") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(value = worry, onValueChange = { worry = it }, label = { Text("Sorge") })
                OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Grund") })
                OutlinedTextField(value = worstCase, onValueChange = { worstCase = it }, label = { Text("Worst Case") })
                OutlinedTextField(value = solution, onValueChange = { solution = it }, label = { Text("Lösung") })
            }
        },
        confirmButton = {
            Button(onClick = { 
                onConfirm(entry.copy(worry = worry, reason = reason, worstCase = worstCase, solution = solution))
            }) { Text("Speichern") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        }
    )
}

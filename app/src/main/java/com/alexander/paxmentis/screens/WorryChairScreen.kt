package com.alexander.paxmentis.screens

import android.speech.tts.TextToSpeech
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
import androidx.compose.ui.unit.isSpecified
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
    val context = LocalContext.current

    var editEntry by remember { mutableStateOf<WorryEntry?>(null) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    val speechRate by userPreferences.speechRateFlow.collectAsState()

    DisposableEffect(context) {
        var textToSpeechInstance: TextToSpeech? = null
        textToSpeechInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeechInstance?.language = Locale.GERMAN
                textToSpeechInstance?.setPitch(0.9f)
            }
        }
        tts = textToSpeechInstance

        onDispose {
            textToSpeechInstance?.stop()
            textToSpeechInstance?.shutdown()
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
                .padding(24.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Dein Sorgenstuhl",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Ein Ort, um Gedanken abzuladen und Klarheit zu finden.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
                    shape = MaterialTheme.shapes.large
                ) {
                    val bodyMediumStyle = MaterialTheme.typography.bodyMedium
                    val safeLineHeight = if (bodyMediumStyle.lineHeight.isSpecified) {
                        bodyMediumStyle.lineHeight * 1.2f
                    } else {
                        androidx.compose.ui.unit.TextUnit.Unspecified
                    }

                    Text(
                        text = "Schreibe auf, was dich belastet. Wir schauen es uns gemeinsam an, Schritt für Schritt.",
                        modifier = Modifier.padding(20.dp),
                        style = bodyMediumStyle,
                        fontStyle = FontStyle.Italic,
                        lineHeight = safeLineHeight
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(imageVector = Icons.Default.CloudDone, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Gedanken ablegen",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            item {
                Text(
                    text = "Vergangene Gedanken",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
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
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = onHelpClick) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = "Hilfe",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder) },
            shape = MaterialTheme.shapes.medium,
            textStyle = MaterialTheme.typography.bodyLarge
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { onEdit(entry) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { onDelete(entry) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Text(
                text = entry.worry ?: "",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (!entry.solution.isNullOrBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = entry.solution!!,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.primary
                    )
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
        title = {
            Text(
                text = "Gedanken bearbeiten",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = worry,
                    onValueChange = { worry = it },
                    label = { Text("Sorge") },
                    shape = MaterialTheme.shapes.medium
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Grund") },
                    shape = MaterialTheme.shapes.medium
                )
                OutlinedTextField(
                    value = worstCase,
                    onValueChange = { worstCase = it },
                    label = { Text("Worst Case") },
                    shape = MaterialTheme.shapes.medium
                )
                OutlinedTextField(
                    value = solution,
                    onValueChange = { solution = it },
                    label = { Text("Lösung") },
                    shape = MaterialTheme.shapes.medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(entry.copy(worry = worry, reason = reason, worstCase = worstCase, solution = solution))
                },
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Speichern")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        },
        shape = MaterialTheme.shapes.extraLarge
    )
}
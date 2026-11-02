package com.alexander.paxmentis.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alexander.paxmentis.data.CrisisPlanItem
import com.alexander.paxmentis.domain.CrisisPlanSection
import com.alexander.paxmentis.domain.crisisSteps
import com.alexander.paxmentis.domain.isDialable
import com.alexander.paxmentis.ui.components.EmergencyHelp
import com.alexander.paxmentis.ui.components.dial
import com.alexander.paxmentis.ui.viewmodels.CrisisPlanViewModel

/**
 * Persönlicher Krisenplan. Bewusst NICHT hinter der Sperre: In einer suizidalen Krise darf
 * zwischen Mensch und Plan kein Fingerabdruck stehen. Verschlüsselt gespeichert ist er trotzdem.
 */
@Composable
fun CrisisPlanScreen(
    viewModel: CrisisPlanViewModel = hiltViewModel()
) {
    val plan by viewModel.plan.collectAsState()
    val unavailable by viewModel.unavailable.collectAsState()
    val emergencyContact by viewModel.emergencyContact.collectAsState()
    val emergencyNumber by viewModel.emergencyNumber.collectAsState()
    var editing by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                    )
                )
            )
            .imePadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Mein Krisenplan",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        if (unavailable) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Text(
                    "Dein Plan kann gerade nicht geladen werden. Er ist nicht verloren. " +
                        "Hol dir jetzt bitte direkt Hilfe – die Nummern stehen unten.",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        val current = plan
        when {
            editing && current != null -> CrisisPlanEditor(
                plan = current,
                onAdd = viewModel::addItem,
                onUpdate = viewModel::updateItem,
                onDelete = viewModel::deleteItem,
                onDone = { editing = false }
            )
            current != null -> CrisisPlanWalkthrough(plan = current, onEdit = { editing = true })
            // Lädt noch (oder Datenbank nicht verfügbar): Notrufe trotzdem sofort zeigen.
        }

        if (!editing) {
            HorizontalDivider()
            EmergencyHelp(
                contactName = emergencyContact,
                contactNumber = emergencyNumber,
                showAllLines = true
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// --- Krisenmodus: Schritt für Schritt ablesen ---------------------------------------------

@Composable
private fun CrisisPlanWalkthrough(
    plan: Map<CrisisPlanSection, List<CrisisPlanItem>>,
    onEdit: () -> Unit
) {
    val steps = crisisSteps(plan.filterValues { it.isNotEmpty() }.keys)
    val reasons = plan[CrisisPlanSection.REASONS].orEmpty()

    if (steps.isEmpty() && reasons.isEmpty()) {
        Text(
            "Du hast noch keinen Krisenplan. Das ist okay. Jetzt zählt nur eins: " +
                "Hol dir Hilfe – ruf eine der Nummern unten an oder schreib im Chat.",
            fontSize = 18.sp
        )
        OutlinedButton(onClick = onEdit, modifier = Modifier.fillMaxWidth()) {
            Text("Plan in einer ruhigen Minute anlegen")
        }
        return
    }

    Text(
        "Du musst jetzt nichts lösen. Geh deinen Plan Schritt für Schritt durch. " +
            "Wenn ein Schritt nicht hilft, nimm den nächsten.",
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.secondary
    )

    if (reasons.isNotEmpty()) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(CrisisPlanSection.REASONS.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                reasons.forEach { Text("• ${it.text}", fontSize = 16.sp) }
            }
        }
    }

    if (steps.isNotEmpty()) {
        var stepIndex by rememberSaveable { mutableIntStateOf(0) }
        val index = stepIndex.coerceIn(0, steps.lastIndex)
        val section = steps[index]

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Schritt ${index + 1} von ${steps.size}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(section.title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                plan[section].orEmpty().forEach { item -> PlanItemLine(item) }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            if (index > 0) {
                OutlinedButton(onClick = { stepIndex = index - 1 }, modifier = Modifier.weight(1f)) {
                    Text("Zurück")
                }
            }
            if (index < steps.lastIndex) {
                Button(onClick = { stepIndex = index + 1 }, modifier = Modifier.weight(2f)) {
                    Text("Hilft gerade nicht – nächster Schritt", textAlign = TextAlign.Center)
                }
            }
        }
        if (index == steps.lastIndex) {
            Text(
                "Wenn nichts davon hilft: Bitte ruf jetzt eine der Nummern unten an. Du musst da nicht allein durch.",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        }
    }

    TextButton(onClick = onEdit) { Text("Plan bearbeiten") }
}

@Composable
private fun PlanItemLine(item: CrisisPlanItem) {
    val context = LocalContext.current
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text("• ${item.text}", fontSize = 18.sp, modifier = Modifier.weight(1f))
        if (isDialable(item.phone)) {
            FilledTonalButton(onClick = { dial(context, item.phone!!) }) {
                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Anrufen")
            }
        }
    }
}

// --- Bearbeiten: in einer ruhigen Phase ausfüllen -----------------------------------------

@Composable
private fun CrisisPlanEditor(
    plan: Map<CrisisPlanSection, List<CrisisPlanItem>>,
    onAdd: (CrisisPlanSection, String, String?, (Boolean) -> Unit) -> Unit,
    onUpdate: (CrisisPlanItem, String, String?, (Boolean) -> Unit) -> Unit,
    onDelete: (CrisisPlanItem) -> Unit,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    var adding by remember { mutableStateOf<CrisisPlanSection?>(null) }
    var editingItem by remember { mutableStateOf<CrisisPlanItem?>(null) }
    var deleteCandidate by remember { mutableStateOf<CrisisPlanItem?>(null) }
    val onSaved: (Boolean) -> Unit = { saved ->
        if (saved) {
            adding = null
            editingItem = null
        } else {
            Toast.makeText(context, "Konnte gerade nicht gespeichert werden. Dein Text bleibt stehen.", Toast.LENGTH_LONG).show()
        }
    }

    Text(
        "Fülle den Plan in einer ruhigen Phase aus – gern zusammen mit einer Vertrauensperson " +
            "oder deiner Therapeutin. In einer Krise liest du ihn dann nur noch ab.",
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.secondary
    )

    CrisisPlanSection.entries.forEachIndexed { i, section ->
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val number = if (section == CrisisPlanSection.REASONS) "" else "${i + 1}. "
                Text("$number${section.title}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(section.question, fontSize = 15.sp)
                Text(section.example, fontSize = 13.sp, fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.secondary)

                plan[section].orEmpty().forEach { item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.text, fontSize = 16.sp)
                            item.phone?.let { Text(it, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary) }
                        }
                        IconButton(onClick = { editingItem = item }) {
                            Icon(Icons.Default.Edit, contentDescription = "Bearbeiten")
                        }
                        IconButton(onClick = { deleteCandidate = item }) {
                            Icon(Icons.Default.Delete, contentDescription = "Löschen")
                        }
                    }
                }

                TextButton(onClick = { adding = section }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Hinzufügen")
                }
            }
        }
    }

    Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Fertig") }

    adding?.let { section ->
        PlanItemDialog(
            section = section,
            initial = null,
            onSave = { text, phone -> onAdd(section, text, phone, onSaved) },
            onDismiss = { adding = null }
        )
    }
    editingItem?.let { item ->
        val section = CrisisPlanSection.fromStored(item.section) ?: return@let
        PlanItemDialog(
            section = section,
            initial = item,
            onSave = { text, phone -> onUpdate(item, text, phone, onSaved) },
            onDismiss = { editingItem = null }
        )
    }
    deleteCandidate?.let { item ->
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text("Eintrag löschen?") },
            text = { Text("„${item.text}“ wird aus deinem Krisenplan entfernt.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(item)
                    deleteCandidate = null
                }) { Text("Löschen", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidate = null }) { Text("Behalten") }
            }
        )
    }
}

@Composable
private fun PlanItemDialog(
    section: CrisisPlanSection,
    initial: CrisisPlanItem?,
    onSave: (text: String, phone: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var text by rememberSaveable { mutableStateOf(initial?.text.orEmpty()) }
    var phone by rememberSaveable { mutableStateOf(initial?.phone.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(section.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(section.question, fontSize = 14.sp)
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(if (section.hasPhone) "Name / Ort" else "Eintrag") }
                )
                if (section.hasPhone) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Telefonnummer (optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(text, phone.takeIf { section.hasPhone }) },
                enabled = text.isNotBlank()
            ) { Text("Speichern") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        }
    )
}

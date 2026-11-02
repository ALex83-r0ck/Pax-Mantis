package com.alexander.paxmentis.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun EducationScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                    )
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = "Wissens-Hub",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Verstehe, was in dir vorgeht. Wissen ist Macht über die Angst.",
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        EducationTopicCard(
            title = "Die Amygdala: Dein Bodyguard",
            content = "Die Amygdala ist das Alarmzentrum in deinem Gehirn. Bei Stress schlägt sie Alarm und löst 'Fight, Flight or Freeze' aus. In einer Panikattacke ist sie hyperaktiv und 'kapert' dein logisches Denken. Die Atemübungen in dieser App senden ein Signal an die Amygdala: 'Entwarnung, wir sind sicher'.",
            icon = Icons.Default.Shield
        )

        EducationTopicCard(
            title = "Konditionierung & Trigger",
            content = "Dein Gehirn lernt durch Verknüpfung. Wenn du einmal an einem Ort Panik hattest, speichert das Gehirn diesen Ort als gefährlich ab (Klassische Konditionierung). Ein Trigger ist wie ein alter Schaltkreis, der unbewusst aktiviert wird. Durch neue, positive Erfahrungen und Beruhigungstechniken können wir diese Schaltkreise 'umschreiben'.",
            icon = Icons.Default.Update
        )

        EducationTopicCard(
            title = "Das Nervensystem (Vagus)",
            content = "Dein Körper hat zwei Hauptmodi: Sympathikus (Gas/Stress) und Parasympathikus (Bremse/Ruhe). Der Vagusnerv ist der Hauptnerv des Parasympathikus. Durch tiefes Ausatmen (wie in der Atemübung) stimulieren wir diesen Nerv direkt und zwingen den Körper mechanisch in den Ruhemodus.",
            icon = Icons.Default.AutoGraph
        )

        EducationTopicCard(
            title = "Warum Tiny Steps?",
            content = "In einer Depression ist die Frontalhirn-Leistung reduziert, was Planen und Handeln extrem schwer macht. Große Aufgaben wirken wie Berge. 'Tiny Steps' (kleinste Aufgaben) nutzen ein psychologisches Prinzip: Erfolgserlebnisse schütten Dopamin aus, was den nächsten Schritt erst möglich macht. Es geht nicht um die Aufgabe, sondern um den Impuls.",
            icon = Icons.Default.Psychology
        )

        EducationTopicCard(
            title = "Das Stress-Toleranz-Fenster",
            content = "Jeder Mensch hat einen Bereich, in dem er Stress gut bewältigen kann (das 'Window of Tolerance'). Oberhalb liegt die Hyper-Erregung (Panik, Wut, Flucht), unterhalb die Hypo-Erregung (Einfrieren, Taubheit, Dissoziation). Die Übungen in dieser App helfen dir, wieder in dein Fenster zurückzufinden, wenn du es verlassen hast.",
            icon = Icons.Default.Info
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun EducationTopicCard(
    title: String,
    content: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2f,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
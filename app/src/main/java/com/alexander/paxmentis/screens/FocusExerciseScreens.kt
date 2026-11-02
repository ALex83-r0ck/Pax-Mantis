package com.alexander.paxmentis.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexander.paxmentis.domain.DotPace
import com.alexander.paxmentis.domain.Shape
import com.alexander.paxmentis.domain.ShapeKind
import com.alexander.paxmentis.domain.dotPosition
import com.alexander.paxmentis.domain.generateShapeRound
import kotlin.random.Random

/*
 * Visuelle Konzentrationsübungen: Sie lenken die Aufmerksamkeit sanft nach außen (Grounding),
 * etwa bei Gedankenkreisen oder Dissoziation. Bewusst ohne Zeitdruck, Punkte oder Fehler.
 */

@Composable
fun FollowDotScreen() {
    var running by rememberSaveable { mutableStateOf(true) }
    var pace by rememberSaveable { mutableStateOf(DotPace.CALM) }
    var phase by rememberSaveable { mutableFloatStateOf(0f) }

    // Eigene Zeitrechnung statt Endlos-Animation: Pausieren und Tempowechsel ohne Sprung.
    LaunchedEffect(running, pace) {
        if (!running) return@LaunchedEffect
        var last = withFrameMillis { it }
        while (true) {
            withFrameMillis { now ->
                phase = (phase + (now - last).toFloat() / pace.roundMillis) % 1f
                last = now
            }
        }
    }

    val dotColor = MaterialTheme.colorScheme.primary
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Folge dem Punkt", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Folge dem Punkt nur mit den Augen. Der Kopf bleibt ruhig, der Atem fließt von allein. " +
                "Wenn Gedanken kommen, kehr einfach zum Punkt zurück.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .semantics { contentDescription = "Ein Punkt gleitet langsam in einer liegenden Acht über die Fläche." }
        ) {
            val (x, y) = dotPosition(phase)
            val center = Offset(x * size.width, y * size.height)
            val radius = 14.dp.toPx()
            drawCircle(dotColor.copy(alpha = 0.18f), radius = radius * 2.2f, center = center)
            drawCircle(dotColor, radius = radius, center = center)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            DotPace.entries.forEach { option ->
                FilterChip(
                    selected = pace == option,
                    onClick = { pace = option },
                    label = { Text(option.label) }
                )
            }
            Spacer(Modifier.weight(1f))
            Button(onClick = { running = !running }) {
                Text(if (running) "Pause" else "Weiter")
            }
        }
    }
}

@Composable
fun FindShapesScreen() {
    var seed by rememberSaveable { mutableIntStateOf(Random.nextInt()) }
    var found by rememberSaveable { mutableStateOf(IntArray(0)) }
    var finishedRounds by rememberSaveable { mutableIntStateOf(0) }
    val round = remember(seed) { generateShapeRound(Random(seed)) }
    val foundSet = found.toSet()
    val complete = round.isComplete(foundSet)

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Finde die Formen", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Nimm dir Zeit. Es gibt keine Uhr und keine Fehler.",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                ShapeIcon(round.target, MaterialTheme.colorScheme.onPrimaryContainer, Modifier.size(36.dp))
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(round.target.task, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${foundSet.intersect(round.targetIndices).size} von ${round.targetIndices.size} gefunden",
                        fontSize = 14.sp
                    )
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(round.items) { index, kind ->
                val isFound = index in foundSet
                val isTarget = index in round.targetIndices
                // Falsche Formen reagieren nicht – kein Fehlerton, kein Rot.
                ShapeCell(
                    kind = kind,
                    found = isFound,
                    onClick = { if (isTarget && !isFound) found = found + index }
                )
            }
        }

        if (complete) {
            Text(
                if (finishedRounds > 0) "Gut gemacht – das war Runde ${finishedRounds + 1}."
                else "Gut gemacht. Spür kurz nach: Wo bist du gerade?",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Button(
                onClick = {
                    seed = Random.nextInt()
                    found = IntArray(0)
                    finishedRounds++
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Noch eine Runde") }
        }
    }
}

@Composable
private fun ShapeCell(kind: ShapeKind, found: Boolean, onClick: () -> Unit) {
    val fade by animateFloatAsState(if (found) 0.25f else 1f, animationSpec = tween(400), label = "found")
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .aspectRatio(1f)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick)
            .semantics { contentDescription = if (found) "${kind.label}, gefunden" else kind.label }
    ) {
        ShapeIcon(kind, MaterialTheme.colorScheme.primary, Modifier.fillMaxSize(0.6f).alpha(fade))
        if (found) {
            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun ShapeIcon(kind: ShapeKind, color: Color, modifier: Modifier) {
    Canvas(modifier) {
        val style: DrawStyle = if (kind.filled) Fill else Stroke(width = 3.dp.toPx())
        // Rand einrechnen, damit die Kontur nicht abgeschnitten wird.
        val inset = if (kind.filled) 0f else 1.5.dp.toPx()
        drawShape(kind.shape, color, style, inset)
    }
}

private fun DrawScope.drawShape(shape: Shape, color: Color, style: DrawStyle, inset: Float) {
    val w = size.width - 2 * inset
    val h = size.height - 2 * inset
    when (shape) {
        Shape.CIRCLE -> drawCircle(color, radius = minOf(w, h) / 2, style = style)
        Shape.SQUARE -> drawRoundRect(
            color,
            topLeft = Offset(inset + w * 0.06f, inset + h * 0.06f),
            size = Size(w * 0.88f, h * 0.88f),
            cornerRadius = CornerRadius(w * 0.08f),
            style = style
        )
        Shape.TRIANGLE -> {
            val path = Path().apply {
                moveTo(inset + w / 2, inset)
                lineTo(inset + w, inset + h)
                lineTo(inset, inset + h)
                close()
            }
            drawPath(path, color, style = style)
        }
    }
}

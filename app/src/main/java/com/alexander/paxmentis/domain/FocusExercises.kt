package com.alexander.paxmentis.domain

import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

// --- Folge dem Punkt ----------------------------------------------------------------------

/**
 * Position des Punktes als Anteil der Fläche (0..1), [phase] läuft von 0 bis 1 pro Runde.
 * Eine liegende Acht: weich, ohne Ecken und ohne Sprünge – das Auge kann ihr mühelos folgen.
 */
fun dotPosition(phase: Float): Pair<Float, Float> {
    val angle = 2 * PI * phase
    val x = 0.5 + 0.40 * sin(angle)
    val y = 0.5 + 0.30 * sin(2 * angle)
    return x.toFloat() to y.toFloat()
}

enum class DotPace(val label: String, val roundMillis: Long) {
    CALM("Ruhig", 16_000),
    VERY_CALM("Sehr ruhig", 26_000)
}

// --- Finde die Formen ---------------------------------------------------------------------

enum class Shape(val label: String, val plural: String) {
    CIRCLE("Kreis", "Kreise"),
    SQUARE("Quadrat", "Quadrate"),
    TRIANGLE("Dreieck", "Dreiecke")
}

/**
 * Unterschieden wird nach Form und Füllung, nicht nach Farbe: So funktioniert die Übung
 * bei Farbsehschwäche und in jeder Farbstimmung der App.
 */
data class ShapeKind(val shape: Shape, val filled: Boolean) {
    val label: String get() = when (shape) {
        Shape.CIRCLE -> if (filled) "ausgefüllter Kreis" else "leerer Kreis"
        Shape.SQUARE -> if (filled) "ausgefülltes Quadrat" else "leeres Quadrat"
        Shape.TRIANGLE -> if (filled) "ausgefülltes Dreieck" else "leeres Dreieck"
    }
    val task: String get() = "Tippe alle ${if (filled) "ausgefüllten" else "leeren"} ${shape.plural} an."
}

data class ShapeRound(val target: ShapeKind, val items: List<ShapeKind>) {
    val targetIndices: Set<Int> = items.indices.filter { items[it] == target }.toSet()

    fun isComplete(found: Set<Int>): Boolean = found.containsAll(targetIndices)
}

private val ALL_KINDS = Shape.entries.flatMap { listOf(ShapeKind(it, true), ShapeKind(it, false)) }

/** Eine Runde mit [size] Formen, davon 3 bis 6 gesuchte – genug zum Suchen, nie überfordernd. */
fun generateShapeRound(random: Random, size: Int = 20): ShapeRound {
    val target = ALL_KINDS.random(random)
    val others = ALL_KINDS - target
    val targetCount = random.nextInt(3, 7)
    val items = (List(targetCount) { target } + List(size - targetCount) { others.random(random) })
        .shuffled(random)
    return ShapeRound(target, items)
}

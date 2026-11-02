package com.alexander.paxmentis.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.random.Random

class FocusExercisesTest {

    @Test
    fun dotStaysInsideTheAreaWithMargin() {
        (0..1000).map { it / 1000f }.forEach { phase ->
            val (x, y) = dotPosition(phase)
            assertTrue("x=$x", x in 0.05f..0.95f)
            assertTrue("y=$y", y in 0.05f..0.95f)
        }
    }

    @Test
    fun dotMovesSmoothlyAndLoops() {
        val steps = (0..1000).map { dotPosition(it / 1000f) }
        steps.zipWithNext().forEach { (a, b) ->
            assertTrue(abs(a.first - b.first) < 0.01f && abs(a.second - b.second) < 0.01f)
        }
        val start = dotPosition(0f)
        val end = dotPosition(1f)
        assertEquals(start.first, end.first, 1e-4f)
        assertEquals(start.second, end.second, 1e-4f)
    }

    @Test
    fun everyRoundHasThreeToSixTargets() {
        repeat(500) { seed ->
            val round = generateShapeRound(Random(seed))
            assertEquals(20, round.items.size)
            assertTrue(round.targetIndices.size in 3..6)
        }
    }

    @Test
    fun sameSeedGivesSameRound() {
        assertEquals(generateShapeRound(Random(42)), generateShapeRound(Random(42)))
    }

    @Test
    fun roundIsCompleteOnlyWhenAllTargetsFound() {
        val round = generateShapeRound(Random(7))
        val targets = round.targetIndices
        assertFalse(round.isComplete(targets.drop(1).toSet()))
        assertTrue(round.isComplete(targets))
    }

    @Test
    fun labelsReadNaturally() {
        assertEquals("Tippe alle leeren Dreiecke an.", ShapeKind(Shape.TRIANGLE, false).task)
        assertEquals("ausgefülltes Quadrat", ShapeKind(Shape.SQUARE, true).label)
        assertEquals("leerer Kreis", ShapeKind(Shape.CIRCLE, false).label)
    }
}

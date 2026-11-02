package com.alexander.paxmentis.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CrisisLogicTest {

    // --- Panikhilfe: der Fertig-Zustand (mit den Notruf-Optionen) muss erreichbar sein.

    @Test
    fun panicDoneStateIsReachableByAdvancing() {
        var step = 0
        repeat(PANIC_STEPS.size) { step = nextPanicStep(step) }
        assertTrue(isPanicDone(step))
    }

    @Test
    fun panicIsNotDoneOnLastStep() {
        assertFalse(isPanicDone(PANIC_STEPS.size - 1))
    }

    @Test
    fun panicStepDoesNotOverflow() {
        assertEquals(PANIC_STEPS.size, nextPanicStep(PANIC_STEPS.size))
    }

    // --- Nachtwache

    @Test
    fun minutesUntilMorningRespectsMinutes() {
        assertEquals(5, minutesUntilMorning(5, 55))
        assertEquals(6 * 60 + 1, minutesUntilMorning(23, 59))
        assertEquals(6 * 60, minutesUntilMorning(0, 0))
    }

    @Test
    fun exactlySixMeansNextMorning() {
        assertEquals(24 * 60, minutesUntilMorning(6, 0))
    }

    @Test
    fun formatUsesCorrectGrammar() {
        assertEquals("5 Minuten", formatTimeUntilMorning(5, 55))
        assertEquals("1 Stunde", formatTimeUntilMorning(5, 0))
        assertEquals("1 Stunde und 1 Minute", formatTimeUntilMorning(4, 59))
        assertEquals("6 Stunden", formatTimeUntilMorning(0, 0))
    }

    @Test
    fun nightTimeBoundaries() {
        assertTrue(isNightTime(22))
        assertTrue(isNightTime(5))
        assertFalse(isNightTime(6))
        assertFalse(isNightTime(21))
    }

    // --- Shared Reality

    @Test
    fun whatsAppNumberNormalization() {
        assertEquals("491701234567", normalizeWhatsAppNumber("+49 170 1234567"))
        assertEquals("491701234567", normalizeWhatsAppNumber("0049 170 1234567"))
        assertEquals("491701234567", normalizeWhatsAppNumber("0170 / 123 45 67"))
        assertEquals("491701234567", normalizeWhatsAppNumber("491701234567"))
        assertEquals("43664123456", normalizeWhatsAppNumber("+43 664 123456"))
    }

    // --- Neutral-Messaging

    @Test
    fun angerIsDetected() {
        assertEquals(NeutralTemplate.ANGER, suggestNeutralTemplate("Ich bin total SAUER auf dich"))
    }

    @Test
    fun wordInsideOtherWordDoesNotMatch() {
        // "ruhe" steckt in "Unruhe", "warum" soll nur als ganzes Wort zählen.
        assertEquals(NeutralTemplate.TRIGGER, suggestNeutralTemplate("Ich spüre Angst und Unruhe"))
        assertEquals(NeutralTemplate.NEUTRAL, suggestNeutralTemplate("Ich habe aber Hunger"))
    }

    @Test
    fun boundaryIsDetected() {
        assertEquals(NeutralTemplate.BOUNDARY, suggestNeutralTemplate("Lass mich einfach in Ruhe!"))
    }

    @Test
    fun fallbackIsNeutral() {
        assertEquals(NeutralTemplate.NEUTRAL, suggestNeutralTemplate("Wir sehen uns morgen."))
    }
}

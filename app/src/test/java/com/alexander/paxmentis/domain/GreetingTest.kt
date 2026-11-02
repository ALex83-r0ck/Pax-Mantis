package com.alexander.paxmentis.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GreetingTest {

    @Test
    fun followsTimeOfDay() {
        assertEquals("Guten Morgen, Alex", greetingFor(7, "Alex"))
        assertEquals("Hallo, Alex", greetingFor(14, "Alex"))
        assertEquals("Guten Abend, Alex", greetingFor(20, "Alex"))
    }

    @Test
    fun nightIsNeverAFarewell() {
        listOf(23, 0, 3).forEach { hour ->
            assertEquals("Hallo, Alex", greetingFor(hour, "Alex"))
            assertTrue(greetingSubtitleFor(hour).contains("Nacht"))
        }
    }

    @Test
    fun worksWithoutName() {
        assertEquals("Guten Abend", greetingFor(20, "  "))
    }
}

package com.alexander.paxmentis.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CrisisPlanExportTest {

    private val plan = listOf(
        PlanEntry(CrisisPlanSection.WARNING_SIGNS, "Ich ziehe mich zurück"),
        PlanEntry(CrisisPlanSection.HELP_PEOPLE, "Mama", "0170 1234567"),
        PlanEntry(CrisisPlanSection.HELP_PEOPLE, "Tom"),
        PlanEntry(CrisisPlanSection.REASONS, "mein Hund")
    )

    @Test
    fun exportIsReadableAndRoundTrips() {
        val text = exportCrisisPlan(plan, "02.11.2026")

        assertTrue(text.startsWith(CRISIS_PLAN_HEADER))
        assertTrue(text.contains("## ${CrisisPlanSection.HELP_PEOPLE.title} [HELP_PEOPLE]"))
        assertTrue(text.contains("- Mama | Tel: 0170 1234567"))
        assertEquals(plan, parseCrisisPlan(text))
    }

    @Test
    fun specialCharactersSurvive() {
        val tricky = listOf(
            PlanEntry(CrisisPlanSection.OWN_STRATEGIES, "Erst duschen\ndann | raus \\ gehen"),
            PlanEntry(CrisisPlanSection.PROFESSIONALS, "Praxis | Tel: hier nicht trennen", "030 | 123")
        )
        assertEquals(tricky, parseCrisisPlan(exportCrisisPlan(tricky, "heute")))
    }

    @Test
    fun foreignFilesAreRejected() {
        assertNull(parseCrisisPlan("Einkaufsliste\n- Milch"))
        assertNull(parseCrisisPlan(""))
    }

    @Test
    fun toleratesWindowsLineEndingsBomAndUnknownSections() {
        val content = "﻿PaxMentis Krisenplan\r\n\r\n## Weg [GIBT_ES_NICHT]\r\n- verloren\r\n" +
            "## Umbenannter Titel [REASONS]\r\n- mein Hund\r\n"
        assertEquals(listOf(PlanEntry(CrisisPlanSection.REASONS, "mein Hund")), parseCrisisPlan(content))
    }

    @Test
    fun reimportAddsOnlyMissingEntries() {
        val existing = plan.take(2)
        val imported = plan + plan.last()
        assertEquals(plan.drop(2), newEntries(existing, imported))
    }
}

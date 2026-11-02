package com.alexander.paxmentis.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CrisisPlanTest {

    @Test
    fun stepsFollowPlanOrderAndSkipEmptySections() {
        val filled = setOf(
            CrisisPlanSection.PROFESSIONALS,
            CrisisPlanSection.WARNING_SIGNS,
            CrisisPlanSection.HELP_PEOPLE
        )
        assertEquals(
            listOf(CrisisPlanSection.WARNING_SIGNS, CrisisPlanSection.HELP_PEOPLE, CrisisPlanSection.PROFESSIONALS),
            crisisSteps(filled)
        )
    }

    @Test
    fun reasonsAreNeverAStep() {
        assertEquals(emptyList<CrisisPlanSection>(), crisisSteps(setOf(CrisisPlanSection.REASONS)))
    }

    @Test
    fun professionalHelpComesAfterOwnStrategies() {
        val all = crisisSteps(CrisisPlanSection.entries.toSet())
        assertTrue(all.indexOf(CrisisPlanSection.OWN_STRATEGIES) < all.indexOf(CrisisPlanSection.PROFESSIONALS))
    }

    @Test
    fun sectionsWithContactsAskForPhone() {
        assertTrue(CrisisPlanSection.HELP_PEOPLE.hasPhone)
        assertTrue(CrisisPlanSection.PROFESSIONALS.hasPhone)
        assertFalse(CrisisPlanSection.WARNING_SIGNS.hasPhone)
    }

    @Test
    fun storedSectionNamesRoundTrip() {
        CrisisPlanSection.entries.forEach { assertEquals(it, CrisisPlanSection.fromStored(it.name)) }
        assertNull(CrisisPlanSection.fromStored("UNBEKANNT"))
    }

    @Test
    fun dialableNeedsAtLeastThreeDigits() {
        assertTrue(isDialable("112"))
        assertTrue(isDialable("+49 170 1234567"))
        assertFalse(isDialable("11"))
        assertFalse(isDialable(""))
        assertFalse(isDialable(null))
    }
}

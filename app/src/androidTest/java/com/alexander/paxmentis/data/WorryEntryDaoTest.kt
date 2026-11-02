package com.alexander.paxmentis.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Prüft die Room-Schicht gegen eine In-Memory-Datenbank (ohne SQLCipher – die Verschlüsselung
 * ist Sache der Open-Helper-Factory, nicht des DAO).
 */
@RunWith(AndroidJUnit4::class)
class WorryEntryDaoTest {

    private lateinit var db: PaxMentisDatabase
    private lateinit var dao: WorryEntryDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PaxMentisDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.worryEntryDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun entriesAreReturnedNewestFirst() = runBlocking {
        dao.insert(WorryEntry(date = 1_000, worry = "alt"))
        dao.insert(WorryEntry(date = 3_000, worry = "neu"))
        dao.insert(WorryEntry(date = 2_000, worry = "mittel"))

        val worries = dao.getAll().first().map { it.worry }
        assertEquals(listOf("neu", "mittel", "alt"), worries)
    }

    @Test
    fun updateChangesEntryAndKeepsNulls() = runBlocking {
        dao.insert(WorryEntry(date = 1_000, worry = "vorher", solution = "x"))
        val stored = dao.getAll().first().single()

        dao.update(stored.copy(worry = "nachher", solution = null))

        val updated = dao.getAll().first().single()
        assertEquals("nachher", updated.worry)
        assertNull(updated.solution)
    }

    @Test
    fun deleteRemovesEntry() = runBlocking {
        dao.insert(WorryEntry(date = 1_000, worry = "weg damit"))
        val stored = dao.getAll().first().single()

        dao.delete(stored)

        assertTrue(dao.getAll().first().isEmpty())
    }
}

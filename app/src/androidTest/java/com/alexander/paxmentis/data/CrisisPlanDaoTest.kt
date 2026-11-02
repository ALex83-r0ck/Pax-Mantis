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

/** Room-Schicht des Krisenplans gegen eine In-Memory-Datenbank (ohne SQLCipher). */
@RunWith(AndroidJUnit4::class)
class CrisisPlanDaoTest {

    private lateinit var db: PaxMentisDatabase
    private lateinit var dao: CrisisPlanDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PaxMentisDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.crisisPlanDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun itemsAreReturnedByPosition() = runBlocking {
        dao.insert(CrisisPlanItem(section = "HELP_PEOPLE", text = "zweite", position = 1))
        dao.insert(CrisisPlanItem(section = "HELP_PEOPLE", text = "erste", position = 0))

        assertEquals(listOf("erste", "zweite"), dao.getAll().first().map { it.text })
    }

    @Test
    fun updateKeepsPhoneOptional() = runBlocking {
        dao.insert(CrisisPlanItem(section = "PROFESSIONALS", text = "Praxis", phone = "0301234"))
        val stored = dao.getAll().first().single()

        dao.update(stored.copy(text = "Praxis Dr. M.", phone = null))

        val updated = dao.getAll().first().single()
        assertEquals("Praxis Dr. M.", updated.text)
        assertNull(updated.phone)
    }

    @Test
    fun deleteRemovesItem() = runBlocking {
        dao.insert(CrisisPlanItem(section = "REASONS", text = "mein Hund"))
        dao.delete(dao.getAll().first().single())

        assertTrue(dao.getAll().first().isEmpty())
    }
}

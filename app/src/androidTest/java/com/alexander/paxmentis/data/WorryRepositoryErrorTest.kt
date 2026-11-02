package com.alexander.paxmentis.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alexander.paxmentis.data.repository.WorryRepository
import dagger.Lazy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Ein Datenbankfehler darf nie bis zur App durchschlagen (sonst stürzen auch die Notfall-Screens ab).
 */
@RunWith(AndroidJUnit4::class)
class WorryRepositoryErrorTest {

    @Test
    fun failingDatabaseOpen_isReportedNotThrown() = runBlocking {
        // Wie ein Fehler beim Aufbau der Datenbank (Keystore, Schema, ...).
        val repository = WorryRepository(Lazy { throw IllegalStateException("Room cannot verify the data integrity") })

        assertEquals(emptyList<List<WorryEntry>>(), repository.getAllEntries().toList())
        assertTrue(repository.unavailable.value)
        assertFalse(repository.insertEntry(WorryEntry(date = 1, worry = "x")))
        assertFalse(repository.updateEntry(WorryEntry(id = 1, date = 1, worry = "x")))
        assertFalse(repository.deleteEntry(WorryEntry(id = 1, date = 1, worry = "x")))
    }

    @Test
    fun failingQueryFlow_isReportedNotThrown() = runBlocking {
        val repository = WorryRepository(Lazy { FakeDao(flow { throw IllegalStateException("kaputt") }) })

        assertEquals(emptyList<List<WorryEntry>>(), repository.getAllEntries().toList())
        assertTrue(repository.unavailable.value)
    }

    @Test
    fun recoveredDatabase_clearsUnavailable() = runBlocking {
        var broken = true
        val entries = listOf(WorryEntry(id = 1, date = 1, worry = "da"))
        val repository = WorryRepository(Lazy {
            if (broken) throw IllegalStateException("noch nicht") else FakeDao(flowOf(entries))
        })

        repository.getAllEntries().toList()
        assertTrue(repository.unavailable.value)

        broken = false
        assertEquals(listOf(entries), repository.getAllEntries().toList())
        assertFalse(repository.unavailable.value)
        assertTrue(repository.insertEntry(entries.single()))
    }

    private class FakeDao(private val all: Flow<List<WorryEntry>>) : WorryEntryDao {
        override suspend fun insert(entry: WorryEntry) = Unit
        override fun getAll(): Flow<List<WorryEntry>> = all
        override suspend fun update(entry: WorryEntry) = Unit
        override suspend fun delete(entry: WorryEntry) = Unit
    }
}

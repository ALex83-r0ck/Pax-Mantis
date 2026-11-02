package com.alexander.paxmentis.data.repository

import com.alexander.paxmentis.data.WorryEntry
import com.alexander.paxmentis.data.WorryEntryDao
import dagger.Lazy
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** Sorgenstuhl-Einträge. Fehlerbehandlung siehe [GuardedRepository]. */
@Singleton
class WorryRepository @Inject constructor(
    private val worryEntryDao: Lazy<WorryEntryDao>
) : GuardedRepository("WorryRepository") {

    fun getAllEntries(): Flow<List<WorryEntry>> = guardedFlow { worryEntryDao.get().getAll() }

    suspend fun insertEntry(entry: WorryEntry): Boolean = guarded { worryEntryDao.get().insert(entry) }

    suspend fun updateEntry(entry: WorryEntry): Boolean = guarded { worryEntryDao.get().update(entry) }

    suspend fun deleteEntry(entry: WorryEntry): Boolean = guarded { worryEntryDao.get().delete(entry) }
}

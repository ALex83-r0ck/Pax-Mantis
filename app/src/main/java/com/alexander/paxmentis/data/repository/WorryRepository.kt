package com.alexander.paxmentis.data.repository

import com.alexander.paxmentis.data.WorryEntry
import com.alexander.paxmentis.data.WorryEntryDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorryRepository @Inject constructor(
    private val worryEntryDao: WorryEntryDao
) {
    fun getAllEntries(): Flow<List<WorryEntry>> = worryEntryDao.getAll()

    suspend fun insertEntry(entry: WorryEntry) = worryEntryDao.insert(entry)

    suspend fun updateEntry(entry: WorryEntry) = worryEntryDao.update(entry)

    suspend fun deleteEntry(entry: WorryEntry) = worryEntryDao.delete(entry)
}

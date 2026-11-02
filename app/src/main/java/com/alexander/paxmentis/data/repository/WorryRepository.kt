package com.alexander.paxmentis.data.repository

import com.alexander.paxmentis.data.WorryEntry
import com.alexander.paxmentis.data.WorryEntryDao
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Die Datenbank (inkl. Keystore-Zugriff und ggf. Umschlüsselung) wird erst bei der ersten
 * echten Nutzung und im IO-Thread geöffnet. So hängen Dashboard und Notfall-Screens nie
 * an der Datenbank.
 */
@Singleton
class WorryRepository @Inject constructor(
    private val worryEntryDao: Lazy<WorryEntryDao>
) {
    fun getAllEntries(): Flow<List<WorryEntry>> =
        flow { emitAll(worryEntryDao.get().getAll()) }.flowOn(Dispatchers.IO)

    suspend fun insertEntry(entry: WorryEntry) =
        withContext(Dispatchers.IO) { worryEntryDao.get().insert(entry) }

    suspend fun updateEntry(entry: WorryEntry) =
        withContext(Dispatchers.IO) { worryEntryDao.get().update(entry) }

    suspend fun deleteEntry(entry: WorryEntry) =
        withContext(Dispatchers.IO) { worryEntryDao.get().delete(entry) }
}

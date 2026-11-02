package com.alexander.paxmentis.data.repository

import com.alexander.paxmentis.data.CrisisPlanDao
import com.alexander.paxmentis.data.CrisisPlanItem
import dagger.Lazy
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** Einträge des Krisenplans. Fehlerbehandlung siehe [GuardedRepository]. */
@Singleton
class CrisisPlanRepository @Inject constructor(
    private val crisisPlanDao: Lazy<CrisisPlanDao>
) : GuardedRepository("CrisisPlanRepository") {

    fun getAllItems(): Flow<List<CrisisPlanItem>> = guardedFlow { crisisPlanDao.get().getAll() }

    suspend fun insertItem(item: CrisisPlanItem): Boolean = guarded { crisisPlanDao.get().insert(item) }

    /** Alle oder keiner – ein abgebrochener Import hinterlässt keinen halben Plan. */
    suspend fun insertItems(items: List<CrisisPlanItem>): Boolean = guarded { crisisPlanDao.get().insertAll(items) }

    suspend fun updateItem(item: CrisisPlanItem): Boolean = guarded { crisisPlanDao.get().update(item) }

    suspend fun deleteItem(item: CrisisPlanItem): Boolean = guarded { crisisPlanDao.get().delete(item) }
}

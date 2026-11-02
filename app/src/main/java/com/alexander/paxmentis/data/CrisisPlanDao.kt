package com.alexander.paxmentis.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CrisisPlanDao {
    @Insert
    suspend fun insert(item: CrisisPlanItem)

    @Query("SELECT * FROM crisis_plan_items ORDER BY position, id")
    fun getAll(): Flow<List<CrisisPlanItem>>

    @Update
    suspend fun update(item: CrisisPlanItem)

    @Delete
    suspend fun delete(item: CrisisPlanItem)
}

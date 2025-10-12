package com.alexander.paxmentis.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorryEntryDao {
    @Insert
    suspend fun insert(entry: WorryEntry)

    @Query("SELECT * FROM worry_entries ORDER BY date DESC")
    fun getAll(): Flow<List<WorryEntry>>

    @Update
    suspend fun update(entry: WorryEntry)

    @Delete
    suspend fun delete(entry: WorryEntry)
}
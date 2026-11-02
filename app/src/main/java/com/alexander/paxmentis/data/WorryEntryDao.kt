package com.alexander.paxmentis.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorryEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WorryEntry): Long

    @Query("SELECT * FROM worry_entries ORDER BY date DESC")
    fun getAll(): Flow<List<WorryEntry>>

    @Query("SELECT * FROM worry_entries WHERE id = :id")
    suspend fun getById(id: Int): WorryEntry?

    @Update
    suspend fun update(entry: WorryEntry)

    @Delete
    suspend fun delete(entry: WorryEntry)

    @Query("DELETE FROM worry_entries WHERE id = :id")
    suspend fun deleteById(id: Int)
}
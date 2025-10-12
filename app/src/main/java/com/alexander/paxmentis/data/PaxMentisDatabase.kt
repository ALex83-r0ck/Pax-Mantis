package com.alexander.paxmentis.data

import androidx.room.Database
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Database(entities = [WorryEntry::class], version = 1)
abstract class PaxMentisDatabase : RoomDatabase() {
    abstract fun worryEntryDao(): WorryEntryDao

    companion object {
        @Volatile
        private var INSTANCE: PaxMentisDatabase? = null

        fun getInstance(context: android.content.Context): PaxMentisDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    PaxMentisDatabase::class.java,
                    "paxmentis_db"
                ).build().also { INSTANCE = it }
            }
    }
}
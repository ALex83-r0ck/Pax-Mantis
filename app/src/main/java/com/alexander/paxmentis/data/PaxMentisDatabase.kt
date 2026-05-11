package com.alexander.paxmentis.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Database(entities = [WorryEntry::class], version = 1)
abstract class PaxMentisDatabase : RoomDatabase() {
    abstract fun worryEntryDao(): WorryEntryDao

    companion object {
        @Volatile
        private var INSTANCE: PaxMentisDatabase? = null

        fun getInstance(context: Context): PaxMentisDatabase =
            INSTANCE ?: synchronized(this) {
                val passphrase = "secure_passphrase".toByteArray()
                val factory = SupportOpenHelperFactory(passphrase)
                
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PaxMentisDatabase::class.java,
                    "paxmentis_db"
                )
                .openHelperFactory(factory)
                .build().also { INSTANCE = it }
            }
    }
}

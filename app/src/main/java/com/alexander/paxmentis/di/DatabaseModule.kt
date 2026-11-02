package com.alexander.paxmentis.di

import android.content.Context
import com.alexander.paxmentis.data.PaxMentisDatabase
import com.alexander.paxmentis.data.WorryEntryDao
import com.alexander.paxmentis.security.DatabaseKeyProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        keyProvider: DatabaseKeyProvider
    ): PaxMentisDatabase {
        return PaxMentisDatabase.getInstance(context, keyProvider)
    }

    @Provides
    fun provideWorryEntryDao(database: PaxMentisDatabase): WorryEntryDao {
        return database.worryEntryDao()
    }
}

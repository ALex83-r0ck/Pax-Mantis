package com.alexander.paxmentis.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "worry_entries")
data class WorryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val date: Long,

    // Die Kern-Sorge sollte im Normalfall vorhanden sein
    val worry: String,

    val reason: String? = null,
    val worstCase: String? = null,
    val solution: String? = null
)
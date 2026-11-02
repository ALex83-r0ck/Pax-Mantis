package com.alexander.paxmentis.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Ein Eintrag im Krisenplan. [section] ist der Name einer
 * [com.alexander.paxmentis.domain.CrisisPlanSection]; [phone] nur bei Abschnitten mit Kontakten.
 */
@Entity(tableName = "crisis_plan_items")
data class CrisisPlanItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val section: String,
    val text: String,
    val phone: String? = null,
    val position: Int = 0
)

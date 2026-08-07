package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scam_alerts")
data class ScamAlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String,
    val body: String,
    val dateEpochMillis: Long = System.currentTimeMillis(),
    val primaryReason: String,
    val detectedSignals: String = "",
    val isOverridden: Boolean = false
)

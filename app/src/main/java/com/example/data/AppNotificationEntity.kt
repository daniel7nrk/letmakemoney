package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_notifications")
data class AppNotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val timestamp: String = "Just now",
    val dateEpochMillis: Long = System.currentTimeMillis(),
    val isUnread: Boolean = true,
    val type: String, // "Transactions", "Scam Alerts", "Reports", "Security", "Pending Review"
    val transactionId: Long? = null,
    val scamAlertId: Long? = null,
    val targetRoute: String? = null
)

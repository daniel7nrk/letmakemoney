package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val currency: String,
    val dateEpochMillis: Long,
    val description: String,
    val merchant: String,
    val category: String,
    val source: String, // "SMS" or "MANUAL"
    val status: String, // "PENDING" or "CONFIRMED"
    val isRecurring: Boolean = false,
    val notes: String = "",
    val paymentMethod: String = "Mobile Money"
)

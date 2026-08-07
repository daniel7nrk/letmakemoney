package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val passwordHash: String,
    val fullName: String,
    val photoUri: String? = null,
    val isGoogleAuth: Boolean = false,
    val pinHash: String? = null,
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)

package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Jane Doe",
    val email: String = "jane.doe@promptpay.com",
    val photoUri: String? = null,
    val countryCode: String = "RWANDA",
    val currencyCode: String = "RWF",
    val languageCode: String = "en",
    val pushNotificationsEnabled: Boolean = true,
    val biometricEnabled: Boolean = false,
    val hasCompletedOnboarding: Boolean = false,
    val pinHash: String? = null,
    val lockMode: String = "PIN"
)

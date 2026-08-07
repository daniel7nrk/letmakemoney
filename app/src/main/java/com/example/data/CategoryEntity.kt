package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconName: String,
    val tag: String, // e.g. "Essential", "Fixed", "Income", "Commute", "Lifestyle"
    val colorHex: String,
    val isCustom: Boolean = false,
    val keywords: String = "" // comma separated keywords
)

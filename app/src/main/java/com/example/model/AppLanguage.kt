package com.example.model

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val flag: String
) {
    ENGLISH("en", "English", "English", "🇺🇸"),
    FRENCH("fr", "French", "Français", "🇫🇷");

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return when (code?.lowercase()) {
                "fr", "fra", "french" -> FRENCH
                else -> ENGLISH
            }
        }
    }
}

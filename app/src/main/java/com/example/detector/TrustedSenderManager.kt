package com.example.detector

import android.content.Context

object TrustedSenderManager {

    private const val PREF_NAME = "trusted_senders_pref"
    private const val KEY_TRUSTED_SENDERS = "trusted_senders_set"
    private const val KEY_BALANCE_PREFIX = "last_balance_"
    private const val KEY_SCAM_FILTERING_ENABLED = "scam_filtering_enabled"

    private val BUILT_IN_TRUSTED_SENDERS = setOf(
        "m-money", "mtn momo", "mtnmomo", "mtnrwanda", "airtel",
        "mpesa", "mpesa info", "safaricom", "boa", "cbe", "urubutopay"
    )

    fun isKnownOrTrustedSender(context: Context, sender: String): Boolean {
        val cleanSender = sender.trim().lowercase()
        
        // Check built-in trusted list
        if (BUILT_IN_TRUSTED_SENDERS.contains(cleanSender)) return true

        // Check if numeric shortcode (3 to 6 digits, e.g. 127, 131, 251994, 8994)
        if (cleanSender.matches(Regex("^\\d{3,6}$"))) return true

        // Check user-persisted trusted senders
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val userSet = prefs.getStringSet(KEY_TRUSTED_SENDERS, emptySet()) ?: emptySet()
        return userSet.contains(cleanSender)
    }

    fun addTrustedSender(context: Context, sender: String) {
        val cleanSender = sender.trim().lowercase()
        if (cleanSender.isBlank()) return
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val currentSet = prefs.getStringSet(KEY_TRUSTED_SENDERS, emptySet())?.toMutableSet() ?: mutableSetOf()
        currentSet.add(cleanSender)
        prefs.edit().putStringSet(KEY_TRUSTED_SENDERS, currentSet).apply()
    }

    fun getLastBalance(context: Context, providerKey: String): Double? {
        val cleanKey = providerKey.trim().lowercase()
        if (cleanKey.isBlank()) return null
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val key = KEY_BALANCE_PREFIX + cleanKey
        if (!prefs.contains(key)) return null
        val valFloat = prefs.getFloat(key, -1f)
        return if (valFloat >= 0) valFloat.toDouble() else null
    }

    fun setLastBalance(context: Context, providerKey: String, balance: Double) {
        val cleanKey = providerKey.trim().lowercase()
        if (cleanKey.isBlank()) return
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val key = KEY_BALANCE_PREFIX + cleanKey
        prefs.edit().putFloat(key, balance.toFloat()).apply()
    }

    fun isScamFilteringEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_SCAM_FILTERING_ENABLED, true)
    }

    fun setScamFilteringEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SCAM_FILTERING_ENABLED, enabled).apply()
    }
}

package com.example.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object SupabaseSyncManager {

    private const val TAG = "SupabaseSyncManager"
    private const val SUPABASE_URL = "https://berurshibbxpunyselmd.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_fRdux5ehpwczDqez9SLi_g_znW3Czty"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    fun isOnline(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
            val network = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Safely synchronizes local user profile to Supabase in the background.
     * Guaranteed offline-first: if device is offline or request fails, does nothing
     * and never interrupts the user.
     */
    fun syncIfOnline(context: Context) {
        if (!isOnline(context)) {
            Log.d(TAG, "Device is offline. Skipping Supabase sync.")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val profile = db.userProfileDao().getUserProfileDirect() ?: return@launch
                if (profile.email.isBlank()) return@launch

                val jsonPayload = JSONObject().apply {
                    put("email", profile.email)
                    put("full_name", profile.name)
                    put("role", "Account Executive")
                    put("updated_at", System.currentTimeMillis())
                }.toString()

                val requestBody = jsonPayload.toRequestBody("application/json; charset=utf-8".toMediaType())

                val request = Request.Builder()
                    .url("$SUPABASE_URL/rest/v1/profiles")
                    .header("apikey", SUPABASE_KEY)
                    .header("Authorization", "Bearer $SUPABASE_KEY")
                    .header("Prefer", "resolution=merge-duplicates")
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        Log.d(TAG, "Successfully synced profile to Supabase: ${profile.email}")
                    } else {
                        Log.d(TAG, "Supabase responded with code: ${response.code} (silently handled)")
                    }
                }
            } catch (e: Exception) {
                // Completely safe fallback: never propagate errors to the UI
                Log.d(TAG, "Background Supabase sync skipped: ${e.message}")
            }
        }
    }
}

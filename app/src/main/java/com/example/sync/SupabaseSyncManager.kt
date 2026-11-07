package com.example.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.auth.SupabaseAuthManager
import com.example.data.AppDatabase
import com.example.data.TransactionEntity
import com.example.data.UserProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Offline-first Supabase sync manager for Android.
 *
 * Flow:
 *   1. All transactions and profile edits are saved locally first (Room DB, 100% offline).
 *   2. When NetworkMonitor detects connectivity -> syncAllPendingData(context) is triggered.
 *   3. If the user is authenticated (JWT in SupabaseAuthManager), unsynced records
 *      are upserted to Supabase using the user's access token and user_id.
 *   4. On success, local records are marked isSynced = true.
 *
 * If offline or not authenticated, sync is cleanly skipped without interrupting the user.
 */
object SupabaseSyncManager {

    private const val TAG = "SupabaseSyncManager"
    private const val SUPABASE_URL = SupabaseAuthManager.SUPABASE_URL
    private const val SUPABASE_KEY = SupabaseAuthManager.SUPABASE_KEY

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val syncMutex = Mutex()

    fun isOnline(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
            val network = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Public entry point to trigger synchronization of all pending data.
     * Safe to call from anywhere (main thread, background, network callbacks).
     */
    fun syncAllPendingData(context: Context) {
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            syncInternal(appContext)
        }
    }

    /**
     * Alias for backward compatibility with existing callers.
     */
    fun syncIfOnline(context: Context) {
        syncAllPendingData(context)
    }

    private suspend fun syncInternal(context: Context) {
        if (!isOnline(context)) {
            Log.d(TAG, "📴 Device is offline — skipping Supabase sync")
            return
        }

        val auth = SupabaseAuthManager.getInstance(context)
        if (!auth.isAuthenticated.value) {
            Log.d(TAG, "⚠️ Not authenticated with Supabase — skipping cloud sync")
            return
        }

        syncMutex.withLock {
            try {
                // Silently refresh token if close to expiry
                auth.refreshIfNeeded()

                val token = auth.accessToken
                val userId = auth.userId
                if (token.isNullOrBlank() || userId.isNullOrBlank()) {
                    Log.d(TAG, "⚠️ Missing access token or user ID")
                    return
                }

                val db = AppDatabase.getDatabase(context)

                // 1. Sync User Profile (Push)
                syncUserProfile(db, token, userId)

                // 2. Sync Unsynced Transactions (Push)
                syncTransactions(db, token, userId)

                // 3. Download and merge Cloud Transactions (Pull)
                fetchRemoteTransactions(db, token, userId)

                // 4. Download and merge Cloud Profile (Pull)
                fetchRemoteUserProfile(db, token, userId)

            } catch (e: Exception) {
                Log.e(TAG, "❌ Background Supabase sync skipped/failed: ${e.message}", e)
            }
        }
    }

    private suspend fun syncUserProfile(db: AppDatabase, token: String, userId: String) = withContext(Dispatchers.IO) {
        try {
            val profile = db.userProfileDao().getUserProfileDirect() ?: return@withContext
            if (profile.email.isBlank()) return@withContext

            val jsonPayload = JSONObject().apply {
                put("id", profile.id)
                put("user_id", userId)
                put("name", profile.name)
                put("email", profile.email)
                put("country_code", profile.countryCode)
                put("currency_code", profile.currencyCode)
                put("language_code", profile.languageCode)
                put("lock_mode", profile.lockMode)
                put("push_notifications_enabled", profile.pushNotificationsEnabled)
                put("biometric_enabled", profile.biometricEnabled)
            }.toString()

            val requestBody = jsonPayload.toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url("$SUPABASE_URL/rest/v1/user_profiles?on_conflict=user_id")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("apikey", SUPABASE_KEY)
                .header("Authorization", "Bearer $token")
                .header("Prefer", "resolution=merge-duplicates")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Log.d(TAG, "✅ Synced user profile to Supabase")
                } else {
                    Log.w(TAG, "⚠️ User profile sync returned HTTP ${response.code}")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ User profile sync exception: ${e.message}")
        }
    }

    private suspend fun syncTransactions(db: AppDatabase, token: String, userId: String) = withContext(Dispatchers.IO) {
        val unsynced = db.transactionDao().getUnsyncedTransactions()
        if (unsynced.isEmpty()) {
            Log.d(TAG, "✅ All transactions already synced")
            return@withContext
        }

        Log.d(TAG, "Uploading ${unsynced.size} unsynced transaction(s) to Supabase...")

        val jsonArray = JSONArray()
        for (tx in unsynced) {
            val obj = JSONObject().apply {
                put("id", tx.id)
                put("user_id", userId)
                put("amount", tx.amount)
                put("currency", tx.currency)
                put("merchant", tx.merchant)
                put("description", tx.description)
                put("category", tx.category)
                put("fee", tx.fee)
                put("fee_category", tx.feeCategory)
                put("source", tx.source)
                put("status", tx.status)
                put("is_recurring", tx.isRecurring)
                put("notes", tx.notes)
                put("payment_method", tx.paymentMethod)
                put("date_epoch_millis", tx.dateEpochMillis)
                put("updated_at_epoch_millis", tx.updatedAtEpochMillis)
                put("is_synced", true)
            }
            jsonArray.put(obj)
        }

        val requestBody = jsonArray.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url("$SUPABASE_URL/rest/v1/transactions?on_conflict=id")
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("apikey", SUPABASE_KEY)
            .header("Authorization", "Bearer $token")
            .header("Prefer", "resolution=merge-duplicates")
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val ids = unsynced.map { it.id }
                    db.transactionDao().markTransactionsAsSynced(ids)
                    Log.d(TAG, "✅ Successfully synced ${ids.size} transaction(s) to Supabase")
                } else {
                    val err = response.body?.string() ?: ""
                    Log.e(TAG, "❌ Transactions upsert failed HTTP ${response.code}: $err")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Transactions upsert network error: ${e.message}")
        }
    }

    private suspend fun fetchRemoteTransactions(db: AppDatabase, token: String, userId: String) = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$SUPABASE_URL/rest/v1/transactions?select=*&order=date_epoch_millis.desc")
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("apikey", SUPABASE_KEY)
            .header("Authorization", "Bearer $token")
            .get()
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@use
                    val jsonArray = JSONArray(body)
                    val remoteList = mutableListOf<TransactionEntity>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        remoteList.add(
                            TransactionEntity(
                                id = obj.getLong("id"),
                                amount = obj.optDouble("amount", 0.0),
                                currency = obj.optString("currency", "RWF"),
                                merchant = obj.optString("merchant", ""),
                                description = obj.optString("description", ""),
                                category = obj.optString("category", "Uncategorized"),
                                fee = obj.optDouble("fee", 0.0),
                                feeCategory = obj.optString("fee_category", "Transaction Fee"),
                                source = obj.optString("source", "SMS"),
                                status = obj.optString("status", "CONFIRMED"),
                                isRecurring = obj.optBoolean("is_recurring", false),
                                notes = obj.optString("notes", ""),
                                paymentMethod = obj.optString("payment_method", "Mobile Money"),
                                dateEpochMillis = obj.optLong("date_epoch_millis", System.currentTimeMillis()),
                                updatedAtEpochMillis = obj.optLong("updated_at_epoch_millis", System.currentTimeMillis()),
                                isSynced = true
                            )
                        )
                    }
                    if (remoteList.isNotEmpty()) {
                        db.transactionDao().insertTransactions(remoteList)
                        Log.d(TAG, "✅ Downloaded and merged ${remoteList.size} remote transaction(s) from Supabase")
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Remote transaction fetch error: ${e.message}")
        }
    }

    private suspend fun fetchRemoteUserProfile(db: AppDatabase, token: String, userId: String) = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$SUPABASE_URL/rest/v1/user_profiles?user_id=eq.$userId&select=*")
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("apikey", SUPABASE_KEY)
            .header("Authorization", "Bearer $token")
            .get()
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@use
                    val jsonArray = JSONArray(body)
                    if (jsonArray.length() > 0) {
                        val obj = jsonArray.getJSONObject(0)
                        val existing = db.userProfileDao().getUserProfileDirect()
                        val updated = (existing ?: UserProfileEntity(id = 1)).copy(
                            name = if (existing?.name.isNullOrBlank()) obj.optString("name", "") else existing!!.name,
                            email = if (existing?.email.isNullOrBlank()) obj.optString("email", "") else existing!!.email,
                            countryCode = if (existing?.countryCode.isNullOrBlank()) obj.optString("country_code", "RWANDA") else existing!!.countryCode,
                            currencyCode = if (existing?.currencyCode.isNullOrBlank()) obj.optString("currency_code", "RWF") else existing!!.currencyCode,
                            languageCode = if (existing?.languageCode.isNullOrBlank()) obj.optString("language_code", "en") else existing!!.languageCode,
                            supabaseUserId = userId,
                            hasCompletedSupabaseLogin = true
                        )
                        db.userProfileDao().insertOrUpdateProfile(updated)
                        Log.d(TAG, "✅ Merged remote user profile from Supabase")
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Remote profile fetch error: ${e.message}")
        }
    }
}

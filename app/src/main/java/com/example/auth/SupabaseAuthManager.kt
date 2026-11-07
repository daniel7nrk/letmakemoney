package com.example.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * One-time online Supabase authentication for Android.
 *
 * - First launch: user signs in or registers with email + password (requires internet).
 * - The JWT access token + refresh token are persisted in private SharedPreferences.
 * - On every subsequent launch the stored token is used — NO internet needed.
 * - When online and the access token is near expiry, it is silently refreshed.
 */
class SupabaseAuthManager private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("supabase_auth_prefs", Context.MODE_PRIVATE)

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _supabaseUserId = MutableStateFlow<String?>(null)
    val supabaseUserId: StateFlow<String?> = _supabaseUserId.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        val token = prefs.getString(KEY_ACCESS_TOKEN, null)
        val uid = prefs.getString(KEY_USER_ID, null)
        if (!token.isNullOrBlank()) {
            _isAuthenticated.value = true
            _supabaseUserId.value = uid
        }
    }

    val accessToken: String?
        get() = prefs.getString(KEY_ACCESS_TOKEN, null)

    val userId: String?
        get() = prefs.getString(KEY_USER_ID, null)

    val userEmail: String?
        get() = prefs.getString(KEY_USER_EMAIL, null)

    fun clearError() {
        _authError.value = null
    }

    suspend fun signIn(email: String, pass: String): Boolean = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || pass.isBlank()) {
            _authError.value = "Please enter both email and password."
            return@withContext false
        }

        _isLoading.value = true
        _authError.value = null
        try {
            val url = "$SUPABASE_URL/auth/v1/token?grant_type=password"
            val jsonPayload = JSONObject().apply {
                put("email", trimmedEmail)
                put("password", pass)
            }.toString()

            val requestBody = jsonPayload.toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .header("apikey", SUPABASE_KEY)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val json = JSONObject(bodyString)
                    val newAccessToken = json.optString("access_token", "")
                    val newRefreshToken = json.optString("refresh_token", "")
                    val expiresIn = json.optLong("expires_in", 3600L)
                    val expiresAt = (System.currentTimeMillis() / 1000) + expiresIn

                    var uid = ""
                    var uEmail = trimmedEmail
                    val userObj = json.optJSONObject("user")
                    if (userObj != null) {
                        uid = userObj.optString("id", "")
                        uEmail = userObj.optString("email", trimmedEmail)
                    }

                    prefs.edit()
                        .putString(KEY_ACCESS_TOKEN, newAccessToken)
                        .putString(KEY_REFRESH_TOKEN, newRefreshToken)
                        .putString(KEY_USER_ID, uid)
                        .putString(KEY_USER_EMAIL, uEmail)
                        .putLong(KEY_EXPIRES_AT, expiresAt)
                        .apply()

                    _isAuthenticated.value = true
                    _supabaseUserId.value = uid
                    _authError.value = null
                    Log.d(TAG, "Successfully signed in to Supabase user: $uid")
                    return@withContext true
                } else {
                    val errMsg = parseError(bodyString, "Login failed (${response.code})")
                    _authError.value = errMsg
                    Log.e(TAG, "Supabase sign in failed: $errMsg")
                    return@withContext false
                }
            }
        } catch (e: Exception) {
            val msg = "Network error: ${e.localizedMessage ?: e.message}"
            _authError.value = msg
            Log.e(TAG, msg, e)
            return@withContext false
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun signUp(email: String, pass: String): Boolean = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || pass.length < 6) {
            _authError.value = "Password must be at least 6 characters long."
            return@withContext false
        }

        _isLoading.value = true
        _authError.value = null
        try {
            val url = "$SUPABASE_URL/auth/v1/signup"
            val jsonPayload = JSONObject().apply {
                put("email", trimmedEmail)
                put("password", pass)
            }.toString()

            val requestBody = jsonPayload.toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .header("apikey", SUPABASE_KEY)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val json = JSONObject(bodyString)
                    val token = json.optString("access_token", "")
                    if (token.isNotBlank()) {
                        // Direct session returned
                        val refreshToken = json.optString("refresh_token", "")
                        val expiresIn = json.optLong("expires_in", 3600L)
                        val expiresAt = (System.currentTimeMillis() / 1000) + expiresIn
                        val userObj = json.optJSONObject("user")
                        val uid = userObj?.optString("id", "") ?: ""
                        val uEmail = userObj?.optString("email", trimmedEmail) ?: trimmedEmail

                        prefs.edit()
                            .putString(KEY_ACCESS_TOKEN, token)
                            .putString(KEY_REFRESH_TOKEN, refreshToken)
                            .putString(KEY_USER_ID, uid)
                            .putString(KEY_USER_EMAIL, uEmail)
                            .putLong(KEY_EXPIRES_AT, expiresAt)
                            .apply()

                        _isAuthenticated.value = true
                        _supabaseUserId.value = uid
                        _authError.value = null
                        return@withContext true
                    } else {
                        // Attempt sign in to obtain tokens
                        return@withContext signIn(trimmedEmail, pass)
                    }
                } else {
                    val errMsg = parseError(bodyString, "Sign-up failed (${response.code})")
                    _authError.value = errMsg
                    Log.e(TAG, "Supabase sign up failed: $errMsg")
                    return@withContext false
                }
            }
        } catch (e: Exception) {
            val msg = "Network error: ${e.localizedMessage ?: e.message}"
            _authError.value = msg
            Log.e(TAG, msg, e)
            return@withContext false
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun refreshIfNeeded(): Boolean = withContext(Dispatchers.IO) {
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
        val now = System.currentTimeMillis() / 1000
        val shouldRefresh = now > (expiresAt - 300) // 5 minutes before expiration
        val refreshToken = prefs.getString(KEY_REFRESH_TOKEN, null)

        if (!shouldRefresh || refreshToken.isNullOrBlank()) {
            return@withContext false
        }

        Log.d(TAG, "Refreshing Supabase access token...")
        try {
            val url = "$SUPABASE_URL/auth/v1/token?grant_type=refresh_token"
            val jsonPayload = JSONObject().apply {
                put("refresh_token", refreshToken)
            }.toString()

            val requestBody = jsonPayload.toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .header("apikey", SUPABASE_KEY)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val json = JSONObject(bodyString)
                    val newAccess = json.optString("access_token", "")
                    val newRefresh = json.optString("refresh_token", refreshToken)
                    val expiresIn = json.optLong("expires_in", 3600L)
                    val newExpiresAt = (System.currentTimeMillis() / 1000) + expiresIn

                    prefs.edit()
                        .putString(KEY_ACCESS_TOKEN, newAccess)
                        .putString(KEY_REFRESH_TOKEN, newRefresh)
                        .putLong(KEY_EXPIRES_AT, newExpiresAt)
                        .apply()

                    Log.d(TAG, "Supabase token refreshed successfully")
                    return@withContext true
                } else {
                    Log.w(TAG, "Supabase token refresh failed with HTTP ${response.code}")
                    return@withContext false
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Supabase token refresh skipped (offline?): ${e.message}")
            return@withContext false
        }
    }

    fun signOut() {
        prefs.edit().clear().apply()
        _isAuthenticated.value = false
        _supabaseUserId.value = null
        _authError.value = null
    }

    private fun parseError(body: String, fallback: String): String {
        return try {
            val obj = JSONObject(body)
            obj.optString("error_description",
                obj.optString("msg",
                    obj.optString("message", fallback)
                )
            )
        } catch (_: Exception) {
            fallback
        }
    }

    companion object {
        private const val TAG = "SupabaseAuthManager"
        const val SUPABASE_URL = "https://berurshibbxpunyselmd.supabase.co"
        const val SUPABASE_KEY = "sb_publishable_fRdux5ehpwczDqez9SLi_g_znW3Czty"

        private const val KEY_ACCESS_TOKEN = "supabase.access_token"
        private const val KEY_REFRESH_TOKEN = "supabase.refresh_token"
        private const val KEY_USER_ID = "supabase.user_id"
        private const val KEY_USER_EMAIL = "supabase.user_email"
        private const val KEY_EXPIRES_AT = "supabase.expires_at"

        @Volatile
        private var INSTANCE: SupabaseAuthManager? = null

        fun getInstance(context: Context): SupabaseAuthManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SupabaseAuthManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}

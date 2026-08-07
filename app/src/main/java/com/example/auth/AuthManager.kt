package com.example.auth

import android.content.Context
import com.example.data.AppDatabase
import com.example.data.UserEntity
import com.example.data.UserProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AuthUser(
    val uid: String,
    val email: String,
    val displayName: String,
    val photoUrl: String? = null
)

class AuthManager(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val userDao = db.userDao()
    private val userProfileDao = db.userProfileDao()

    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    val currentUser: StateFlow<AuthUser?> = _currentUser

    init {
        CoroutineScope(Dispatchers.IO).launch {
            val existingProfile = userProfileDao.getUserProfileDirect()
            if (existingProfile != null && existingProfile.email.isNotBlank()) {
                _currentUser.value = AuthUser(
                    uid = "local_${existingProfile.id}",
                    email = existingProfile.email,
                    displayName = existingProfile.name,
                    photoUrl = existingProfile.photoUri
                )
            }
        }
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<AuthUser> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim()
        val existingUser = userDao.getUserByEmail(trimmedEmail)

        if (existingUser == null) {
            return@withContext Result.failure(Exception("No account found with this email. Please sign up."))
        }

        if (!PasswordUtil.verifyPassword(pass, existingUser.passwordHash)) {
            return@withContext Result.failure(Exception("Incorrect password. Please try again."))
        }

        // Update active profile in Room
        val currentProfile = userProfileDao.getUserProfileDirect() ?: UserProfileEntity(id = 1)
        val updatedProfile = currentProfile.copy(
            name = existingUser.fullName,
            email = existingUser.email,
            photoUri = existingUser.photoUri,
            pinHash = existingUser.pinHash ?: currentProfile.pinHash
        )
        userProfileDao.insertOrUpdateProfile(updatedProfile)

        val authUser = AuthUser(
            uid = "local_${existingUser.id}",
            email = existingUser.email,
            displayName = existingUser.fullName,
            photoUrl = existingUser.photoUri
        )
        _currentUser.value = authUser
        Result.success(authUser)
    }

    suspend fun signUpWithEmail(email: String, pass: String, name: String): Result<AuthUser> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim()
        val displayName = name.trim().ifBlank { trimmedEmail.substringBefore("@").replaceFirstChar { it.uppercase() } }

        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) {
            return@withContext Result.failure(Exception("Please enter a valid email address."))
        }
        if (pass.length < 6) {
            return@withContext Result.failure(Exception("Password must be at least 6 characters long."))
        }

        val existingUser = userDao.getUserByEmail(trimmedEmail)
        if (existingUser != null) {
            return@withContext Result.failure(Exception("An account with this email already exists. Please sign in."))
        }

        val hashedPassword = PasswordUtil.hashPassword(pass)
        val newUser = UserEntity(
            email = trimmedEmail,
            passwordHash = hashedPassword,
            fullName = displayName,
            photoUri = null,
            isGoogleAuth = false
        )
        val insertedId = userDao.insertUser(newUser)

        // Clear transactions and seed default categories for clean new-account state
        db.transactionDao().deleteAllTransactions()
        AppDatabase.seedDefaultCategoriesIfEmpty(db)

        // Update active profile in Room (reset PIN for new user)
        val updatedProfile = UserProfileEntity(
            id = 1,
            name = displayName,
            email = trimmedEmail,
            photoUri = null,
            pinHash = null,
            lockMode = "PIN",
            hasCompletedOnboarding = true
        )
        userProfileDao.insertOrUpdateProfile(updatedProfile)

        val authUser = AuthUser(
            uid = "local_$insertedId",
            email = trimmedEmail,
            displayName = displayName,
            photoUrl = null
        )
        _currentUser.value = authUser
        Result.success(authUser)
    }

    suspend fun signInWithGoogle(displayName: String, email: String): Result<AuthUser> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim().ifBlank { "user.google@prompttrack.com" }
        val name = displayName.trim().ifBlank { "Google User" }

        var user = userDao.getUserByEmail(trimmedEmail)
        val isNewAccount = user == null
        val userId = if (isNewAccount) {
            val newUser = UserEntity(
                email = trimmedEmail,
                passwordHash = PasswordUtil.hashPassword("GOOGLE_AUTH_${System.currentTimeMillis()}"),
                fullName = name,
                photoUri = null,
                isGoogleAuth = true
            )
            val newId = userDao.insertUser(newUser)
            db.transactionDao().deleteAllTransactions()
            AppDatabase.seedDefaultCategoriesIfEmpty(db)
            newId
        } else {
            user!!.id
        }

        // Update active profile in Room
        val currentProfile = userProfileDao.getUserProfileDirect() ?: UserProfileEntity(id = 1)
        val updatedProfile = if (isNewAccount) {
            UserProfileEntity(
                id = 1,
                name = name,
                email = trimmedEmail,
                photoUri = null,
                pinHash = null,
                lockMode = "PIN",
                hasCompletedOnboarding = true
            )
        } else {
            currentProfile.copy(
                name = name,
                email = trimmedEmail,
                photoUri = user?.photoUri,
                pinHash = user?.pinHash ?: currentProfile.pinHash
            )
        }
        userProfileDao.insertOrUpdateProfile(updatedProfile)

        val authUser = AuthUser(
            uid = "local_$userId",
            email = trimmedEmail,
            displayName = name,
            photoUrl = user?.photoUri
        )
        _currentUser.value = authUser
        Result.success(authUser)
    }

    suspend fun updateProfilePhoto(photoUri: String): Unit = withContext(Dispatchers.IO) {
        val currentProfile = userProfileDao.getUserProfileDirect() ?: UserProfileEntity(id = 1)
        val updatedProfile = currentProfile.copy(photoUri = photoUri)
        userProfileDao.insertOrUpdateProfile(updatedProfile)

        val activeUser = userDao.getUserByEmail(currentProfile.email)
        if (activeUser != null) {
            userDao.updateUser(activeUser.copy(photoUri = photoUri))
        }

        val curr = _currentUser.value
        if (curr != null) {
            _currentUser.value = curr.copy(photoUrl = photoUri)
        }
    }

    fun signOut() {
        CoroutineScope(Dispatchers.IO).launch {
            // Optionally retain profile preferences, or clear current session state
        }
        _currentUser.value = null
    }
}

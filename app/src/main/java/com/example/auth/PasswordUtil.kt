package com.example.auth

import java.security.MessageDigest

object PasswordUtil {
    fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, storedHash: String): Boolean {
        return hashPassword(password) == storedHash
    }
}

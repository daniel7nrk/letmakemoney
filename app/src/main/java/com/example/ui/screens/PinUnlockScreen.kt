package com.example.ui.screens

import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.data.UserProfileEntity
import com.example.ui.theme.RoyalBlue
import kotlinx.coroutines.delay

@Composable
fun PinUnlockScreen(
    userProfile: UserProfileEntity?,
    onVerifyPin: (String) -> Boolean,
    onUnlockSuccess: () -> Unit
) {
    val context = LocalContext.current
    var pinInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var failedAttempts by remember { mutableStateOf(0) }
    var isLockedOut by remember { mutableStateOf(false) }
    var lockoutSeconds by remember { mutableStateOf(0) }

    // Lockout timer coroutine
    LaunchedEffect(isLockedOut) {
        if (isLockedOut) {
            lockoutSeconds = 30
            while (lockoutSeconds > 0) {
                delay(1000)
                lockoutSeconds -= 1
            }
            isLockedOut = false
            failedAttempts = 0
            errorMessage = null
        }
    }

    fun triggerBiometricPrompt() {
        val activity = context as? FragmentActivity ?: return
        try {
            val executor = ContextCompat.getMainExecutor(activity)
            val biometricPrompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        onUnlockSuccess()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                            errorMessage = errString.toString()
                        }
                    }

                    override fun onAuthenticationFailed() {
                        errorMessage = "Biometric authentication failed."
                    }
                }
            )

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock PromptPay")
                .setSubtitle("Confirm fingerprint, face, or device PIN")
                .setNegativeButtonText("Use 5-Digit PIN")
                .build()

            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Auto-trigger biometric prompt if lockMode is BIOMETRIC
    LaunchedEffect(userProfile?.lockMode) {
        if (userProfile?.lockMode == "BIOMETRIC") {
            triggerBiometricPrompt()
        }
    }

    fun handlePinSubmit(input: String) {
        if (isLockedOut) return
        if (onVerifyPin(input)) {
            onUnlockSuccess()
        } else {
            failedAttempts += 1
            pinInput = ""
            if (failedAttempts >= 5) {
                isLockedOut = true
                errorMessage = "Too many failed attempts. Please wait 30 seconds."
            } else {
                val remaining = 5 - failedAttempts
                errorMessage = "Incorrect PIN. $remaining attempts remaining."
            }
        }
    }

    fun onDigitClick(digit: String) {
        if (isLockedOut) return
        if (pinInput.length < 5) {
            val newInput = pinInput + digit
            pinInput = newInput
            if (newInput.length == 5) {
                handlePinSubmit(newInput)
            }
        }
    }

    fun onDeleteClick() {
        if (pinInput.isNotEmpty()) {
            pinInput = pinInput.dropLast(1)
        }
    }

    fun onClearClick() {
        pinInput = ""
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("pin_unlock_screen"),
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(28.dp))

                // Avatar Circle
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(RoyalBlue),
                    contentAlignment = Alignment.Center
                ) {
                    if (!userProfile?.photoUri.isNullOrBlank()) {
                        coil.compose.AsyncImage(
                            model = userProfile!!.photoUri,
                            contentDescription = "Profile Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = (userProfile?.name?.take(1) ?: "C").uppercase(),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 30.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Welcome Back,",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = userProfile?.name?.ifBlank { "User" } ?: "User",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isLockedOut) "Account temporarily locked" else "Enter your 5-digit PIN to unlock",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "5-Digit PIN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Masked PIN Card (Filled RoyalBlue dots & Grey outline circles)
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 0 until 5) {
                                val isFilled = i < pinInput.length
                                if (isFilled) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(RoyalBlue)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, Color(0xFF94A3B8), CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }

                if (isLockedOut) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Try again in ${lockoutSeconds}s",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                } else if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Completely hidden text field with PasswordVisualTransformation so password can never be seen
                androidx.compose.foundation.text.BasicTextField(
                    value = pinInput,
                    onValueChange = { input ->
                        if (!isLockedOut) {
                            val digits = input.filter { it.isDigit() }.take(5)
                            pinInput = digits
                            if (pinInput.length == 5) {
                                handlePinSubmit(pinInput)
                            }
                        }
                    },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    modifier = Modifier
                        .size(1.dp)
                        .alpha(0.001f)
                        .testTag("pin_unlock_input")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Rounded Numeric Keypad
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val numRows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("BIO", "0", "DEL")
                )

                for (row in numRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (key in row) {
                            val isSpecial = key == "BIO" || key == "DEL"
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp)
                                    .clickable(enabled = !isLockedOut) {
                                        when (key) {
                                            "BIO" -> triggerBiometricPrompt()
                                            "DEL" -> onDeleteClick()
                                            else -> onDigitClick(key)
                                        }
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSpecial) Color(0xFFEFF6FF) else Color.White
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSpecial) Color(0xFFDBEAFE) else Color(0xFFF1F5F9)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (isSpecial) 0.dp else 1.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    when (key) {
                                        "DEL" -> Icon(
                                            Icons.AutoMirrored.Filled.Backspace,
                                            contentDescription = "Delete",
                                            tint = RoyalBlue,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        "BIO" -> Icon(
                                            Icons.Default.Fingerprint,
                                            contentDescription = "Biometric",
                                            tint = RoyalBlue,
                                            modifier = Modifier.size(26.dp)
                                        )
                                        else -> Text(
                                            text = key,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

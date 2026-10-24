package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.AuthManager
import com.example.model.AppLanguage
import com.example.ui.theme.ElectricBlue
import kotlinx.coroutines.launch

@Composable
fun LoginSignUpScreen(
    authManager: AuthManager,
    onLoginSuccess: () -> Unit,
    onSignUpSuccess: () -> Unit,
    initialLanguage: AppLanguage = AppLanguage.ENGLISH
) {
    var isSignUp by remember { mutableStateOf(false) }
    var currentLang by remember { mutableStateOf(initialLanguage) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val isFrench = currentLang == AppLanguage.FRENCH

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("login_signup_screen"),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Language Switcher Pill in Top Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    onClick = {
                        currentLang = if (isFrench) AppLanguage.ENGLISH else AppLanguage.FRENCH
                    },
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Text(
                        text = if (isFrench) "🇺🇸 English" else "🇫🇷 Français",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isSignUp) {
                    if (isFrench) "Créer un compte" else "Create Account"
                } else {
                    if (isFrench) "Bon retour" else "Welcome Back"
                },
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = if (isFrench) {
                    "Stockage 100% hors ligne. Vos transactions et identifiants ne quittent jamais cet appareil."
                } else {
                    "100% On-Device Auth & Storage. Your transactions and credentials never leave this device."
                },
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            // Mode Selector Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (!isSignUp) ElectricBlue else Color.Transparent)
                        .clickable {
                            isSignUp = false
                            errorMessage = null
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (isFrench) "Se connecter" else "Sign In",
                        color = if (!isSignUp) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (isSignUp) ElectricBlue else Color.Transparent)
                        .clickable {
                            isSignUp = true
                            errorMessage = null
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (isFrench) "Créer compte" else "Sign Up",
                        color = if (isSignUp) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isSignUp) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text(if (isFrench) "Nom complet" else "Full Name") },
                    placeholder = { Text(if (isFrench) "ex. Alex Morgan" else "e.g. Alex Morgan") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("full_name_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(if (isFrench) "Adresse email" else "Email Address") },
                placeholder = { Text("alex@example.com") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("email_input"),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(if (isFrench) "Mot de passe" else "Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("password_input"),
                shape = RoundedCornerShape(14.dp)
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    coroutineScope.launch {
                        if (isSignUp) {
                            val result = authManager.signUpWithEmail(email, password, fullName)
                            isLoading = false
                            if (result.isSuccess) {
                                onSignUpSuccess()
                            } else {
                                errorMessage = result.exceptionOrNull()?.message ?: "Sign up failed"
                            }
                        } else {
                            val result = authManager.signInWithEmail(email, password)
                            isLoading = false
                            if (result.isSuccess) {
                                onLoginSuccess()
                            } else {
                                errorMessage = result.exceptionOrNull()?.message ?: "Sign in failed"
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("submit_auth_button"),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        if (isSignUp) {
                            if (isFrench) "Créer un compte" else "Create Account"
                        } else {
                            if (isFrench) "Se connecter" else "Sign In"
                        },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Social / Google Login
            OutlinedButton(
                onClick = {
                    isLoading = true
                    coroutineScope.launch {
                        val gEmail = if (email.contains("@")) email else "google.user@prompttrack.com"
                        val gName = if (fullName.isNotBlank()) fullName else "Google User"
                        val result = authManager.signInWithGoogle(gName, gEmail)
                        isLoading = false
                        if (result.isSuccess) {
                            onLoginSuccess()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("google_login_button"),
                shape = RoundedCornerShape(999.dp)
            ) {
                Text(
                    if (isFrench) "Continuer avec Google" else "Continue with Google",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

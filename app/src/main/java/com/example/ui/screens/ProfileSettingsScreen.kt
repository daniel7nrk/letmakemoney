package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProfileEntity
import com.example.model.Country
import com.example.ui.theme.RoyalBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    userProfile: UserProfileEntity?,
    selectedCountry: Country,
    isScamFilteringEnabled: Boolean = true,
    onSelectCountry: (Country) -> Unit,
    onToggleScamFiltering: (Boolean) -> Unit = {},
    onUpdateLockMode: (String) -> Unit,
    onSavePin: (String) -> Unit,
    onVerifyPin: (String) -> Boolean,
    onNavigateToCategoryManagement: () -> Unit,
    onSignOut: () -> Unit,
    onBack: () -> Unit
) {
    var showCountryDialog by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }
    var pendingCountry by remember { mutableStateOf<Country?>(null) }
    var pushNotificationsEnabled by remember { mutableStateOf(userProfile?.pushNotificationsEnabled ?: true) }

    val currentLockMode = userProfile?.lockMode ?: "PIN"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("profile_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .testTag("profile_settings_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Profile Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
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
                                text = (userProfile?.name?.take(1) ?: "P").uppercase(),
                                color = Color.White,
                                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = userProfile?.name?.ifBlank { "PromptTrack User" } ?: "PromptTrack User",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = userProfile?.email?.ifBlank { "on-device@prompttrack.com" } ?: "on-device@prompttrack.com",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Account Settings Section
            Text("Account Settings", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    // Country & Currency Selector
                    SettingsTile(
                        icon = Icons.Default.Public,
                        title = "Country & Currency",
                        subtitle = "${selectedCountry.countryName} (${selectedCountry.currencyCode})",
                        onClick = { showCountryDialog = true }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Language Selector
                    SettingsTile(
                        icon = Icons.Default.Category,
                        title = "Language",
                        subtitle = "Change app language",
                        onClick = { /* App language */ }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Category Management
                    SettingsTile(
                        icon = Icons.Default.Category,
                        title = "Category Management",
                        subtitle = "Manage tags & keyword rules",
                        onClick = onNavigateToCategoryManagement
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Push Notifications Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = RoyalBlue)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Pending Review Reminders", fontWeight = FontWeight.Bold)
                                Text("Local daily notifications", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = pushNotificationsEnabled,
                            onCheckedChange = { pushNotificationsEnabled = it }
                        )
                    }
                }
            }

            // Security Group
            Text("Security & Privacy", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    // Biometric Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = RoyalBlue)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Biometric / Passcode Lock", fontWeight = FontWeight.Bold)
                                Text("Use device fingerprint, face, or passcode prompt", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = currentLockMode == "BIOMETRIC",
                            onCheckedChange = { isChecked ->
                                onUpdateLockMode(if (isChecked) "BIOMETRIC" else "PIN")
                            }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Lock Mode Options
                    Text(
                        "App Lock Mode",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    // Option 1: PIN
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onUpdateLockMode("PIN") }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentLockMode == "PIN",
                            onClick = { onUpdateLockMode("PIN") }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("5-Digit PIN Required", fontWeight = FontWeight.SemiBold)
                            Text("Require 5-digit PIN on app launch", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Option 2: No Lock
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onUpdateLockMode("NONE") }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentLockMode == "NONE",
                            onClick = { onUpdateLockMode("NONE") }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("No Lock (Direct Launch)", fontWeight = FontWeight.SemiBold)
                            Text("Explicitly disable PIN/Biometric prompt on launch", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Change PIN tile
                    SettingsTile(
                        icon = Icons.Default.Password,
                        title = "Change 5-Digit PIN",
                        subtitle = "Update your local security passcode",
                        onClick = { showChangePinDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Outlined Sign Out Button (Matching Image 5)
            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("sign_out_button"),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Sign Out",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFFEF4444)
                )
            }
        }
    }

    if (showCountryDialog) {
        AlertDialog(
            onDismissRequest = { showCountryDialog = false },
            title = { Text("Change Country / Currency") },
            text = {
                Column {
                    Text("Select your primary country. Past transactions will NOT be converted; active currency symbol will update.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Country.entries.forEach { c ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    pendingCountry = c
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (pendingCountry ?: selectedCountry) == c,
                                onClick = { pendingCountry = c }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${c.countryName} (${c.currencyCode})", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingCountry?.let { onSelectCountry(it) }
                        showCountryDialog = false
                    }
                ) {
                    Text("Confirm Change")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCountryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showChangePinDialog) {
        var currentPinText by remember { mutableStateOf("") }
        var newPinText by remember { mutableStateOf("") }
        var confirmPinText by remember { mutableStateOf("") }
        var dialogError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            title = { Text("Change 5-Digit PIN") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = currentPinText,
                        onValueChange = { currentPinText = it.filter { c -> c.isDigit() }.take(5) },
                        label = { Text("Current PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPinText,
                        onValueChange = { newPinText = it.filter { c -> c.isDigit() }.take(5) },
                        label = { Text("New 5-Digit PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPinText,
                        onValueChange = { confirmPinText = it.filter { c -> c.isDigit() }.take(5) },
                        label = { Text("Confirm New 5-Digit PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (dialogError != null) {
                        Text(
                            text = dialogError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!onVerifyPin(currentPinText)) {
                            dialogError = "Current PIN is incorrect."
                        } else if (newPinText.length != 5) {
                            dialogError = "New PIN must be exactly 5 digits."
                        } else if (newPinText != confirmPinText) {
                            dialogError = "New PINs do not match."
                        } else {
                            onSavePin(newPinText)
                            showChangePinDialog = false
                        }
                    }
                ) {
                    Text("Save PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = RoyalBlue)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

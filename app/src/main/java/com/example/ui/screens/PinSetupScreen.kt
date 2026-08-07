package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.RoyalBlue

@Composable
fun PinSetupScreen(
    onPinSaved: (String) -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1: Initial PIN, 2: Confirm PIN
    var firstPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val currentInput = if (step == 1) firstPin else confirmPin

    fun onDigitClick(digit: String) {
        if (currentInput.length < 5) {
            val newInput = currentInput + digit
            if (step == 1) {
                firstPin = newInput
                if (firstPin.length == 5) {
                    step = 2
                    errorMessage = null
                }
            } else {
                confirmPin = newInput
                if (confirmPin.length == 5) {
                    if (confirmPin == firstPin) {
                        onPinSaved(confirmPin)
                    } else {
                        errorMessage = "PINs do not match. Please try again."
                        step = 1
                        firstPin = ""
                        confirmPin = ""
                    }
                }
            }
        }
    }

    fun onDeleteClick() {
        if (step == 1) {
            if (firstPin.isNotEmpty()) firstPin = firstPin.dropLast(1)
        } else {
            if (confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1)
        }
    }

    fun onClearClick() {
        if (step == 1) {
            firstPin = ""
        } else {
            confirmPin = ""
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("pin_setup_screen"),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(RoyalBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Security PIN",
                        tint = RoyalBlue,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (step == 1) "Set 5-Digit Security PIN" else "Confirm Your 5-Digit PIN",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (step == 1)
                        "Create a quick 5-digit passcode to secure your transaction data on device launch."
                    else
                        "Re-enter your 5-digit PIN to confirm.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // PIN Dots Indicator (5 slots)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 5) {
                        val isFilled = i < currentInput.length
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(if (isFilled) RoyalBlue else Color.Transparent)
                                .border(
                                    width = 2.dp,
                                    color = if (isFilled) RoyalBlue else MaterialTheme.colorScheme.outline,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Hidden field for test accessibility or direct text input
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = currentInput,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }.take(5)
                        if (step == 1) {
                            firstPin = digits
                            if (firstPin.length == 5) {
                                step = 2
                                errorMessage = null
                            }
                        } else {
                            confirmPin = digits
                            if (confirmPin.length == 5) {
                                if (confirmPin == firstPin) {
                                    onPinSaved(confirmPin)
                                } else {
                                    errorMessage = "PINs do not match. Please try again."
                                    step = 1
                                    firstPin = ""
                                    confirmPin = ""
                                }
                            }
                        }
                    },
                    label = { Text("5-Digit PIN") },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .testTag("pin_setup_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Keypad
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val numRows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "DEL")
                )

                for (row in numRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(0.85f),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (key in row) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (key) {
                                            "C", "DEL" -> MaterialTheme.colorScheme.surfaceContainerHigh
                                            else -> MaterialTheme.colorScheme.surfaceContainerLow
                                        }
                                    )
                                    .clickable {
                                        when (key) {
                                            "C" -> onClearClick()
                                            "DEL" -> onDeleteClick()
                                            else -> onDigitClick(key)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                when (key) {
                                    "DEL" -> Icon(
                                        Icons.AutoMirrored.Filled.Backspace,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                    "C" -> Text("C", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    else -> Text(key, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (step == 2) {
                    TextButton(
                        onClick = {
                            step = 1
                            firstPin = ""
                            confirmPin = ""
                            errorMessage = null
                        }
                    ) {
                        Text("Start Over", color = RoyalBlue, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

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
import androidx.compose.ui.draw.alpha
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
                                val isFilled = i < currentInput.length
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

                if (errorMessage != null) {
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
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    modifier = Modifier
                        .size(1.dp)
                        .alpha(0.001f)
                        .testTag("pin_setup_input")
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
                    listOf("C", "0", "DEL")
                )

                for (row in numRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (key in row) {
                            val isSpecial = key == "C" || key == "DEL"
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp)
                                    .clickable {
                                        when (key) {
                                            "C" -> onClearClick()
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
                                        "C" -> Text(
                                            "C",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = RoyalBlue
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

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@Composable
fun PinSetupScreenPreview() {
    PinSetupScreen(onPinSaved = {})
}


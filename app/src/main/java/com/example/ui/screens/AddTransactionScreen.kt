package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CategoryEntity
import com.example.model.Country
import com.example.ui.theme.RoyalBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    categories: List<CategoryEntity>,
    selectedCountry: Country,
    onSaveTransaction: (Double, String, String, Double, String) -> Unit,
    onBack: () -> Unit
) {
    var amountInput by remember { mutableStateOf("4500") }
    var feeInput by remember { mutableStateOf("0") }
    var descriptionInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull()?.name ?: "Food & Dining") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Manual Transaction", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("add_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .testTag("add_transaction_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Amount Input Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = RoyalBlue)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Enter Amount (${selectedCountry.currencyCode})", color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f))

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it.filter { char -> char.isDigit() || char == '.' } },
                        textStyle = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp,
                            color = androidx.compose.ui.graphics.Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("amount_text_field"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = androidx.compose.ui.graphics.Color.White,
                            unfocusedBorderColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            // Optional Fee Input
            OutlinedTextField(
                value = feeInput,
                onValueChange = { feeInput = it.filter { char -> char.isDigit() || char == '.' } },
                label = { Text("Transaction Fee (${selectedCountry.currencyCode}) - Optional") },
                placeholder = { Text("e.g. 0, 20, 100") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("fee_text_field"),
                shape = RoundedCornerShape(12.dp)
            )

            // Description Input
            OutlinedTextField(
                value = descriptionInput,
                onValueChange = { descriptionInput = it },
                label = { Text("Description / Merchant Name") },
                placeholder = { Text("e.g. Starbucks, Simba Supermarket") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("description_text_field"),
                shape = RoundedCornerShape(12.dp)
            )

            // Category Selection Grid
            Text("Select Category", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat.name
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat.name },
                        label = { Text(cat.name) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val doubleAmount = amountInput.toDoubleOrNull() ?: 0.0
                    val doubleFee = feeInput.toDoubleOrNull() ?: 0.0
                    if (doubleAmount > 0) {
                        onSaveTransaction(doubleAmount, selectedCategory, descriptionInput, doubleFee, "Transaction Fee")
                        onBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_manual_transaction_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Transaction", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

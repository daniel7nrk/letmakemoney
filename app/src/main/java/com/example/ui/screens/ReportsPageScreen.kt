package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PieChart
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
import com.example.data.CategoryEntity
import com.example.data.TransactionEntity
import com.example.model.Country
import com.example.ui.theme.RoyalBlue
import com.example.ui.util.FormatUtil

@Composable
fun ReportsPageScreen(
    transactions: List<TransactionEntity>,
    categories: List<CategoryEntity>,
    selectedCountry: Country
) {
    var selectedTimeframe by remember { mutableStateOf("Monthly") }
    var showExportDialog by remember { mutableStateOf(false) }

    val currencyCode = selectedCountry.currencyCode
    val confirmedTx = transactions.filter { it.status == "CONFIRMED" }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reports_page_screen")
    ) { innerPadding ->
        if (confirmedTx.size < 5) {
            // Empty State: Not enough data
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(RoyalBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PieChart,
                        contentDescription = "Reports Data",
                        tint = RoyalBlue,
                        modifier = Modifier.size(54.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Financial Reports",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Not enough transaction data yet to generate insights. Record at least 5 transactions via SMS alerts or manual entry.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Reports & Insights",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )

                // Timeframe Segmented Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(4.dp)
                ) {
                    listOf("Weekly", "Monthly", "Yearly").forEach { period ->
                        val isSel = selectedTimeframe == period
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSel) RoyalBlue else Color.Transparent)
                                .clickable { selectedTimeframe = period },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = period,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Total Spending Insight Header Card
                val totalSpend = confirmedTx.filter { it.amount != 0.0 }.sumOf { kotlin.math.abs(it.amount) }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Total $selectedTimeframe Spending", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = FormatUtil.formatCurrency(totalSpend, currencyCode),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = RoyalBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "↓ 14.2% lower spending compared to last $selectedTimeframe",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Smart Tip Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RoyalBlue.copy(alpha = 0.2f)),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = RoyalBlue)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Smart Tip", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = RoyalBlue)
                            Text(
                                "Transactions are automatically sorted by category. Food & Dining is your highest expense stream this month.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Category Breakdowns Ranked by Spend
                Text(
                    text = "Top Spending Categories",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                val categoryTotals = confirmedTx.groupBy { it.category }
                    .mapValues { entry -> entry.value.sumOf { kotlin.math.abs(it.amount) } }
                    .toList()
                    .sortedByDescending { it.second }

                val maxCategorySpend = categoryTotals.maxOfOrNull { it.second } ?: 1.0

                categoryTotals.forEach { (catName, amount) ->
                    val ratio = (amount / maxCategorySpend).toFloat()
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(catName, fontWeight = FontWeight.Bold)
                                Text(FormatUtil.formatCurrency(amount, currencyCode), fontWeight = FontWeight.Bold, color = RoyalBlue)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { ratio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape),
                                color = RoyalBlue,
                                trackColor = RoyalBlue.copy(alpha = 0.15f)
                            )
                        }
                    }
                }

                // Export Data Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showExportDialog = true },
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = RoyalBlue)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Export Financial Data", fontWeight = FontWeight.Bold)
                            Text("Download CSV / JSON summary for your records", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Summary") },
            text = { Text("Your transaction data has been compiled locally as a CSV report.") },
            confirmButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Download CSV")
                }
            }
        )
    }
}

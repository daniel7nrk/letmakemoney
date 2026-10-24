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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import android.content.Intent
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
    val context = LocalContext.current
    var selectedTimeframe by remember { mutableStateOf("Monthly") }
    var showExportDialog by remember { mutableStateOf(false) }

    val currencyCode = selectedCountry.currencyCode
    val confirmedTx = transactions.filter { it.status == "CONFIRMED" }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reports_page_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Reports & Insights",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                ),
                color = Color(0xFF0F172A)
            )

            // Timeframe Segmented Switch (Weekly / Monthly / Yearly)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(4.dp)
            ) {
                listOf("Weekly", "Monthly", "Yearly").forEach { period ->
                    val isSel = selectedTimeframe == period
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSel) RoyalBlue else Color.Transparent)
                            .clickable { selectedTimeframe = period },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = period,
                            color = if (isSel) Color.White else Color(0xFF64748B),
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Total Monthly Spending Card
            val totalSpend = confirmedTx.filter { it.amount != 0.0 }.sumOf { kotlin.math.abs(it.amount) }
            val totalFees = confirmedTx.sumOf { it.fee }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Text(
                        "Total $selectedTimeframe Spending",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = FormatUtil.formatCurrency(totalSpend, currencyCode),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 34.sp
                        ),
                        color = RoyalBlue
                    )
                }
            }

            // Total Transaction Fees Card (Tracked separately with its own category)
            if (totalFees > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Total $selectedTimeframe Transaction Fees",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF92400E)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = FormatUtil.formatCurrency(totalFees, currencyCode),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                ),
                                color = Color(0xFFB45309)
                            )
                        }
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Category: Transaction Fees",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF92400E),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Smart Tip Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDBEAFE)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = RoyalBlue,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Smart Tip",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = RoyalBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Transactions are automatically sorted by category. Review your categories to keep reports accurate.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }

            // Top Spending Categories
            Text(
                text = "Top Spending Categories",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = Color(0xFF0F172A)
            )

            val categoryTotals = confirmedTx.groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { kotlin.math.abs(it.amount) } }
                .toList()
                .sortedByDescending { it.second }

            val maxCategorySpend = (categoryTotals.maxOfOrNull { it.second } ?: 1.0).coerceAtLeast(1.0)

            if (categoryTotals.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No categorized expenses yet", color = Color(0xFF64748B))
                    }
                }
            } else {
                categoryTotals.forEach { (catName, amount) ->
                    val ratio = (amount / maxCategorySpend).toFloat()
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    catName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    FormatUtil.formatCurrency(amount, currencyCode),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = RoyalBlue
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { ratio.coerceIn(0.05f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = RoyalBlue,
                                trackColor = Color(0xFFF1F5F9)
                            )
                        }
                    }
                }
            }

            // Export Financial Data Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showExportDialog = true },
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = null,
                        tint = RoyalBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Export Financial Data",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Download CSV / JSON summary for your records",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B)
                        )
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Financial Data") },
            text = {
                Text(
                    "Export " + transactions.size + " transactions compiled as a CSV or JSON report for your records, accounting, or backup."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showExportDialog = false
                    try {
                        val csvHeader = "ID,Date,Merchant,Category,Source,Status,Amount,Currency,Fee,FeeCategory\n"
                        val csvBody = transactions.joinToString("\n") { t ->
                            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(t.dateEpochMillis))
                            val cleanMerchant = t.merchant.replace("\"", "\"\"")
                            val cleanCat = t.category.replace("\"", "\"\"")
                            val cleanSource = t.source.replace("\"", "\"\"")
                            val cleanStatus = t.status.replace("\"", "\"\"")
                            val cleanFeeCat = t.feeCategory.replace("\"", "\"\"")
                            "\"" + t.id + "\",\"" + dateStr + "\",\"" + cleanMerchant + "\",\"" + cleanCat + "\",\"" + cleanSource + "\",\"" + cleanStatus + "\"," + t.amount + ",\"" + t.currency + "\"," + t.fee + ",\"" + cleanFeeCat + "\""
                        }
                        val fullCsv = "\uFEFF" + csvHeader + csvBody

                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(Intent.EXTRA_SUBJECT, "PromptPay Financial Report (" + currencyCode + ")")
                            putExtra(Intent.EXTRA_TEXT, fullCsv)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Export Financial Data (CSV)"))
                        Toast.makeText(context, "Exporting " + transactions.size + " transactions...", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Export failed: " + e.message, Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Export CSV")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showExportDialog = false
                    try {
                        val json = buildString {
                            append("[\n")
                            transactions.forEachIndexed { index, t ->
                                val dateStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date(t.dateEpochMillis))
                                val cleanMerchant = t.merchant.replace("\"", "\\\"")
                                val cleanCat = t.category.replace("\"", "\\\"")
                                val cleanSource = t.source.replace("\"", "\\\"")
                                val cleanStatus = t.status.replace("\"", "\\\"")
                                val cleanFeeCat = t.feeCategory.replace("\"", "\\\"")
                                append("  {\n")
                                append("    \"id\": \"" + t.id + "\",\n")
                                append("    \"date\": \"" + dateStr + "\",\n")
                                append("    \"merchant\": \"" + cleanMerchant + "\",\n")
                                append("    \"category\": \"" + cleanCat + "\",\n")
                                append("    \"source\": \"" + cleanSource + "\",\n")
                                append("    \"status\": \"" + cleanStatus + "\",\n")
                                append("    \"amount\": " + t.amount + ",\n")
                                append("    \"currency\": \"" + t.currency + "\",\n")
                                append("    \"fee\": " + t.fee + ",\n")
                                append("    \"feeCategory\": \"" + cleanFeeCat + "\"\n")
                                append("  }" + (if (index < transactions.size - 1) "," else "") + "\n")
                            }
                            append("]\n")
                        }

                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/json"
                            putExtra(Intent.EXTRA_SUBJECT, "PromptPay JSON Backup (" + currencyCode + ")")
                            putExtra(Intent.EXTRA_TEXT, json)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Export JSON Backup"))
                        Toast.makeText(context, "Exporting JSON backup...", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Export failed: " + e.message, Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Export JSON")
                }
            }
        )
    }
}

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CategoryEntity
import com.example.data.TransactionEntity
import com.example.model.Country
import com.example.ui.theme.RoyalBlue
import com.example.ui.util.FormatUtil

data class DaySpendData(
    val dayName: String,
    val fullDateStr: String,
    val dayStartMillis: Long,
    val dayEndMillis: Long,
    val totalAmount: Double
)

@Composable
fun TransactionsPageScreen(
    transactions: List<TransactionEntity>,
    categories: List<CategoryEntity>,
    selectedCountry: Country,
    isLoadingOlder: Boolean = false,
    onLoadOlderTransactions: () -> Unit = {},
    onTransactionClick: (Long) -> Unit,
    onAddTransactionClick: () -> Unit
) {
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var selectedDayIndex by remember { mutableStateOf<Int?>(null) }

    val currencyCode = selectedCountry.currencyCode

    // Calculate 7-day spending window ending today
    val daysData = remember(transactions) {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)

        val todayStart = cal.timeInMillis
        val dayMillis = 86400000L

        val dayFormat = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
        val fullDateFormat = java.text.SimpleDateFormat("EEE, MMM d", java.util.Locale.getDefault())

        val list = mutableListOf<DaySpendData>()
        for (i in 6 downTo 0) {
            val start = todayStart - (i * dayMillis)
            val end = start + dayMillis
            val dateObj = java.util.Date(start)

            val totalForDay = transactions
                .filter { it.status == "CONFIRMED" && it.dateEpochMillis in start until end }
                .sumOf { kotlin.math.abs(it.amount) }

            list.add(
                DaySpendData(
                    dayName = dayFormat.format(dateObj),
                    fullDateStr = fullDateFormat.format(dateObj),
                    dayStartMillis = start,
                    dayEndMillis = end,
                    totalAmount = totalForDay
                )
            )
        }
        list
    }

    // Dynamic average daily spend calculation from user history
    val avgDailySpend = remember(transactions) {
        val confirmedTx = transactions.filter { it.status == "CONFIRMED" }
        val distinctDaysCount = confirmedTx.map { tx ->
            val cal = java.util.Calendar.getInstance()
            cal.timeInMillis = tx.dateEpochMillis
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.distinct().size

        val totalSum = confirmedTx.sumOf { kotlin.math.abs(it.amount) }
        if (distinctDaysCount > 0) totalSum / distinctDaysCount else 0.0
    }

    val selectedDay = selectedDayIndex?.let { daysData.getOrNull(it) }

    val filteredTransactions = remember(transactions, selectedCategoryFilter, selectedDayIndex, daysData) {
        transactions.filter { tx ->
            val isConfirmed = tx.status == "CONFIRMED"
            val matchesCategory = (selectedCategoryFilter == "All" || tx.category == selectedCategoryFilter)
            val matchesDay = if (selectedDay != null) {
                tx.dateEpochMillis in selectedDay.dayStartMillis until selectedDay.dayEndMillis
            } else true

            isConfirmed && matchesCategory && matchesDay
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransactionClick,
                containerColor = RoyalBlue,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_transaction")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .testTag("transactions_page_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                Text(
                    text = "Transactions",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Custom Weekly Spending Bar Chart with Spend Intensity Colors
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Weekly Overview", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                if (avgDailySpend > 0) {
                                    Text(
                                        text = "Avg daily: ${FormatUtil.formatCurrency(avgDailySpend, currencyCode)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text("Last 7 Days", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val maxVal = daysData.maxOfOrNull { it.totalAmount }?.coerceAtLeast(1.0) ?: 1.0

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            daysData.forEachIndexed { idx, day ->
                                val isSelected = selectedDayIndex == idx
                                val heightRatio = if (maxVal > 0) (day.totalAmount / maxVal).toFloat() else 0f

                                // Determine spend intensity color
                                val barColor = when {
                                    day.totalAmount <= 0.0 -> Color(0xFFD1D5DB) // Neutral gray for no spend
                                    avgDailySpend <= 0.0 -> Color(0xFF10B981)   // Default green
                                    day.totalAmount > 1.15 * avgDailySpend -> Color(0xFFEF4444) // Red: notably above average
                                    day.totalAmount >= 0.85 * avgDailySpend -> Color(0xFFF59E0B) // Yellow: close to average
                                    else -> Color(0xFF10B981) // Green: below average
                                }

                                val barAlpha = if (selectedDayIndex == null || isSelected) 1f else 0.35f

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            selectedDayIndex = if (selectedDayIndex == idx) null else idx
                                        }
                                        .padding(vertical = 4.dp)
                                        .testTag("chart_bar_$idx")
                                ) {
                                    if (day.totalAmount > 0) {
                                        Text(
                                            text = if (day.totalAmount >= 1000) "${(day.totalAmount / 1000).toInt()}k" else "${day.totalAmount.toInt()}",
                                            fontSize = 9.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) RoyalBlue else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                    }

                                    Box(
                                        modifier = Modifier
                                            .width(22.dp)
                                            .fillMaxHeight(heightRatio.coerceAtLeast(0.12f))
                                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                            .background(barColor.copy(alpha = barAlpha))
                                            .then(
                                                if (isSelected) {
                                                    Modifier.border(2.dp, RoyalBlue, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                                } else Modifier
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = day.dayName,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) RoyalBlue else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color(0xFFF3F4F6))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Chart Color Legend
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LegendBadge(color = Color(0xFF10B981), label = "Below Avg")
                            LegendBadge(color = Color(0xFFF59E0B), label = "Around Avg")
                            LegendBadge(color = Color(0xFFEF4444), label = "Above Avg")
                            LegendBadge(color = Color(0xFFD1D5DB), label = "No Spend")
                        }
                    }
                }
            }

            // Selected Day Filter Banner
            if (selectedDay != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("day_filter_active_card"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = RoyalBlue.copy(alpha = 0.08f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RoyalBlue.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FilterList, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Filtered by: ${selectedDay.fullDateStr}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = RoyalBlue
                                    )
                                    Text(
                                        text = "Day total: ${FormatUtil.formatCurrency(selectedDay.totalAmount, currencyCode)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            TextButton(
                                onClick = { selectedDayIndex = null },
                                modifier = Modifier.testTag("clear_day_filter_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp), tint = RoyalBlue)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Clear", color = RoyalBlue, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Filter Chips Row
            item {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Category Filters", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = selectedCategoryFilter == "All",
                                onClick = { selectedCategoryFilter = "All" },
                                label = { Text("All Categories") }
                            )
                        }
                        items(categories) { cat ->
                            FilterChip(
                                selected = selectedCategoryFilter == cat.name,
                                onClick = { selectedCategoryFilter = cat.name },
                                label = { Text(cat.name) }
                            )
                        }
                    }
                }
            }

            // Transaction Items or Empty State
            if (filteredTransactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No transactions found for this selection",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (selectedDayIndex != null || selectedCategoryFilter != "All") {
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = {
                                    selectedDayIndex = null
                                    selectedCategoryFilter = "All"
                                }) {
                                    Text("Reset Filters")
                                }
                            }
                        }
                    }
                }
            } else {
                items(filteredTransactions, key = { it.id }) { tx ->
                    TransactionCardItem(
                        transaction = tx,
                        currencyCode = currencyCode,
                        onClick = { onTransactionClick(tx.id) }
                    )
                }
            }

            if (transactions.isNotEmpty()) {
                item {
                    OutlinedButton(
                        onClick = onLoadOlderTransactions,
                        enabled = !isLoadingOlder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .testTag("load_older_transactions_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoadingOlder) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = RoyalBlue
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Fetching older records...")
                        } else {
                            Text("Load Older Transactions")
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun LegendBadge(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

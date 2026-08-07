package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Warning
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
import com.example.data.AppNotificationEntity
import com.example.data.ScamAlertEntity
import com.example.data.TransactionEntity
import com.example.ui.theme.RoyalBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    allNotifications: List<AppNotificationEntity> = emptyList(),
    scamAlerts: List<ScamAlertEntity> = emptyList(),
    pendingTransactions: List<TransactionEntity> = emptyList(),
    onNavigateToTransactionDetail: (Long) -> Unit = {},
    onNavigateToPendingReview: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onOverrideScamAlert: (ScamAlertEntity) -> Unit = {},
    onDeleteScamAlert: (ScamAlertEntity) -> Unit = {},
    onDeleteNotification: (AppNotificationEntity) -> Unit = {},
    onClearAllNotifications: () -> Unit = {},
    onTriggerPendingReminder: () -> Unit = {},
    onTriggerReportReady: () -> Unit = {},
    onTriggerNewLogin: () -> Unit = {},
    onBack: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedScamAlertForDetail by remember { mutableStateOf<ScamAlertEntity?>(null) }

    // Merge persisted notifications and active scam alerts cleanly
    val combinedList = remember(allNotifications, scamAlerts, pendingTransactions) {
        val list = mutableListOf<AppNotificationEntity>()

        // 1. Add all persisted DB notifications
        list.addAll(allNotifications)

        // 2. Add active scam alerts if not already represented in persisted notifications
        scamAlerts.forEach { scam ->
            if (list.none { it.scamAlertId == scam.id }) {
                list.add(
                    AppNotificationEntity(
                        id = 100000L + scam.id,
                        title = "⚠️ Possible Scam Detected",
                        message = "From ${scam.sender}: ${scam.primaryReason}",
                        timestamp = "Warning",
                        isUnread = true,
                        type = "Scam Alerts",
                        scamAlertId = scam.id,
                        targetRoute = "scam_alerts"
                    )
                )
            }
        }

        // 3. Add pending transaction notifications if not represented
        pendingTransactions.forEach { tx ->
            if (list.none { it.transactionId == tx.id }) {
                val formattedAmount = String.format("%,.0f %s", tx.amount, tx.currency)
                list.add(
                    AppNotificationEntity(
                        id = 200000L + tx.id,
                        title = "Pending Review: ${tx.merchant}",
                        message = "Transaction of $formattedAmount needs your description & category review.",
                        timestamp = "Action Needed",
                        isUnread = true,
                        type = "Pending Review",
                        transactionId = tx.id,
                        targetRoute = "transaction_detail"
                    )
                )
            }
        }

        list.sortedByDescending { it.dateEpochMillis }
    }

    val filteredList = combinedList.filter { notif ->
        when (selectedFilter) {
            "All" -> true
            "Scam Alerts" -> notif.type == "Scam Alerts" || notif.scamAlertId != null
            "Transactions" -> notif.type == "Transactions" || notif.type == "Pending Review"
            "Pending Review" -> notif.type == "Pending Review"
            "Reports" -> notif.type == "Reports"
            "Security" -> notif.type == "Security"
            else -> notif.type == selectedFilter
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("notifications_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (combinedList.isNotEmpty()) {
                        IconButton(
                            onClick = onClearAllNotifications,
                            modifier = Modifier.testTag("clear_all_notifications_button")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear All Notifications")
                        }
                    }
                }
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .testTag("notifications_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Testing / Trigger Bar for Notification Types
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Trigger System & In-App Alerts (Test Suite):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item {
                            AssistChip(
                                onClick = onTriggerPendingReminder,
                                label = { Text("Pending Review", fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                modifier = Modifier.testTag("trigger_pending_reminder")
                            )
                        }
                        item {
                            AssistChip(
                                onClick = onTriggerReportReady,
                                label = { Text("Monthly Report", fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                modifier = Modifier.testTag("trigger_report_ready")
                            )
                        }
                        item {
                            AssistChip(
                                onClick = onTriggerNewLogin,
                                label = { Text("New Login", fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                modifier = Modifier.testTag("trigger_new_login")
                            )
                        }
                    }
                }
            }

            // Filter Chips Row
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Scam Alerts", "Transactions", "Pending Review", "Reports", "Security").forEach { f ->
                    item {
                        FilterChip(
                            selected = selectedFilter == f,
                            onClick = { selectedFilter = f },
                            label = { Text(f) }
                        )
                    }
                }
            }

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(RoyalBlue.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = RoyalBlue,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "No notifications in '$selectedFilter'",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "You're all caught up! New transaction reviews, scam alerts, and reports will appear here.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredList, key = { "${it.type}_${it.id}" }) { item ->
                        val isScamItem = item.type == "Scam Alerts" || item.scamAlertId != null
                        val associatedScam = if (item.scamAlertId != null) {
                            scamAlerts.find { it.id == item.scamAlertId }
                        } else null

                        val effectiveScam = associatedScam ?: if (isScamItem) {
                            val extractedSender = if (item.message.startsWith("From ")) {
                                val colon = item.message.indexOf(":")
                                if (colon > 5) item.message.substring(5, colon).trim() else "SMS Sender"
                            } else "SMS Sender"
                            ScamAlertEntity(
                                id = item.scamAlertId ?: item.id,
                                sender = extractedSender,
                                body = item.message,
                                dateEpochMillis = item.dateEpochMillis,
                                primaryReason = item.message
                            )
                        } else null

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (effectiveScam != null) {
                                        selectedScamAlertForDetail = effectiveScam
                                    } else if (item.transactionId != null) {
                                        onNavigateToTransactionDetail(item.transactionId)
                                    } else when (item.targetRoute) {
                                        "transaction_detail" -> onNavigateToPendingReview()
                                        "pending_review" -> onNavigateToPendingReview()
                                        "reports" -> onNavigateToReports()
                                        "security" -> onNavigateToProfile()
                                        "scam_alerts" -> {
                                            val s = scamAlerts.firstOrNull() ?: effectiveScam
                                            if (s != null) selectedScamAlertForDetail = s
                                        }
                                        else -> {
                                            when (item.type) {
                                                "Transactions" -> onNavigateToPendingReview()
                                                "Pending Review" -> onNavigateToPendingReview()
                                                "Reports" -> onNavigateToReports()
                                                "Security" -> onNavigateToProfile()
                                            }
                                        }
                                    }
                                }
                                .testTag("notification_item_${item.id}"),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isScamItem) Color(0xFFEF5350) else Color(0xFFE5E7EB)
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isScamItem)
                                    Color(0xFFFFF5F5)
                                else if (item.isUnread)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                                else
                                    Color.White
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(if (isScamItem) Color(0xFFFFEBEE) else RoyalBlue.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when {
                                                isScamItem -> Icons.Default.Warning
                                                item.type == "Security" -> Icons.Default.Lock
                                                item.type == "Reports" -> Icons.Default.Info
                                                item.type == "Pending Review" -> Icons.Default.RateReview
                                                else -> Icons.Default.Notifications
                                            },
                                            contentDescription = null,
                                            tint = if (isScamItem) Color(0xFFD32F2F) else RoyalBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = item.title,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = if (isScamItem) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f, fill = false),
                                                maxLines = 1
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = item.timestamp,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isScamItem) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = item.message,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Action / Delete icon button
                                    IconButton(
                                        onClick = {
                                            if (effectiveScam != null) {
                                                onDeleteScamAlert(effectiveScam)
                                            } else {
                                                onDeleteNotification(item)
                                            }
                                        },
                                        modifier = Modifier.testTag("delete_notification_${item.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete notification",
                                            tint = Color.Gray.copy(alpha = 0.6f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                // Quick Actions for Scam Alert Items
                                if (isScamItem && effectiveScam != null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedButton(
                                            onClick = { onDeleteScamAlert(effectiveScam) },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCDD2)),
                                            modifier = Modifier
                                                .height(34.dp)
                                                .testTag("scam_card_delete_button_${effectiveScam.id}")
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Delete", fontSize = 12.sp)
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Button(
                                            onClick = { onOverrideScamAlert(effectiveScam) },
                                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                            modifier = Modifier
                                                .height(34.dp)
                                                .testTag("scam_card_override_button_${effectiveScam.id}")
                                        ) {
                                            Text("Real Transaction", fontSize = 12.sp)
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

    if (selectedScamAlertForDetail != null) {
        val scam = selectedScamAlertForDetail!!
        AlertDialog(
            onDismissRequest = { selectedScamAlertForDetail = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Scam Warning",
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "⚠️ Possible Scam Detected",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Sender: ${scam.sender}",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCDD2)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Message Content:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = scam.body,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF333333)
                            )
                        }
                    }
                    Text(
                        text = "Why flagged: ${scam.primaryReason}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (scam.detectedSignals.isNotBlank()) {
                        Text(
                            text = "Detected Signals: ${scam.detectedSignals}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFD32F2F)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onOverrideScamAlert(scam)
                        selectedScamAlertForDetail = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                    modifier = Modifier.testTag("scam_override_button")
                ) {
                    Text("This is a real transaction")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            onDeleteScamAlert(scam)
                            selectedScamAlertForDetail = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFD32F2F)),
                        modifier = Modifier.testTag("scam_delete_dialog_button")
                    ) {
                        Text("Delete")
                    }
                    TextButton(onClick = { selectedScamAlertForDetail = null }) {
                        Text("Close")
                    }
                }
            }
        )
    }
}

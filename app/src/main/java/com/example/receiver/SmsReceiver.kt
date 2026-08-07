package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.example.data.AppDatabase
import com.example.data.AppNotificationEntity
import com.example.data.AppRepository
import com.example.data.TransactionEntity
import com.example.notification.NotificationHelper
import com.example.parser.SmsParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION ||
            action == "android.provider.Telephony.SMS_RECEIVED" ||
            action == Telephony.Sms.Intents.SMS_DELIVER_ACTION ||
            action == null
        ) {
            val database = AppDatabase.getDatabase(context)
            val repository = AppRepository(database)
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val userProfile = database.userProfileDao().getUserProfileDirect()
                    val userCurrency = userProfile?.currencyCode ?: "RWF"
                    val categories = repository.getCategoriesList()

                    val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                    if (!messages.isNullOrEmpty()) {
                        for (message in messages) {
                            val sender = message.originatingAddress ?: "SMS"
                            val body = message.messageBody ?: continue

                            processIncomingSms(
                                context = context,
                                repository = repository,
                                sender = sender,
                                body = body,
                                categories = categories,
                                userCurrency = userCurrency
                            )
                        }
                    } else {
                        val body = intent.getStringExtra("body")
                            ?: intent.getStringExtra("message")
                            ?: intent.getStringExtra("sms_body")
                        val sender = intent.getStringExtra("sender")
                            ?: intent.getStringExtra("originatingAddress")
                            ?: "Mobile Money"

                        if (!body.isNullOrBlank()) {
                            processIncomingSms(
                                context = context,
                                repository = repository,
                                sender = sender,
                                body = body,
                                categories = categories,
                                userCurrency = userCurrency
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    private suspend fun processIncomingSms(
        context: Context,
        repository: AppRepository,
        sender: String,
        body: String,
        categories: List<com.example.data.CategoryEntity>,
        userCurrency: String
    ) {
        val recentTransactions = repository.getAllTransactionsList()
        val recentScams = repository.getActiveScamAlertsList()

        val currentBalance = recentTransactions.sumOf { tx ->
            if (tx.category.equals("Salary", ignoreCase = true) || tx.amount < 0) {
                Math.abs(tx.amount)
            } else {
                -tx.amount
            }
        }

        val scamDetection = com.example.detector.ScamDetector.detectScam(
            sender = sender,
            body = body,
            recentTransactions = recentTransactions,
            recentScams = recentScams,
            currentAccountBalance = currentBalance
        )

        if (scamDetection.isScam) {
            val scamAlert = com.example.data.ScamAlertEntity(
                sender = sender,
                body = body,
                dateEpochMillis = System.currentTimeMillis(),
                primaryReason = scamDetection.primaryReason,
                detectedSignals = scamDetection.detectedSignals.joinToString(", ")
            )
            val scamId = repository.insertScamAlert(scamAlert)

            // Persist in-app notification
            val notif = AppNotificationEntity(
                title = "⚠️ Possible Scam Detected",
                message = "From $sender: ${scamDetection.primaryReason}",
                type = "Scam Alerts",
                scamAlertId = scamId,
                targetRoute = "scam_alerts"
            )
            repository.insertNotification(notif)

            // System tray notification
            NotificationHelper.showScamAlertNotification(
                context = context,
                scamAlertId = scamId,
                sender = sender,
                reason = scamDetection.primaryReason
            )
            return
        }

        val parsed = SmsParser.parseSms(
            sender = sender,
            body = body,
            categories = categories,
            userCurrency = userCurrency
        )

        if (parsed != null) {
            val transaction = TransactionEntity(
                amount = parsed.amount,
                currency = parsed.currency,
                dateEpochMillis = System.currentTimeMillis(),
                description = parsed.description,
                merchant = parsed.merchant,
                category = parsed.category,
                source = "SMS",
                status = parsed.status,
                paymentMethod = sender
            )
            val insertedId = repository.insertTransaction(transaction)

            val formattedAmount = String.format("%,.0f %s", transaction.amount, transaction.currency)
            val notif = AppNotificationEntity(
                title = "New Transaction Detected",
                message = "Amount: $formattedAmount at ${transaction.merchant}. Tap to add description & review.",
                type = "Transactions",
                transactionId = insertedId,
                targetRoute = "transaction_detail"
            )
            repository.insertNotification(notif)

            NotificationHelper.showNewTransactionNotification(
                context = context,
                transactionId = insertedId,
                merchant = transaction.merchant,
                amount = transaction.amount,
                currency = transaction.currency
            )
        }
    }
}

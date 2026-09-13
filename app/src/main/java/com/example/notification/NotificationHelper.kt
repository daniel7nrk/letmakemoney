package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

object NotificationHelper {

    const val CHANNEL_ID = "promptpay_notifications"
    const val CHANNEL_NAME = "PromptPay Reminders"

    const val IMMEDIATE_CHANNEL_ID = "promptpay_immediate_transactions"
    const val IMMEDIATE_CHANNEL_NAME = "Transaction Alerts"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val reminderChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Local reminders and status alerts for PromptPay"
            }
            notificationManager.createNotificationChannel(reminderChannel)

            val immediateChannel = NotificationChannel(
                IMMEDIATE_CHANNEL_ID,
                IMMEDIATE_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Immediate high-priority transaction alerts when SMS is detected"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(immediateChannel)
        }
    }

    fun showNewTransactionNotification(
        context: Context,
        transactionId: Long,
        merchant: String,
        amount: Double,
        currency: String
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "transaction_detail")
            putExtra("transaction_id", transactionId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            (2000 + transactionId).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val formattedAmount = String.format("%,.0f %s", amount, currency)

        val builder = NotificationCompat.Builder(context, IMMEDIATE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("New Transaction: $merchant")
            .setContentText("Amount: $formattedAmount. Tap to add description & review details.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Amount: $formattedAmount. Tap to add description & review details."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOngoing(false)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify((2000 + transactionId).toInt(), builder.build())
    }

    fun showPendingReviewNotification(context: Context, pendingCount: Int) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "pending_review")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            101,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Pending Review")
            .setContentText("$pendingCount transactions need your review to keep reports accurate.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1001, builder.build())
    }

    fun showNewLoginNotification(context: Context, deviceName: String) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            102,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setContentTitle("New Login Detected")
            .setContentText("A new session was authenticated on $deviceName.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1002, builder.build())
    }

    fun showWeeklyReportNotification(
        context: Context,
        totalSpent: Double,
        currency: String,
        txCount: Int,
        weekLabel: String = "this week"
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "reports")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1004,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val formattedAmount = String.format("%,.0f %s", totalSpent, currency)
        val text = "You spent $formattedAmount across $txCount transactions $weekLabel. Tap to view your breakdown."

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle("📊 Weekly Spending Report")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1004, builder.build())
    }

    fun showMonthlyReportNotification(
        context: Context,
        monthName: String = "This Month",
        totalSpent: Double = 0.0,
        currency: String = "RWF",
        txCount: Int = 0
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "reports")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1003,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val text = if (totalSpent > 0) {
            val formattedAmount = String.format("%,.0f %s", totalSpent, currency)
            "You spent $formattedAmount across $txCount transactions in $monthName. Tap to view your monthly breakdown."
        } else {
            "Your financial report for $monthName is ready to review. Tap to check your summary."
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle("📈 Monthly Spending Report")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1003, builder.build())
    }

    fun cancelTransactionNotification(context: Context, transactionId: Long) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel((2000 + transactionId).toInt())
    }

    fun cancelPendingReviewNotification(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1001)
    }

    fun showScamAlertNotification(
        context: Context,
        scamAlertId: Long,
        sender: String,
        reason: String
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "scam_alerts")
            putExtra("scam_alert_id", scamAlertId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            (5000 + scamAlertId).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, IMMEDIATE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("🚫 Scam Alert Detected")
            .setContentText("Sender: $sender. $reason")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Sender: $sender\n\n$reason\n\nTap to review or mark as real transaction."))
            .setColor(0xFFD32F2F.toInt())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify((5000 + scamAlertId).toInt(), builder.build())
    }

    fun showNeedsReviewNotification(
        context: Context,
        scamAlertId: Long,
        sender: String,
        reason: String
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "scam_alerts")
            putExtra("scam_alert_id", scamAlertId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            (6000 + scamAlertId).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, IMMEDIATE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("⚠️ Transaction Needs Review")
            .setContentText("Sender: $sender. $reason")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Sender: $sender\n\n$reason\n\nTap to review transaction."))
            .setColor(0xFFF57C00.toInt())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify((6000 + scamAlertId).toInt(), builder.build())
    }

    fun cancelScamAlertNotification(context: Context, scamAlertId: Long) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel((5000 + scamAlertId).toInt())
    }

    fun cancelAllSystemNotificationsForAppNotification(context: Context, notification: com.example.data.AppNotificationEntity) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (notification.scamAlertId != null) {
            notificationManager.cancel((5000 + notification.scamAlertId).toInt())
        }
        if (notification.transactionId != null) {
            notificationManager.cancel((2000 + notification.transactionId).toInt())
        }

        when (notification.type) {
            "Pending Review" -> notificationManager.cancel(1001)
            "Security" -> notificationManager.cancel(1002)
            "Reports" -> notificationManager.cancel(1003)
            "Scam Alerts" -> {
                if (notification.scamAlertId != null) {
                    notificationManager.cancel((5000 + notification.scamAlertId).toInt())
                }
            }
        }

        notificationManager.cancel(notification.id.toInt())
        notificationManager.cancel((3000 + notification.id).toInt())
    }

    fun cancelAllNotifications(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }
}

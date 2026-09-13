package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.AppDatabase
import com.example.data.AppNotificationEntity
import com.example.data.AppRepository
import com.example.notification.NotificationHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SpendingReportWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            processReportChecks(context)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    companion object {
        private const val PREFS_NAME = "promptpay_report_prefs"
        private const val KEY_LAST_WEEKLY_REPORT = "last_weekly_report_time"
        private const val KEY_LAST_MONTHLY_REPORT = "last_monthly_report_time"

        private const val SEVEN_DAYS_MILLIS = 7 * 24 * 60 * 60 * 1000L
        private const val THIRTY_DAYS_MILLIS = 30 * 24 * 60 * 60 * 1000L

        suspend fun processReportChecks(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val now = System.currentTimeMillis()

            val lastWeekly = prefs.getLong(KEY_LAST_WEEKLY_REPORT, 0L)
            val lastMonthly = prefs.getLong(KEY_LAST_MONTHLY_REPORT, 0L)

            // Check if at least 7 days passed since last weekly report
            if (lastWeekly == 0L || (now - lastWeekly) >= SEVEN_DAYS_MILLIS) {
                generateAndSendWeeklyReport(context, now)
                prefs.edit().putLong(KEY_LAST_WEEKLY_REPORT, now).apply()
            }

            // Check if at least 30 days passed since last monthly report
            if (lastMonthly == 0L || (now - lastMonthly) >= THIRTY_DAYS_MILLIS) {
                generateAndSendMonthlyReport(context, now)
                prefs.edit().putLong(KEY_LAST_MONTHLY_REPORT, now).apply()
            }
        }

        suspend fun generateAndSendWeeklyReport(context: Context, referenceTime: Long = System.currentTimeMillis()): Double {
            val database = AppDatabase.getDatabase(context)
            val repository = AppRepository(database)
            val userProfile = database.userProfileDao().getUserProfileDirect()
            val currency = userProfile?.currencyCode ?: "RWF"

            val allTransactions = repository.getAllTransactionsList()
            val oneWeekAgo = referenceTime - SEVEN_DAYS_MILLIS

            val weeklyExpenses = allTransactions.filter { tx ->
                tx.dateEpochMillis in oneWeekAgo..referenceTime &&
                !tx.category.equals("Salary", ignoreCase = true) &&
                tx.amount > 0
            }

            val totalSpent = weeklyExpenses.sumOf { it.amount }
            val count = weeklyExpenses.size

            val formattedAmount = String.format("%,.0f %s", totalSpent, currency)
            val message = "You spent $formattedAmount across $count transactions this week. Tap to view your breakdown."

            val notif = AppNotificationEntity(
                title = "Weekly Spending Report",
                message = message,
                type = "Reports",
                targetRoute = "reports"
            )
            repository.insertNotification(notif)

            NotificationHelper.showWeeklyReportNotification(
                context = context,
                totalSpent = totalSpent,
                currency = currency,
                txCount = count,
                weekLabel = "this week"
            )

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putLong(KEY_LAST_WEEKLY_REPORT, referenceTime).apply()

            return totalSpent
        }

        suspend fun generateAndSendMonthlyReport(context: Context, referenceTime: Long = System.currentTimeMillis()): Double {
            val database = AppDatabase.getDatabase(context)
            val repository = AppRepository(database)
            val userProfile = database.userProfileDao().getUserProfileDirect()
            val currency = userProfile?.currencyCode ?: "RWF"

            val allTransactions = repository.getAllTransactionsList()
            val thirtyDaysAgo = referenceTime - THIRTY_DAYS_MILLIS

            val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
            val monthName = monthFormat.format(Date(referenceTime))

            val monthlyExpenses = allTransactions.filter { tx ->
                tx.dateEpochMillis in thirtyDaysAgo..referenceTime &&
                !tx.category.equals("Salary", ignoreCase = true) &&
                tx.amount > 0
            }

            val totalSpent = monthlyExpenses.sumOf { it.amount }
            val count = monthlyExpenses.size

            val formattedAmount = String.format("%,.0f %s", totalSpent, currency)
            val message = "You spent $formattedAmount across $count transactions in $monthName. Tap to view your breakdown."

            val notif = AppNotificationEntity(
                title = "Monthly Spending Report",
                message = message,
                type = "Reports",
                targetRoute = "reports"
            )
            repository.insertNotification(notif)

            NotificationHelper.showMonthlyReportNotification(
                context = context,
                monthName = monthName,
                totalSpent = totalSpent,
                currency = currency,
                txCount = count
            )

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putLong(KEY_LAST_MONTHLY_REPORT, referenceTime).apply()

            return totalSpent
        }
    }
}

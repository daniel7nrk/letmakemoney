package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.ui.AppNavigation
import com.example.ui.MainViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.worker.PendingReviewWorker
import com.example.worker.SpendingReportWorker
import java.util.concurrent.TimeUnit

class MainActivity : FragmentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestSmsAndNotificationPermissions()

        // Schedule WorkManager for pending transaction reviews & spending reports
        schedulePendingReviewWorker()
        scheduleSpendingReportWorker()

        // Background Supabase sync (100% offline-first: runs only if online, never blocks)
        com.example.sync.SupabaseSyncManager.syncIfOnline(applicationContext)

        handleIntent(intent)

        setContent {
            MyApplicationTheme {
                AppNavigation(viewModel = viewModel)
            }
        }
    }

    private fun requestSmsAndNotificationPermissions() {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missingPermissions = permissionsToRequest.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                missingPermissions.toTypedArray(),
                101
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val navigateTo = intent.getStringExtra("navigate_to")
        val txId = intent.getLongExtra("transaction_id", -1L)
        if (!navigateTo.isNullOrBlank()) {
            intent.removeExtra("navigate_to")
            intent.removeExtra("transaction_id")
            viewModel.handleDeepLink(navigateTo, txId)
        }
    }

    private fun schedulePendingReviewWorker() {
        try {
            val workRequest = PeriodicWorkRequestBuilder<PendingReviewWorker>(1, TimeUnit.DAYS).build()
            WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                "pending_review_worker",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun scheduleSpendingReportWorker() {
        try {
            val workRequest = PeriodicWorkRequestBuilder<SpendingReportWorker>(1, TimeUnit.DAYS).build()
            WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                "spending_report_worker",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

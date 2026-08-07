package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.notification.NotificationHelper

class PendingReviewWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.getDatabase(context)
            val repository = AppRepository(database)
            val pending = repository.getPendingTransactionsList()

            if (pending.isNotEmpty()) {
                NotificationHelper.showPendingReviewNotification(context, pending.size)
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}

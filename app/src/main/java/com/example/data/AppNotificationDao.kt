package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppNotificationDao {
    @Query("SELECT * FROM app_notifications ORDER BY dateEpochMillis DESC")
    fun getAllNotifications(): Flow<List<AppNotificationEntity>>

    @Query("SELECT * FROM app_notifications ORDER BY dateEpochMillis DESC")
    suspend fun getAllNotificationsList(): List<AppNotificationEntity>

    @Query("SELECT * FROM app_notifications WHERE id = :id")
    suspend fun getNotificationById(id: Long): AppNotificationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotificationEntity): Long

    @Update
    suspend fun updateNotification(notification: AppNotificationEntity)

    @Query("DELETE FROM app_notifications WHERE id = :id")
    suspend fun deleteNotificationById(id: Long)

    @Query("DELETE FROM app_notifications WHERE scamAlertId = :scamAlertId")
    suspend fun deleteByScamAlertId(scamAlertId: Long)

    @Query("DELETE FROM app_notifications WHERE transactionId = :transactionId")
    suspend fun deleteByTransactionId(transactionId: Long)

    @Query("DELETE FROM app_notifications")
    suspend fun deleteAllNotifications()

    @Query("UPDATE app_notifications SET isUnread = 0 WHERE id = :id")
    suspend fun markAsRead(id: Long)
}

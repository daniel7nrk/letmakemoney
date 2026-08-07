package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScamAlertDao {
    @Query("SELECT * FROM scam_alerts WHERE isOverridden = 0 ORDER BY dateEpochMillis DESC")
    fun getActiveScamAlerts(): Flow<List<ScamAlertEntity>>

    @Query("SELECT * FROM scam_alerts ORDER BY dateEpochMillis DESC")
    fun getAllScamAlerts(): Flow<List<ScamAlertEntity>>

    @Query("SELECT * FROM scam_alerts WHERE isOverridden = 0 ORDER BY dateEpochMillis DESC")
    suspend fun getActiveScamAlertsList(): List<ScamAlertEntity>

    @Query("SELECT * FROM scam_alerts WHERE id = :id")
    suspend fun getScamAlertById(id: Long): ScamAlertEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScamAlert(scamAlert: ScamAlertEntity): Long

    @Update
    suspend fun updateScamAlert(scamAlert: ScamAlertEntity)

    @Query("DELETE FROM scam_alerts WHERE id = :id")
    suspend fun deleteScamAlertById(id: Long)

    @Query("SELECT * FROM scam_alerts WHERE sender = :sender AND body = :body AND dateEpochMillis >= :timeWindowMillis")
    suspend fun findRecentSimilarScams(sender: String, body: String, timeWindowMillis: Long): List<ScamAlertEntity>
}

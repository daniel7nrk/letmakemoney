package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY dateEpochMillis DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY dateEpochMillis DESC")
    suspend fun getAllTransactionsList(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE status = 'PENDING' ORDER BY dateEpochMillis DESC")
    fun getPendingTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE status = 'PENDING' ORDER BY dateEpochMillis DESC")
    suspend fun getPendingTransactionsList(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    @Query("UPDATE transactions SET category = 'Uncategorized', status = 'PENDING' WHERE category = :categoryName")
    suspend fun reassignCategoryToUncategorized(categoryName: String)

    @Query("UPDATE transactions SET category = :newName WHERE category = :oldName")
    suspend fun updateTransactionCategoryName(oldName: String, newName: String)
}

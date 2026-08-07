package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val database: AppDatabase) {
    val allTransactions: Flow<List<TransactionEntity>> = database.transactionDao().getAllTransactions()
    val pendingTransactions: Flow<List<TransactionEntity>> = database.transactionDao().getPendingTransactions()
    val activeScamAlerts: Flow<List<ScamAlertEntity>> = database.scamAlertDao().getActiveScamAlerts()
    val allScamAlerts: Flow<List<ScamAlertEntity>> = database.scamAlertDao().getAllScamAlerts()
    val allNotifications: Flow<List<AppNotificationEntity>> = database.notificationDao().getAllNotifications()
    val allCategories: Flow<List<CategoryEntity>> = database.categoryDao().getAllCategories()
    val userProfile: Flow<UserProfileEntity?> = database.userProfileDao().getUserProfile()

    suspend fun getTransactionById(id: Long): TransactionEntity? {
        return database.transactionDao().getTransactionById(id)
    }

    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        return database.transactionDao().insertTransaction(transaction)
    }

    suspend fun insertTransactions(transactions: List<TransactionEntity>) {
        database.transactionDao().insertTransactions(transactions)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        database.transactionDao().updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        database.transactionDao().deleteTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: Long) {
        database.transactionDao().deleteTransactionById(id)
    }

    suspend fun deleteAllTransactions() {
        database.transactionDao().deleteAllTransactions()
    }

    suspend fun insertCategory(category: CategoryEntity): Long {
        return database.categoryDao().insertCategory(category)
    }

    suspend fun updateCategory(category: CategoryEntity, oldName: String? = null) {
        if (!oldName.isNullOrBlank() && oldName != category.name) {
            database.transactionDao().updateTransactionCategoryName(oldName, category.name)
        }
        database.categoryDao().updateCategory(category)
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        // Reassign transactions under deleted category to "Uncategorized"
        database.transactionDao().reassignCategoryToUncategorized(category.name)
        database.categoryDao().deleteCategory(category)
    }

    suspend fun updateUserProfile(profile: UserProfileEntity) {
        database.userProfileDao().insertOrUpdateProfile(profile)
    }

    suspend fun getCategoriesList(): List<CategoryEntity> {
        return database.categoryDao().getCategoriesList()
    }

    suspend fun getPendingTransactionsList(): List<TransactionEntity> {
        return database.transactionDao().getPendingTransactionsList()
    }

    suspend fun getAllTransactionsList(): List<TransactionEntity> {
        return database.transactionDao().getAllTransactionsList()
    }

    suspend fun getActiveScamAlertsList(): List<ScamAlertEntity> {
        return database.scamAlertDao().getActiveScamAlertsList()
    }

    suspend fun getScamAlertById(id: Long): ScamAlertEntity? {
        return database.scamAlertDao().getScamAlertById(id)
    }

    suspend fun insertScamAlert(scamAlert: ScamAlertEntity): Long {
        return database.scamAlertDao().insertScamAlert(scamAlert)
    }

    suspend fun updateScamAlert(scamAlert: ScamAlertEntity) {
        database.scamAlertDao().updateScamAlert(scamAlert)
    }

    suspend fun deleteScamAlertById(id: Long) {
        database.scamAlertDao().deleteScamAlertById(id)
    }

    suspend fun insertNotification(notification: AppNotificationEntity): Long {
        return database.notificationDao().insertNotification(notification)
    }

    suspend fun updateNotification(notification: AppNotificationEntity) {
        database.notificationDao().updateNotification(notification)
    }

    suspend fun deleteNotificationById(id: Long) {
        database.notificationDao().deleteNotificationById(id)
    }

    suspend fun deleteNotificationsByScamAlertId(scamAlertId: Long) {
        database.notificationDao().deleteByScamAlertId(scamAlertId)
    }

    suspend fun deleteNotificationsByTransactionId(transactionId: Long) {
        database.notificationDao().deleteByTransactionId(transactionId)
    }

    suspend fun deleteAllNotifications() {
        database.notificationDao().deleteAllNotifications()
    }

    suspend fun markNotificationAsRead(id: Long) {
        database.notificationDao().markAsRead(id)
    }
}

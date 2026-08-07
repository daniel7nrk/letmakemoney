package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.AuthManager
import com.example.data.AppDatabase
import com.example.data.AppNotificationEntity
import com.example.data.AppRepository
import com.example.data.CategoryEntity
import com.example.data.TransactionEntity
import com.example.data.UserProfileEntity
import com.example.model.Country
import com.example.notification.NotificationHelper
import com.example.parser.SmsParser
import com.example.auth.PasswordUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository
    val authManager: AuthManager = AuthManager(application)

    val allTransactions: StateFlow<List<TransactionEntity>>
    val pendingTransactions: StateFlow<List<TransactionEntity>>
    val activeScamAlerts: StateFlow<List<com.example.data.ScamAlertEntity>>
    val allNotifications: StateFlow<List<AppNotificationEntity>>
    val categories: StateFlow<List<CategoryEntity>>
    val userProfile: StateFlow<UserProfileEntity?>

    val selectedCountry: MutableStateFlow<Country> = MutableStateFlow(Country.RWANDA)

    val isLoadingOlder: MutableStateFlow<Boolean> = MutableStateFlow(false)

    data class DeepLinkEvent(val destination: String, val transactionId: Long)
    private val _deepLinkEvent = MutableStateFlow<DeepLinkEvent?>(null)
    val deepLinkEvent: StateFlow<DeepLinkEvent?> = _deepLinkEvent

    fun handleDeepLink(destination: String, transactionId: Long) {
        if (destination.isNotBlank()) {
            _deepLinkEvent.value = DeepLinkEvent(destination, transactionId)
        }
    }

    fun clearDeepLink() {
        _deepLinkEvent.value = null
    }

    init {
        val db = AppDatabase.getDatabase(application)
        repository = AppRepository(db)

        allTransactions = repository.allTransactions.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        pendingTransactions = repository.pendingTransactions.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        activeScamAlerts = repository.activeScamAlerts.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allNotifications = repository.allNotifications.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        categories = repository.allCategories.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        userProfile = repository.userProfile.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

        viewModelScope.launch {
            repository.userProfile.collect { profile ->
                if (profile != null) {
                    val country = Country.fromCurrencyCode(profile.currencyCode)
                    selectedCountry.value = country
                }
            }
        }
    }

    fun selectCountry(country: Country) {
        selectedCountry.value = country
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity(id = 1)
            val updated = current.copy(
                countryCode = country.countryName,
                currencyCode = country.currencyCode
            )
            repository.updateUserProfile(updated)
        }
    }

    fun addManualTransaction(
        amount: Double,
        category: String,
        description: String,
        dateEpochMillis: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            val currCode = selectedCountry.value.currencyCode
            val newTx = TransactionEntity(
                amount = amount,
                currency = currCode,
                dateEpochMillis = dateEpochMillis,
                description = description.ifBlank { "Manual Transaction" },
                merchant = description.ifBlank { category },
                category = category,
                source = "MANUAL",
                status = "CONFIRMED",
                isRecurring = false,
                paymentMethod = "Manual Entry"
            )
            repository.insertTransaction(newTx)
        }
    }

    suspend fun getTransactionById(id: Long): TransactionEntity? {
        return repository.getTransactionById(id)
    }

    suspend fun getPendingTransactionsList(): List<TransactionEntity> {
        return repository.getPendingTransactionsList()
    }

    fun categorizePendingTransaction(id: Long, categoryName: String, description: String = "", notes: String = "") {
        viewModelScope.launch {
            val tx = repository.getTransactionById(id)
            if (tx != null) {
                val finalDesc = description.ifBlank { tx.merchant.ifBlank { categoryName } }
                val updated = tx.copy(
                    category = categoryName,
                    description = finalDesc,
                    notes = notes,
                    status = "CONFIRMED"
                )
                repository.updateTransaction(updated)
                repository.deleteNotificationsByTransactionId(id)
                NotificationHelper.cancelTransactionNotification(getApplication(), id)
                val remainingPending = repository.getPendingTransactionsList()
                if (remainingPending.isEmpty()) {
                    NotificationHelper.cancelPendingReviewNotification(getApplication())
                }
            }
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransactionById(id)
            repository.deleteNotificationsByTransactionId(id)
            NotificationHelper.cancelTransactionNotification(getApplication(), id)
            val remainingPending = repository.getPendingTransactionsList()
            if (remainingPending.isEmpty()) {
                NotificationHelper.cancelPendingReviewNotification(getApplication())
            }
        }
    }

    fun updateTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(tx)
            if (tx.status == "CONFIRMED") {
                repository.deleteNotificationsByTransactionId(tx.id)
                NotificationHelper.cancelTransactionNotification(getApplication(), tx.id)
                val remainingPending = repository.getPendingTransactionsList()
                if (remainingPending.isEmpty()) {
                    NotificationHelper.cancelPendingReviewNotification(getApplication())
                }
            }
        }
    }

    fun loadOlderTransactions() {
        viewModelScope.launch {
            if (isLoadingOlder.value) return@launch
            isLoadingOlder.value = true
            kotlinx.coroutines.delay(600)
            isLoadingOlder.value = false
            android.widget.Toast.makeText(
                getApplication(),
                "No older transactions available for this account.",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun addCategory(name: String, tag: String, keywords: String, iconName: String = "category", colorHex: String = "#DAE2FF") {
        viewModelScope.launch {
            val newCategory = CategoryEntity(
                name = name,
                iconName = iconName.ifBlank { "category" },
                tag = tag.ifBlank { "Custom" },
                colorHex = colorHex.ifBlank { "#DAE2FF" },
                isCustom = true,
                keywords = keywords
            )
            repository.insertCategory(newCategory)
        }
    }

    fun updateCategory(category: CategoryEntity, oldName: String? = null) {
        viewModelScope.launch {
            repository.updateCategory(category, oldName)
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun simulateSms(sender: String, body: String) {
        viewModelScope.launch {
            val userCurrency = selectedCountry.value.currencyCode
            val catList = repository.getCategoriesList()
            val recentTx = repository.getAllTransactionsList()
            val recentScams = repository.getActiveScamAlertsList()

            val currentBalance = recentTx.sumOf { tx ->
                if (tx.category.equals("Salary", ignoreCase = true) || tx.amount < 0) {
                    Math.abs(tx.amount)
                } else {
                    -tx.amount
                }
            }

            val scamDetection = com.example.detector.ScamDetector.detectScam(
                sender = sender,
                body = body,
                recentTransactions = recentTx,
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

                val notif = AppNotificationEntity(
                    title = "⚠️ Possible Scam Detected",
                    message = "From $sender: ${scamDetection.primaryReason}",
                    type = "Scam Alerts",
                    scamAlertId = scamId,
                    targetRoute = "scam_alerts"
                )
                repository.insertNotification(notif)

                NotificationHelper.showScamAlertNotification(
                    context = getApplication(),
                    scamAlertId = scamId,
                    sender = sender,
                    reason = scamDetection.primaryReason
                )
                android.widget.Toast.makeText(
                    getApplication(),
                    "⚠️ Possible Scam Detected & Flagged!",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                return@launch
            }

            val parsed = SmsParser.parseSms(sender, body, catList, userCurrency)

            if (parsed != null) {
                val newTx = TransactionEntity(
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
                val insertedId = repository.insertTransaction(newTx)

                val formattedAmount = String.format("%,.0f %s", newTx.amount, newTx.currency)
                val notif = AppNotificationEntity(
                    title = "New Transaction Detected",
                    message = "Amount: $formattedAmount at ${newTx.merchant}. Tap to add description & review.",
                    type = "Transactions",
                    transactionId = insertedId,
                    targetRoute = "transaction_detail"
                )
                repository.insertNotification(notif)

                NotificationHelper.showNewTransactionNotification(
                    context = getApplication(),
                    transactionId = insertedId,
                    merchant = newTx.merchant,
                    amount = newTx.amount,
                    currency = newTx.currency
                )
            }
        }
    }

    fun deleteScamAlert(scamAlert: com.example.data.ScamAlertEntity) {
        viewModelScope.launch {
            repository.deleteScamAlertById(scamAlert.id)
            repository.deleteNotificationsByScamAlertId(scamAlert.id)
            NotificationHelper.cancelScamAlertNotification(getApplication(), scamAlert.id)
            android.widget.Toast.makeText(
                getApplication(),
                "Flagged scam alert deleted.",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun deleteNotification(notification: AppNotificationEntity) {
        viewModelScope.launch {
            repository.deleteNotificationById(notification.id)
            if (notification.scamAlertId != null) {
                repository.deleteScamAlertById(notification.scamAlertId)
            }
            NotificationHelper.cancelAllSystemNotificationsForAppNotification(getApplication(), notification)
            android.widget.Toast.makeText(
                getApplication(),
                "Notification removed.",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.deleteAllNotifications()
            NotificationHelper.cancelAllNotifications(getApplication())
            android.widget.Toast.makeText(
                getApplication(),
                "All notifications cleared.",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun overrideScamAlert(scamAlert: com.example.data.ScamAlertEntity) {
        viewModelScope.launch {
            repository.deleteScamAlertById(scamAlert.id)
            repository.deleteNotificationsByScamAlertId(scamAlert.id)
            NotificationHelper.cancelScamAlertNotification(getApplication(), scamAlert.id)

            val userCurrency = selectedCountry.value.currencyCode
            val catList = repository.getCategoriesList()

            val parsed = SmsParser.parseSms(scamAlert.sender, scamAlert.body, catList, userCurrency)
            val newTx = if (parsed != null) {
                TransactionEntity(
                    amount = parsed.amount,
                    currency = parsed.currency,
                    dateEpochMillis = System.currentTimeMillis(),
                    description = parsed.description,
                    merchant = parsed.merchant,
                    category = "Uncategorized",
                    source = "SMS",
                    status = "PENDING",
                    paymentMethod = scamAlert.sender
                )
            } else {
                TransactionEntity(
                    amount = extractFallbackAmount(scamAlert.body),
                    currency = userCurrency,
                    dateEpochMillis = System.currentTimeMillis(),
                    description = "${scamAlert.sender} (Overridden Scam Alert)",
                    merchant = scamAlert.sender,
                    category = "Uncategorized",
                    source = "SMS",
                    status = "PENDING",
                    paymentMethod = scamAlert.sender
                )
            }

            val insertedId = repository.insertTransaction(newTx)

            val formattedAmount = String.format("%,.0f %s", newTx.amount, newTx.currency)
            val notif = AppNotificationEntity(
                title = "New Transaction (Reclassified)",
                message = "From ${scamAlert.sender}: $formattedAmount. Added to Pending Review.",
                type = "Transactions",
                transactionId = insertedId,
                targetRoute = "transaction_detail"
            )
            repository.insertNotification(notif)

            NotificationHelper.showNewTransactionNotification(
                context = getApplication(),
                transactionId = insertedId,
                merchant = newTx.merchant,
                amount = newTx.amount,
                currency = newTx.currency
            )

            android.widget.Toast.makeText(
                getApplication(),
                "Reclassified as real transaction. Added to Pending Review.",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun extractFallbackAmount(body: String): Double {
        val pattern = java.util.regex.Pattern.compile("([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{1,2})?|[0-9]+(?:\\.[0-9]{1,2})?)")
        val matcher = pattern.matcher(body)
        if (matcher.find()) {
            val numStr = matcher.group(1)?.replace(",", "")
            return numStr?.toDoubleOrNull() ?: 0.0
        }
        return 0.0
    }

    fun savePin(pin: String) {
        val pinHash = PasswordUtil.hashPassword(pin)
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity(id = 1)
            val updated = current.copy(
                pinHash = pinHash,
                lockMode = "PIN"
            )
            repository.updateUserProfile(updated)
        }
    }

    fun updateLockMode(mode: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity(id = 1)
            val updated = current.copy(
                lockMode = mode,
                biometricEnabled = (mode == "BIOMETRIC")
            )
            repository.updateUserProfile(updated)
        }
    }

    fun verifyPin(enteredPin: String): Boolean {
        val currentHash = userProfile.value?.pinHash ?: return false
        return PasswordUtil.verifyPassword(enteredPin, currentHash)
    }

    fun triggerPendingReviewReminder() {
        viewModelScope.launch {
            val pendingCount = repository.getPendingTransactionsList().size
            val count = if (pendingCount > 0) pendingCount else 1
            val notif = AppNotificationEntity(
                title = "Pending Review Reminder",
                message = "$count transaction(s) need your review to keep reports accurate.",
                type = "Pending Review",
                targetRoute = "pending_review"
            )
            repository.insertNotification(notif)
            NotificationHelper.showPendingReviewNotification(getApplication(), count)
        }
    }

    fun triggerMonthlyReportNotification() {
        viewModelScope.launch {
            val notif = AppNotificationEntity(
                title = "Monthly Report Ready",
                message = "Your financial summary for this month is ready to review.",
                type = "Reports",
                targetRoute = "reports"
            )
            repository.insertNotification(notif)
            NotificationHelper.showMonthlyReportNotification(getApplication())
        }
    }

    fun triggerNewLoginNotification(deviceName: String = "Pixel 8 Pro") {
        viewModelScope.launch {
            val notif = AppNotificationEntity(
                title = "New Login Detected",
                message = "A new session was authenticated on $deviceName.",
                type = "Security",
                targetRoute = "security"
            )
            repository.insertNotification(notif)
            NotificationHelper.showNewLoginNotification(getApplication(), deviceName)
        }
    }
}

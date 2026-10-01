import Foundation
import Combine
import SwiftUI
import CryptoKit

public final class MainViewModel: ObservableObject {
    @Published public var transactions: [Transaction] = []
    @Published public var categories: [Category] = []
    @Published public var scamAlerts: [ScamAlert] = []
    @Published public var notifications: [AppNotification] = []
    @Published public var userProfile: UserProfile = UserProfile()
    @Published public var selectedCountry: Country = .rwanda
    @Published public var isScamFilteringEnabled: Bool = true
    @Published public var isUnlocked: Bool = false

    @Published public var deepLinkRoute: String? = nil
    @Published public var detectedClipboardText: String? = nil
    @Published public var showClipboardImportPrompt: Bool = false

    private let storage = StorageService.shared
    private let parser = SMSParser.shared
    private let detector = ScamDetector.shared
    private let senderManager = TrustedSenderManager.shared
    private let biometrics = BiometricAuthService.shared

    public var pendingTransactions: [Transaction] {
        transactions.filter { $0.status == "PENDING" }
    }

    public var activeScamAlerts: [ScamAlert] {
        scamAlerts.filter { !$0.isOverridden }
    }

    public var unreadNotificationsCount: Int {
        notifications.filter { $0.isUnread }.count
    }

    public init() {
        refreshAll()
    }

    public func refreshAll() {
        self.categories = storage.loadCategories()
        self.transactions = storage.loadTransactions()
        self.scamAlerts = storage.loadScamAlerts()
        self.notifications = storage.loadNotifications()
        if let profile = storage.loadUserProfile() {
            self.userProfile = profile
            self.selectedCountry = Country.fromCurrencyCode(profile.currencyCode)
        }
        self.isScamFilteringEnabled = senderManager.isScamFilteringEnabled()

        if userProfile.lockMode == "NONE" || userProfile.pinHash == nil {
            self.isUnlocked = true
        }
    }

    // MARK: - Core Automated SMS Ingestion & Fraud Detection
    @discardableResult
    public func processIncomingSms(sender: String, body: String) -> (transaction: Transaction?, scam: ScamAlert?) {
        let detection = detector.detectScam(
            sender: sender,
            body: body,
            recentTransactions: transactions,
            recentScams: scamAlerts
        )

        // If Likely Scam -> Log Scam Alert & Notification
        if detection.classification == .likelyScam {
            let scamAlert = ScamAlert(
                sender: sender,
                body: body,
                primaryReason: detection.primaryReason,
                detectedSignals: detection.detectedSignals.joined(separator: ", ")
            )
            storage.addScamAlert(scamAlert)

            let scamNotification = AppNotification(
                title: "⚠️ High Risk Fraud Detected",
                message: "Suspicious message blocked from \(sender): \(detection.primaryReason)",
                type: "Scam Alerts",
                scamAlertId: scamAlert.id,
                targetRoute: "notifications"
            )
            storage.addNotification(scamNotification)
            refreshAll()
            return (nil, scamAlert)
        }

        // Sequential Balance update
        if let newBal = detection.extractedNewBalance {
            senderManager.setLastBalance(for: sender, balance: newBal)
        }

        // Parse Transaction
        guard let parsed = parser.parseSms(
            sender: sender,
            body: body,
            categories: categories,
            userCurrency: selectedCountry.currencyCode
        ) else {
            return (nil, nil)
        }

        let initialStatus = (detection.classification == .needsReview) ? "PENDING" : parsed.status

        let newTx = Transaction(
            amount: parsed.amount,
            currency: parsed.currency,
            description: parsed.description,
            merchant: parsed.merchant,
            category: parsed.category,
            source: "SMS",
            status: initialStatus,
            paymentMethod: sender
        )
        storage.addTransaction(newTx)

        // Notify user of new transaction or review request
        let notifTitle = (initialStatus == "PENDING") ? "New Transaction Needs Review" : "Transaction Recorded"
        let notifMsg = "\(FormatUtil.formatAmount(newTx.amount, currency: newTx.currency)) at \(newTx.merchant) (\(newTx.category))"
        let notif = AppNotification(
            title: notifTitle,
            message: notifMsg,
            type: (initialStatus == "PENDING") ? "Pending Review" : "Transactions",
            transactionId: newTx.id,
            targetRoute: (initialStatus == "PENDING") ? "pending_review" : "transactions"
        )
        storage.addNotification(notif)

        refreshAll()
        return (newTx, nil)
    }

    // MARK: - Clipboard Ingestion
    public func checkClipboard() {
        if let clip = UIPasteboard.general.string, !clip.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            let lower = clip.lowercased()
            let isTransactionSms = ["paid", "received", "momo", "m-pesa", "txid", "transferred", "bal:"].contains { lower.contains($0) }
            if isTransactionSms && clip != detectedClipboardText {
                detectedClipboardText = clip
                showClipboardImportPrompt = true
            }
        }
    }

    public func importFromClipboard() {
        guard let text = detectedClipboardText else { return }
        processIncomingSms(sender: "SMS Ingestion", body: text)
        detectedClipboardText = nil
        showClipboardImportPrompt = false
    }

    // MARK: - Manual & Editing Operations
    public func addManualTransaction(amount: Double, category: String, description: String) {
        let tx = Transaction(
            amount: amount,
            currency: selectedCountry.currencyCode,
            description: description.isEmpty ? "Manual Expense" : description,
            merchant: description.isEmpty ? "Manual Expense" : description,
            category: category,
            source: "MANUAL",
            status: "CONFIRMED",
            paymentMethod: "Manual"
        )
        storage.addTransaction(tx)
        refreshAll()
    }

    public func categorizePendingTransaction(id: Int64, category: String, description: String, notes: String) {
        if var tx = transactions.first(where: { $0.id == id }) {
            tx.category = category
            if !description.isEmpty { tx.description = description }
            tx.notes = notes
            tx.status = "CONFIRMED"
            storage.updateTransaction(tx)
            refreshAll()
        }
    }

    public func updateTransaction(_ tx: Transaction) {
        var lockedTx = tx
        if let existing = transactions.first(where: { $0.id == tx.id }) {
            lockedTx.source = existing.source
        }
        storage.updateTransaction(lockedTx)
        refreshAll()
    }

    public func deleteTransaction(_ tx: Transaction) {
        storage.deleteTransaction(id: tx.id)
        refreshAll()
    }

    // MARK: - Categories
    public func addCategory(name: String, tag: String, keywords: String, iconName: String, colorHex: String) {
        let cat = Category(
            id: Int64(Date().timeIntervalSince1970 * 1000),
            name: name,
            iconName: iconName,
            tag: tag,
            colorHex: colorHex,
            isCustom: true,
            keywords: keywords
        )
        storage.addCategory(cat)
        refreshAll()
    }

    public func updateCategory(_ category: Category, oldName: String) {
        storage.updateCategory(category)
        // If name changed, optionally update transactions with old category name
        if category.name != oldName {
            for var tx in transactions where tx.category == oldName {
                tx.category = category.name
                storage.updateTransaction(tx)
            }
        }
        refreshAll()
    }

    public func deleteCategory(_ category: Category) {
        storage.deleteCategory(id: category.id)
        // Re-assign transactions to Uncategorized
        for var tx in transactions where tx.category == category.name {
            tx.category = "Uncategorized"
            tx.status = "PENDING"
            storage.updateTransaction(tx)
        }
        refreshAll()
    }

    // MARK: - Scam Alerts & Notifications
    public func overrideScamAlert(_ alert: ScamAlert) {
        var updated = alert
        updated.isOverridden = true
        storage.updateScamAlert(updated)
        // Process as transaction if possible
        _ = parser.parseSms(
            sender: alert.sender,
            body: alert.body,
            categories: categories,
            userCurrency: selectedCountry.currencyCode
        ).map { parsed in
            let tx = Transaction(
                amount: parsed.amount,
                currency: parsed.currency,
                description: parsed.description,
                merchant: parsed.merchant,
                category: parsed.category,
                source: "SMS",
                status: "CONFIRMED",
                paymentMethod: alert.sender
            )
            storage.addTransaction(tx)
        }
        refreshAll()
    }

    public func deleteScamAlert(_ alert: ScamAlert) {
        storage.deleteScamAlert(id: alert.id)
        refreshAll()
    }

    public func deleteNotification(_ notif: AppNotification) {
        storage.deleteNotification(id: notif.id)
        refreshAll()
    }

    public func clearAllNotifications() {
        storage.clearAllNotifications()
        refreshAll()
    }

    // MARK: - Country & Settings
    public func selectCountry(_ country: Country) {
        selectedCountry = country
        userProfile.countryCode = country.rawValue
        userProfile.currencyCode = country.currencyCode
        storage.saveUserProfile(userProfile)
        refreshAll()
    }

    public func setScamFilteringEnabled(_ enabled: Bool) {
        isScamFilteringEnabled = enabled
        senderManager.setScamFilteringEnabled(enabled)
    }

    public func updateLockMode(_ mode: String) {
        userProfile.lockMode = mode
        storage.saveUserProfile(userProfile)
        refreshAll()
    }

    // MARK: - PIN & Biometrics
    public func savePin(_ pin: String) {
        let hash = hashPin(pin)
        userProfile.pinHash = hash
        storage.saveUserProfile(userProfile)
        isUnlocked = true
        refreshAll()
    }

    public func verifyPin(_ pin: String) -> Bool {
        guard let hash = userProfile.pinHash else { return true }
        let enteredHash = hashPin(pin)
        let matched = (enteredHash == hash)
        if matched {
            isUnlocked = true
        }
        return matched
    }

    public func unlockWithBiometrics() {
        biometrics.authenticate { [weak self] success, _ in
            if success {
                self?.isUnlocked = true
            }
        }
    }

    private func hashPin(_ pin: String) -> String {
        let inputData = Data(pin.utf8)
        let hashed = SHA256.hash(data: inputData)
        return hashed.compactMap { String(format: "%02x", $0) }.joined()
    }

    // MARK: - Test Notification Triggers
    public func triggerPendingReviewReminder() {
        let notif = AppNotification(
            title: "Pending Review Reminder",
            message: "You have \(pendingTransactions.count) transactions waiting for categorization.",
            type: "Pending Review",
            targetRoute: "pending_review"
        )
        storage.addNotification(notif)
        refreshAll()
    }

    public func triggerMonthlyReportNotification() {
        let notif = AppNotification(
            title: "Monthly Financial Report Ready",
            message: "Your financial summary for this month has been generated.",
            type: "Reports",
            targetRoute: "reports"
        )
        storage.addNotification(notif)
        refreshAll()
    }

    public func triggerWeeklyReportNotification() {
        let notif = AppNotification(
            title: "Weekly Spending Summary",
            message: "You spent a total of \(FormatUtil.formatAmount(totalMonthlyExpenses, currency: selectedCountry.currencyCode)) this week.",
            type: "Reports",
            targetRoute: "reports"
        )
        storage.addNotification(notif)
        refreshAll()
    }

    public func triggerNewLoginNotification() {
        let notif = AppNotification(
            title: "New Session Authenticated",
            message: "PromptPay was accessed on iPhone.",
            type: "Security",
            targetRoute: "security"
        )
        storage.addNotification(notif)
        refreshAll()
    }

    // MARK: - Aggregations & Stats
    public var totalBalance: Double {
        let income = transactions.filter { $0.category == "Salary" || $0.amount > 0 && $0.category.lowercased().contains("income") }.reduce(0.0) { $0 + $1.amount }
        let expenses = transactions.filter { $0.category != "Salary" && !$0.category.lowercased().contains("income") }.reduce(0.0) { $0 + $1.amount }
        return income - expenses
    }

    public var totalMonthlyExpenses: Double {
        transactions.filter { $0.category != "Salary" && !$0.category.lowercased().contains("income") }.reduce(0.0) { $0 + $1.amount }
    }

    public var totalMonthlyIncome: Double {
        transactions.filter { $0.category == "Salary" || $0.category.lowercased().contains("income") }.reduce(0.0) { $0 + $1.amount }
    }
}

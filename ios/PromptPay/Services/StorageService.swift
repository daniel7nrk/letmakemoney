import Foundation

public final class StorageService {
    public static let shared = StorageService()

    private let fileManager = FileManager.default
    private let appGroupId = "group.com.promptpay"
    private let queue = DispatchQueue(label: "com.promptpay.storageservice", attributes: .concurrent)

    private var baseDirectory: URL {
        if let container = fileManager.containerURL(forSecurityApplicationGroupIdentifier: appGroupId) {
            return container
        }
        return fileManager.urls(for: .documentDirectory, in: .userDomainMask)[0]
    }

    private var transactionsFile: URL { baseDirectory.appendingPathComponent("transactions.json") }
    private var categoriesFile: URL { baseDirectory.appendingPathComponent("categories.json") }
    private var scamAlertsFile: URL { baseDirectory.appendingPathComponent("scam_alerts.json") }
    private var notificationsFile: URL { baseDirectory.appendingPathComponent("notifications.json") }
    private var userProfileFile: URL { baseDirectory.appendingPathComponent("user_profile.json") }

    private init() {
        seedInitialDataIfEmpty()
    }

    // MARK: - Initial Seeding
    private func seedInitialDataIfEmpty() {
        if loadCategories().isEmpty {
            saveCategories(Category.defaults)
        }
        if loadTransactions().isEmpty {
            saveTransactions(Transaction.defaults)
        }
        if loadNotifications().isEmpty {
            saveNotifications(AppNotification.defaults)
        }
        if loadUserProfile() == nil {
            saveUserProfile(UserProfile())
        }
    }

    // MARK: - Generic Persistence Helper
    private func load<T: Decodable>(from url: URL, as type: T.Type) -> T? {
        queue.sync {
            guard fileManager.fileExists(atPath: url.path),
                  let data = try? Data(contentsOf: url) else {
                return nil
            }
            return try? JSONDecoder().decode(T.self, from: data)
        }
    }

    private func save<T: Encodable>(_ object: T, to url: URL) {
        queue.async(flags: .barrier) {
            if let data = try? JSONEncoder().encode(object) {
                try? data.write(to: url, options: .atomic)
            }
        }
    }

    // MARK: - Transactions
    public func loadTransactions() -> [Transaction] {
        load(from: transactionsFile, as: [Transaction].self) ?? []
    }

    public func saveTransactions(_ transactions: [Transaction]) {
        save(transactions, to: transactionsFile)
    }

    public func addTransaction(_ transaction: Transaction) {
        var list = loadTransactions()
        list.insert(transaction, at: 0)
        saveTransactions(list)
    }

    public func updateTransaction(_ transaction: Transaction) {
        var list = loadTransactions()
        if let idx = list.firstIndex(where: { $0.id == transaction.id }) {
            list[idx] = transaction
            saveTransactions(list)
        }
    }

    public func deleteTransaction(id: Int64) {
        var list = loadTransactions()
        list.removeAll { $0.id == id }
        saveTransactions(list)
    }

    // MARK: - Categories
    public func loadCategories() -> [Category] {
        load(from: categoriesFile, as: [Category].self) ?? []
    }

    public func saveCategories(_ categories: [Category]) {
        save(categories, to: categoriesFile)
    }

    public func addCategory(_ category: Category) {
        var list = loadCategories()
        list.append(category)
        saveCategories(list)
    }

    public func updateCategory(_ category: Category) {
        var list = loadCategories()
        if let idx = list.firstIndex(where: { $0.id == category.id }) {
            list[idx] = category
            saveCategories(list)
        }
    }

    public func deleteCategory(id: Int64) {
        var list = loadCategories()
        list.removeAll { $0.id == id }
        saveCategories(list)
    }

    // MARK: - Scam Alerts
    public func loadScamAlerts() -> [ScamAlert] {
        load(from: scamAlertsFile, as: [ScamAlert].self) ?? []
    }

    public func saveScamAlerts(_ alerts: [ScamAlert]) {
        save(alerts, to: scamAlertsFile)
    }

    public func addScamAlert(_ alert: ScamAlert) {
        var list = loadScamAlerts()
        list.insert(alert, at: 0)
        saveScamAlerts(list)
    }

    public func updateScamAlert(_ alert: ScamAlert) {
        var list = loadScamAlerts()
        if let idx = list.firstIndex(where: { $0.id == alert.id }) {
            list[idx] = alert
            saveScamAlerts(list)
        }
    }

    public func deleteScamAlert(id: Int64) {
        var list = loadScamAlerts()
        list.removeAll { $0.id == id }
        saveScamAlerts(list)
    }

    // MARK: - Notifications
    public func loadNotifications() -> [AppNotification] {
        load(from: notificationsFile, as: [AppNotification].self) ?? []
    }

    public func saveNotifications(_ notifications: [AppNotification]) {
        save(notifications, to: notificationsFile)
    }

    public func addNotification(_ notification: AppNotification) {
        var list = loadNotifications()
        list.insert(notification, at: 0)
        saveNotifications(list)
    }

    public func deleteNotification(id: Int64) {
        var list = loadNotifications()
        list.removeAll { $0.id == id }
        saveNotifications(list)
    }

    public func clearAllNotifications() {
        saveNotifications([])
    }

    // MARK: - User Profile
    public func loadUserProfile() -> UserProfile? {
        load(from: userProfileFile, as: UserProfile.self)
    }

    public func saveUserProfile(_ profile: UserProfile) {
        save(profile, to: userProfileFile)
    }
}

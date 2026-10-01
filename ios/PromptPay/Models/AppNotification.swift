import Foundation

public struct AppNotification: Identifiable, Codable, Equatable {
    public var id: Int64
    public var title: String
    public var message: String
    public var timestamp: String
    public var dateEpochMillis: Int64
    public var isUnread: Bool
    public var type: String             // "Transactions", "Scam Alerts", "Reports", "Security", "Pending Review"
    public var transactionId: Int64?
    public var scamAlertId: Int64?
    public var targetRoute: String?

    public init(
        id: Int64 = Int64(Date().timeIntervalSince1970 * 1000),
        title: String,
        message: String,
        timestamp: String = "Just now",
        dateEpochMillis: Int64 = Int64(Date().timeIntervalSince1970 * 1000),
        isUnread: Bool = true,
        type: String,
        transactionId: Int64? = nil,
        scamAlertId: Int64? = nil,
        targetRoute: String? = nil
    ) {
        self.id = id
        self.title = title
        self.message = message
        self.timestamp = timestamp
        self.dateEpochMillis = dateEpochMillis
        self.isUnread = isUnread
        self.type = type
        self.transactionId = transactionId
        self.scamAlertId = scamAlertId
        self.targetRoute = targetRoute
    }

    public static let defaults: [AppNotification] = [
        AppNotification(
            id: 1,
            title: "Pending Review Reminder",
            message: "You have uncategorized transactions waiting for review.",
            timestamp: "2 hours ago",
            isUnread: true,
            type: "Pending Review",
            targetRoute: "pending_review"
        ),
        AppNotification(
            id: 2,
            title: "Monthly Report Ready",
            message: "Your financial summary for last month is ready to review.",
            timestamp: "1 day ago",
            isUnread: false,
            type: "Reports",
            targetRoute: "reports"
        ),
        AppNotification(
            id: 3,
            title: "New Login Detected",
            message: "A new session was authenticated on iPhone 16 Pro.",
            timestamp: "3 days ago",
            isUnread: false,
            type: "Security",
            targetRoute: "security"
        )
    ]
}

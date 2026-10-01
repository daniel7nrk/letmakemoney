import Foundation

public struct ScamAlert: Identifiable, Codable, Equatable {
    public var id: Int64
    public var sender: String
    public var body: String
    public var dateEpochMillis: Int64
    public var primaryReason: String
    public var detectedSignals: String
    public var isOverridden: Bool

    public init(
        id: Int64 = Int64(Date().timeIntervalSince1970 * 1000),
        sender: String,
        body: String,
        dateEpochMillis: Int64 = Int64(Date().timeIntervalSince1970 * 1000),
        primaryReason: String,
        detectedSignals: String = "",
        isOverridden: Bool = false
    ) {
        self.id = id
        self.sender = sender
        self.body = body
        self.dateEpochMillis = dateEpochMillis
        self.primaryReason = primaryReason
        self.detectedSignals = detectedSignals
        self.isOverridden = isOverridden
    }
}

import Foundation

public struct Transaction: Identifiable, Codable, Equatable {
    public var id: Int64
    public var amount: Double
    public var currency: String
    public var dateEpochMillis: Int64
    public var description: String
    public var merchant: String
    public var category: String
    public var source: String          // "SMS" or "MANUAL"
    public var status: String          // "PENDING" or "CONFIRMED"
    public var isRecurring: BooleanType
    public var notes: String
    public var paymentMethod: String

    public init(
        id: Int64 = Int64(Date().timeIntervalSince1970 * 1000),
        amount: Double,
        currency: String,
        dateEpochMillis: Int64 = Int64(Date().timeIntervalSince1970 * 1000),
        description: String,
        merchant: String,
        category: String,
        source: String = "SMS",
        status: String = "CONFIRMED",
        isRecurring: Bool = false,
        notes: String = "",
        paymentMethod: String = "Mobile Money"
    ) {
        self.id = id
        self.amount = amount
        self.currency = currency
        self.dateEpochMillis = dateEpochMillis
        self.description = description
        self.merchant = merchant
        self.category = category
        self.source = source
        self.status = status
        self.isRecurring = isRecurring
        self.notes = notes
        self.paymentMethod = paymentMethod
    }

    public typealias BooleanType = Bool
}

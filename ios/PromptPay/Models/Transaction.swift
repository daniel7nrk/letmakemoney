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
    public var fee: Double
    public var feeCategory: String
    public var notes: String
    public var paymentMethod: String
    public var isSynced: Bool
    public var updatedAtEpochMillis: Int64

    public init(
        id: Int64 = Int64(Date().timeIntervalSince1970 * 1000),
        amount: Double,
        currency: String,
        dateEpochMillis: Int64 = Int64(Date().timeIntervalSince1970 * 1000),
        description: String,
        merchant: String,
        category: String,
        fee: Double = 0.0,
        feeCategory: String = "Transaction Fee",
        source: String = "SMS",
        status: String = "CONFIRMED",
        isRecurring: Bool = false,
        notes: String = "",
        paymentMethod: String = "Mobile Money",
        isSynced: Bool = false,
        updatedAtEpochMillis: Int64 = Int64(Date().timeIntervalSince1970 * 1000)
    ) {
        self.id = id
        self.amount = amount
        self.currency = currency
        self.dateEpochMillis = dateEpochMillis
        self.description = description
        self.merchant = merchant
        self.category = category
        self.fee = fee
        self.feeCategory = feeCategory
        self.source = source
        self.status = status
        self.isRecurring = isRecurring
        self.notes = notes
        self.paymentMethod = paymentMethod
        self.isSynced = isSynced
        self.updatedAtEpochMillis = updatedAtEpochMillis
    }

    public typealias BooleanType = Bool
}

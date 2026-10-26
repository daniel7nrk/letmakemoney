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
        paymentMethod: String = "Mobile Money"
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
    }

    public static let defaults: [Transaction] = [
        Transaction(
            id: 1,
            amount: 8500.0,
            currency: "RWF",
            dateEpochMillis: Int64(Date().timeIntervalSince1970 * 1000) - 3600000,
            description: "Simba Supermarket (SMS)",
            merchant: "Simba Supermarket",
            category: "Food & Dining",
            fee: 100.0,
            feeCategory: "Transaction Fee",
            source: "SMS",
            status: "CONFIRMED",
            paymentMethod: "MTN MoMo"
        ),
        Transaction(
            id: 2,
            amount: 3200.0,
            currency: "RWF",
            dateEpochMillis: Int64(Date().timeIntervalSince1970 * 1000) - 18000000,
            description: "Yego Moto (SMS)",
            merchant: "Yego Moto",
            category: "Transport",
            fee: 50.0,
            feeCategory: "Transaction Fee",
            source: "SMS",
            status: "CONFIRMED",
            paymentMethod: "Airtel Money"
        ),
        Transaction(
            id: 3,
            amount: 15000.0,
            currency: "RWF",
            dateEpochMillis: Int64(Date().timeIntervalSince1970 * 1000) - 86400000,
            description: "EUCL Cashpower (SMS)",
            merchant: "EUCL Cashpower",
            category: "Rent & Utilities",
            fee: 200.0,
            feeCategory: "Transaction Fee",
            source: "SMS",
            status: "CONFIRMED",
            paymentMethod: "MTN MoMo"
        ),
        Transaction(
            id: 4,
            amount: 5000.0,
            currency: "RWF",
            dateEpochMillis: Int64(Date().timeIntervalSince1970 * 1000) - 172800000,
            description: "Kigali Fresh Mart (SMS)",
            merchant: "Kigali Fresh Mart",
            category: "Uncategorized",
            fee: 100.0,
            feeCategory: "Transaction Fee",
            source: "SMS",
            status: "PENDING",
            paymentMethod: "MTN MoMo"
        )
    ]

    public typealias BooleanType = Bool
}

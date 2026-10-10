import Foundation

public struct ParsedSmsResult: Equatable {
    public let amount: Double
    public let currency: String
    public let merchant: String
    public let description: String
    public let category: String
    public let status: String // "PENDING" or "CONFIRMED"
    public let fee: Double
    public let feeCategory: String
    public let shouldRecord: Bool
    public let timestampEpochMillis: Int64?

    public init(
        amount: Double,
        currency: String,
        merchant: String,
        description: String,
        category: String,
        status: String,
        fee: Double = 0.0,
        feeCategory: String = "Transaction Fee",
        shouldRecord: Bool = true,
        timestampEpochMillis: Int64? = nil
    ) {
        self.amount = amount
        self.currency = currency
        self.merchant = merchant
        self.description = description
        self.category = category
        self.status = status
        self.fee = fee
        self.feeCategory = feeCategory
        self.shouldRecord = shouldRecord
        self.timestampEpochMillis = timestampEpochMillis
    }
}

public final class SMSParser {
    public static let shared = SMSParser()

    private let transactionKeywords = [
        "paid", "payment", "transferred", "transfer", "sent", "debit", "debited",
        "received", "credited", "bought", "purchased", "spent", "airtime", "cash power",
        "cashpower", "fee", "bill", "m-pesa", "momo", "airtel money"
    ]

    private let genericSmsWords: Set<String> = [
        "momo", "paid", "payment", "bank", "bill", "transfer", "transferred",
        "spent", "debit", "credit", "confirmed", "airtime", "deposit", "money",
        "sent", "you", "received", "fee"
    ]

    private init() {}

    public func parseSms(
        sender: String,
        body: String,
        categories: [Category],
        userCurrency: String
    ) -> ParsedSmsResult? {
        let lowerBody = body.lowercased()

        // 1. Check if SMS is a transaction message
        let isTransaction = transactionKeywords.contains { lowerBody.contains($0) }
        if !isTransaction {
            return nil
        }

        // 2. Determine whether it should be auto-recorded as an expense
        let isIncomingReceived = lowerBody.contains("you have received") ||
            lowerBody.contains("received from") ||
            (lowerBody.contains("received ") && !lowerBody.contains("payment to") && !lowerBody.contains("transferred to"))
        let shouldRecord = !isIncomingReceived

        // 3. Extract Amount & Currency
        guard let (amount, currency) = extractAmountAndCurrency(body: body, defaultCurrency: userCurrency) else {
            return nil
        }

        // 4. Extract Transaction Fee
        let fee = extractFee(body: body)
        let feeCategory = "Transaction Fee"

        // 5. Extract Date/Time timestamp
        let timestampEpochMillis = extractTimestamp(body: body)

        // 6. Extract Recipient / Merchant
        let merchant = extractMerchant(body: body, sender: sender)

        // 7. Auto-categorization
        var matchedCategory = "Uncategorized"
        var status = "PENDING"

        if lowerBody.contains("airtime") {
            matchedCategory = "Airtime & Data"
            status = "CONFIRMED"
        } else if lowerBody.contains("wasac") || lowerBody.contains("water") || lowerBody.contains("electricity") || lowerBody.contains("cash power") || lowerBody.contains("cashpower") {
            matchedCategory = "Utilities & Bills"
            status = "CONFIRMED"
        }

        if matchedCategory == "Uncategorized" {
            for cat in categories {
                let keywordsList = cat.keywords
                    .split(separator: ",")
                    .map { $0.trimmingCharacters(in: .whitespaces).lowercased() }
                    .filter { !$0.isEmpty && !genericSmsWords.contains($0) }

                let foundMatch = keywordsList.contains { kw in
                    lowerBody.contains(kw) || merchant.lowercased().contains(kw)
                }

                if foundMatch {
                    matchedCategory = cat.name
                    status = "CONFIRMED"
                    break
                }
            }
        }

        if matchedCategory == "Uncategorized" {
            status = "PENDING"
        }

        let desc = merchant.isEmpty ? "Mobile Money Transaction" : "\(merchant) (SMS)"

        return ParsedSmsResult(
            amount: amount,
            currency: currency,
            merchant: merchant,
            description: desc,
            category: matchedCategory,
            status: status,
            fee: fee,
            feeCategory: feeCategory,
            shouldRecord: shouldRecord,
            timestampEpochMillis: timestampEpochMillis
        )
    }

    private func extractFee(body: String) -> Double {
        let nsBody = body as NSString
        let feePatterns = [
            #"(?i)(?:fee\s*(?:was|is|:)?\s*)\s*([0-9,]+(?:\.[0-9]{1,2})?)\s*(?:RWF|FRW|RF|KES|KSH|UGX|TZS|USD|BIF|SSP)?"#,
            #"(?i)\.Fee:\s*([0-9,]+(?:\.[0-9]{1,2})?)\s*(?:RWF|FRW|RF|KES|KSH|UGX|TZS|USD|BIF|SSP)?"#,
            #"(?i)Fee\s+([0-9,]+(?:\.[0-9]{1,2})?)\s*(?:RWF|FRW|RF)?"#
        ]

        for p in feePatterns {
            if let regex = try? NSRegularExpression(pattern: p) {
                let range = NSRange(location: 0, length: nsBody.length)
                if let match = regex.firstMatch(in: body, options: [], range: range), match.numberOfRanges >= 2 {
                    let feeStr = nsBody.substring(with: match.range(at: 1)).replacingOccurrences(of: ",", with: "")
                    if let f = Double(feeStr) {
                        return f
                    }
                }
            }
        }
        return 0.0
    }

    private func extractTimestamp(body: String) -> Int64? {
        let nsBody = body as NSString
        let pattern = #"at\s+(\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2}:\d{2})"#
        if let regex = try? NSRegularExpression(pattern: pattern) {
            let range = NSRange(location: 0, length: nsBody.length)
            if let match = regex.firstMatch(in: body, options: [], range: range), match.numberOfRanges >= 2 {
                let dateStr = nsBody.substring(with: match.range(at: 1))
                let formatter = DateFormatter()
                formatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
                formatter.locale = Locale(identifier: "en_US_POSIX")
                if let date = formatter.date(from: dateStr) {
                    return Int64(date.timeIntervalSince1970 * 1000)
                }
            }
        }
        return nil
    }

    private func extractAmountAndCurrency(body: String, defaultCurrency: String) -> (Double, String)? {
        let nsBody = body as NSString

        // Pattern 1: Amount followed by currency
        let pattern1 = #"(?i)(?<![A-Za-z0-9])([0-9,]+(?:\.[0-9]{1,2})?)\s*(RWF|FRW|RF|KES|KSH|UGX|TZS|USD|BIF|SSP)(?![A-Za-z0-9])"#
        if let regex1 = try? NSRegularExpression(pattern: pattern1) {
            let range = NSRange(location: 0, length: nsBody.length)
            if let match = regex1.firstMatch(in: body, options: [], range: range), match.numberOfRanges >= 3 {
                let numStr = nsBody.substring(with: match.range(at: 1)).replacingOccurrences(of: ",", with: "")
                let currGroup = nsBody.substring(with: match.range(at: 2)).uppercased()
                if let val = Double(numStr), val > 0 {
                    let stdCurrency: String
                    switch currGroup {
                    case "FRW", "RF": stdCurrency = "RWF"
                    case "KSH": stdCurrency = "KES"
                    default: stdCurrency = currGroup
                    }
                    return (val, stdCurrency)
                }
            }
        }

        // Pattern 2: Currency followed by amount
        let pattern2 = #"(?i)(RWF|FRW|RF|KES|KSH|UGX|TZS|USD|BIF|SSP)\s*([0-9,]+(?:\.[0-9]{1,2})?)"#
        if let regex2 = try? NSRegularExpression(pattern: pattern2) {
            let range = NSRange(location: 0, length: nsBody.length)
            if let match = regex2.firstMatch(in: body, options: [], range: range), match.numberOfRanges >= 3 {
                let currGroup = nsBody.substring(with: match.range(at: 1)).uppercased()
                let numStr = nsBody.substring(with: match.range(at: 2)).replacingOccurrences(of: ",", with: "")
                if let val = Double(numStr), val > 0 {
                    let stdCurrency: String
                    switch currGroup {
                    case "FRW", "RF": stdCurrency = "RWF"
                    case "KSH": stdCurrency = "KES"
                    default: stdCurrency = currGroup
                    }
                    return (val, stdCurrency)
                }
            }
        }

        // Pattern 3: Transaction verb followed by number
        let pattern3 = #"(?i)(?:paid|transferred|spent|sent|debited|credited|sum of|amount of)\s+([0-9,]+(?:\.[0-9]{1,2})?)"#
        if let regex3 = try? NSRegularExpression(pattern: pattern3) {
            let range = NSRange(location: 0, length: nsBody.length)
            if let match = regex3.firstMatch(in: body, options: [], range: range), match.numberOfRanges >= 2 {
                let numStr = nsBody.substring(with: match.range(at: 1)).replacingOccurrences(of: ",", with: "")
                if let val = Double(numStr), val > 0 {
                    return (val, defaultCurrency)
                }
            }
        }

        return nil
    }

    private func extractMerchant(body: String, sender: String) -> String {
        let nsBody = body as NSString

        let patterns = [
            #"(?i)(?:payment of\s+[0-9,]+(?:\.[0-9]+)?\s*(?:RWF|FRW|RF|KES|USD)?\s+to\s+)([A-Za-z0-9&'\-\.\s]+?)(?=\s+(?:\([0-9*+ ]+\)|with|was|\bat\b|\bon\b|\bdate\b|\bfee\b|ref|txid|\.|$))"#,
            #"(?i)(?:transferred to\s+)([A-Za-z0-9&'\-\.\s]+?)(?=\s+(?:\([0-9*+ ]+\)|with|was|\bat\b|\bon\b|\bdate\b|\bfee\b|ref|txid|\.|$))"#,
            #"(?i)(?:paid to\s+)([A-Za-z0-9&'\-\.\s]+?)(?=\s+(?:\([0-9*+ ]+\)|with|was|\bat\b|\bon\b|\bdate\b|\bfee\b|ref|txid|\.|$))"#,
            #"(?i)(?:sent to\s+)([A-Za-z0-9&'\-\.\s]+?)(?=\s+(?:\([0-9*+ ]+\)|with|was|\bat\b|\bon\b|\bdate\b|\bfee\b|ref|txid|\.|$))"#,
            #"(?i)(?:received from\s+)([A-Za-z0-9&'\-\.\s]+?)(?=\s+(?:\([0-9*+ ]+\)|with|was|\bat\b|\bon\b|\bdate\b|\bfee\b|ref|txid|\.|$))"#,
            #"(?i)(?:from\s+)([A-Za-z0-9&'\-\.\s]+?)(?=\s+(?:\([0-9*+ ]+\)|with|was|\bat\b|\bon\b|\bdate\b|\bfee\b|ref|txid|\.|$))"#
        ]

        for p in patterns {
            if let regex = try? NSRegularExpression(pattern: p) {
                let range = NSRange(location: 0, length: nsBody.length)
                if let match = regex.firstMatch(in: body, options: [], range: range), match.numberOfRanges >= 2 {
                    var candidate = nsBody.substring(with: match.range(at: 1)).trimmingCharacters(in: .whitespacesAndNewlines)
                    candidate = candidate.replacingOccurrences(of: #"\([0-9*+ ]+\)"#, with: "", options: .regularExpression).trimmingCharacters(in: .whitespacesAndNewlines)
                    if !candidate.isEmpty && candidate.lowercased() != "me" && candidate.count >= 2 {
                        return candidate
                    }
                }
            }
        }

        let lowerBody = body.lowercased()
        if lowerBody.contains("airtime") {
            return "Airtime"
        }
        if lowerBody.contains("wasac") {
            return "WASAC"
        }

        let trimmedSender = sender.trimmingCharacters(in: .whitespacesAndNewlines)
        if !trimmedSender.isEmpty && !trimmedSender.allSatisfy({ $0.isNumber || $0 == "+" }) {
            return trimmedSender
        }

        return "Mobile Money Merchant"
    }
}

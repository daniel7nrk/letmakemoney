import Foundation

public struct ParsedSmsResult: Equatable {
    public let amount: Double
    public let currency: String
    public let merchant: String
    public let description: String
    public let category: String
    public let status: String // "PENDING" or "CONFIRMED"

    public init(
        amount: Double,
        currency: String,
        merchant: String,
        description: String,
        category: String,
        status: String
    ) {
        self.amount = amount
        self.currency = currency
        self.merchant = merchant
        self.description = description
        self.category = category
        self.status = status
    }
}

public final class SMSParser {
    public static let shared = SMSParser()

    private let transactionKeywords = [
        "paid", "received", "transferred", "payment", "txn", "txid",
        "debit", "credit", "confirmed", "m-pesa", "momo", "airtel money",
        "bank", "bal:", "balance:", "spent"
    ]

    private let genericSmsWords: Set<String> = [
        "momo", "paid", "received", "payment", "bank", "bill", "transfer",
        "transferred", "spent", "debit", "credit", "confirmed", "airtime",
        "deposit", "money", "sent", "you"
    ]

    private init() {}

    public func parseSms(
        sender: String,
        body: String,
        categories: [Category],
        userCurrency: String
    ) -> ParsedSmsResult? {
        let lowerBody = body.lowercased()

        // Step 1: Check if SMS matches transaction-alert pattern
        let isTransaction = transactionKeywords.contains { lowerBody.contains($0) }
        if !isTransaction {
            return nil
        }

        // Step 2: Extract Amount & Currency
        guard let amountResult = extractAmountAndCurrency(body: body, defaultCurrency: userCurrency) else {
            return nil
        }

        // Step 3: Extract Merchant / Counterparty
        let merchant = extractMerchant(body: body, sender: sender)

        // Step 4: Keyword-based Category Auto-suggestion
        var matchedCategory = "Uncategorized"
        var status = "PENDING"

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

        if matchedCategory == "Uncategorized" {
            status = "PENDING"
        }

        return ParsedSmsResult(
            amount: amountResult.0,
            currency: amountResult.1,
            merchant: merchant,
            description: "\(merchant) (SMS)",
            category: matchedCategory,
            status: status
        )
    }

    private func extractAmountAndCurrency(body: String, defaultCurrency: String) -> (Double, String)? {
        let nsBody = body as NSString

        // Pattern 1: Amount followed by currency (e.g. "4,250 RWF", "4250.00 KES")
        let pattern1 = #"(?i)(?<![A-Za-z0-9])([0-9]{1,3}(?:,[0-9]{3})*(?:\.[0-9]{1,2})?|[0-9]+(?:\.[0-9]{1,2})?)\s*(RWF|FRW|RF|KES|KSH|UGX|TZS|USD|BIF|SSP)(?![A-Za-z0-9])"#
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

        // Pattern 2: Currency followed by amount (e.g. "RWF 4,250", "KSH 4250")
        let pattern2 = #"(?i)(RWF|FRW|RF|KES|KSH|UGX|TZS|USD|BIF|SSP)\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\.[0-9]{1,2})?|[0-9]+(?:\.[0-9]{1,2})?)"#
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

        // Pattern 3: Transaction verb followed by number (e.g. "paid 4,250", "received 15000")
        let pattern3 = #"(?i)(?:paid|received|transferred|spent|sent|debited|credited|sum of|amount of)\s+([0-9]{1,3}(?:,[0-9]{3})*(?:\.[0-9]{1,2})?|[0-9]+(?:\.[0-9]{1,2})?)"#
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
            #"(?i)(?:paid to|payment to|transferred to|sent to|to)\s+([A-Za-z0-9&\s'-]{2,25}?)(?=\s+(?:on|at|date|ref|txid|bal|balance|new|financial|your|is|was|for|\.|\,|$))"#,
            #"(?i)(?:from|received from)\s+([A-Za-z0-9&\s'-]{2,25}?)(?=\s+(?:on|at|date|ref|txid|bal|balance|new|financial|your|is|was|for|\.|\,|$))"#
        ]

        for p in patterns {
            if let regex = try? NSRegularExpression(pattern: p) {
                let range = NSRange(location: 0, length: nsBody.length)
                if let match = regex.firstMatch(in: body, options: [], range: range), match.numberOfRanges >= 2 {
                    let candidate = nsBody.substring(with: match.range(at: 1)).trimmingCharacters(in: .whitespacesAndNewlines)
                    if !candidate.isEmpty {
                        let cleaned = String(candidate.prefix(25)).trimmingCharacters(in: CharacterSet(charactersIn: " .,-"))
                        if cleaned.count >= 2 && cleaned.lowercased() != "me" {
                            return cleaned
                        }
                    }
                }
            }
        }

        let trimmedSender = sender.trimmingCharacters(in: .whitespacesAndNewlines)
        return trimmedSender.isEmpty ? "Mobile Money Merchant" : trimmedSender
    }
}

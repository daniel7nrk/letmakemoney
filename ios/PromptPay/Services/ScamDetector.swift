import Foundation

public enum ScamClassification: String, Codable {
    case confirmedReal = "CONFIRMED_REAL"
    case needsReview = "NEEDS_REVIEW"
    case likelyScam = "LIKELY_SCAM"
}

public struct ScamDetectionResult: Codable {
    public let classification: ScamClassification
    public let isScam: Bool
    public let needsReview: Bool
    public let primaryReason: String
    public let detectedSignals: [String]
    public let extractedNewBalance: Double?

    public init(
        classification: ScamClassification,
        isScam: Bool,
        needsReview: Bool,
        primaryReason: String,
        detectedSignals: [String],
        extractedNewBalance: Double? = nil
    ) {
        self.classification = classification
        self.isScam = isScam
        self.needsReview = needsReview
        self.primaryReason = primaryReason
        self.detectedSignals = detectedSignals
        self.extractedNewBalance = extractedNewBalance
    }
}

public final class ScamDetector {
    public static let shared = ScamDetector()

    private let callToActionKeywords = [
        "click here", "click link", "verify your account", "verify account",
        "enter your pin", "enter pin", "share your pin", "call this number",
        "reply with", "reply code", "otp", "claim your", "claim now",
        "http://", "https://", "www.", ".com/", "bit.ly", "tinyurl",
        "verify here", "click to verify", "update your pin", "free bonus"
    ]

    private let transactionClaimKeywords = [
        "paid", "received", "transferred", "payment", "txn", "txid",
        "debit", "credit", "confirmed", "m-pesa", "momo", "airtel money",
        "bank", "bal:", "balance:", "spent", "sent to", "you have paid",
        "you received", "received from", "deposit", "transfer", "rwf", "etb"
    ]

    private init() {}

    public func detectScam(
        sender: String,
        body: String,
        recentTransactions: [Transaction] = [],
        recentScams: [ScamAlert] = []
    ) -> ScamDetectionResult {
        // If user turned off scam filtering, pass messages through as confirmed real
        if !TrustedSenderManager.shared.isScamFilteringEnabled() {
            let extractedNewBal = extractBalanceFromMessage(body)
            return ScamDetectionResult(
                classification: .confirmedReal,
                isScam: false,
                needsReview: false,
                primaryReason: "Scam filtering is currently turned off.",
                detectedSignals: [],
                extractedNewBalance: extractedNewBal
            )
        }

        let lowerSender = sender.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        let lowerBody = body.lowercased()

        let isTransactionClaim = transactionClaimKeywords.contains { lowerBody.contains($0) }

        // Red Flag Check 1: Call to Action / Phishing / PIN Request
        if let matchedCta = callToActionKeywords.first(where: { lowerBody.contains($0) }) {
            return ScamDetectionResult(
                classification: .likelyScam,
                isScam: true,
                needsReview: false,
                primaryReason: "Contains a suspicious link/request.",
                detectedSignals: ["Phishing / Suspicious CTA ('\(matchedCta)')"]
            )
        }

        // Red Flag Check 2: Duplicate Message / Replay Attack (5 minutes window)
        let now = Int64(Date().timeIntervalSince1970 * 1000)
        let fiveMinutesAgo = now - (5 * 60 * 1000)

        let isDuplicateTx = recentTransactions.contains { tx in
            guard tx.dateEpochMillis >= fiveMinutesAgo else { return false }
            let matchSender = tx.paymentMethod.caseInsensitiveCompare(sender) == .orderedSame ||
                              tx.description.localizedCaseInsensitiveContains(sender)
            let amountString = String(Int(tx.amount))
            return matchSender && lowerBody.contains(amountString)
        }

        let isDuplicateScam = recentScams.contains { scam in
            scam.dateEpochMillis >= fiveMinutesAgo &&
            scam.sender.caseInsensitiveCompare(sender) == .orderedSame &&
            scam.body.caseInsensitiveCompare(body) == .orderedSame
        }

        if isDuplicateTx || isDuplicateScam {
            return ScamDetectionResult(
                classification: .likelyScam,
                isScam: true,
                needsReview: false,
                primaryReason: "Duplicate transaction message.",
                detectedSignals: ["Duplicate Message Pattern"]
            )
        }

        if !isTransactionClaim {
            // Not a transaction claim at all, and no CTA
            return ScamDetectionResult(
                classification: .confirmedReal,
                isScam: false,
                needsReview: false,
                primaryReason: "",
                detectedSignals: []
            )
        }

        // --- Structural Fingerprint Verification ---
        let isMoMo = isMoMoProviderOrClaim(lowerSender: lowerSender, lowerBody: lowerBody)
        let isBank = isBankProviderOrClaim(lowerSender: lowerSender, lowerBody: lowerBody)

        var structurePassed = true
        var structureFailureReason: String? = nil

        if isMoMo {
            if !matchesMoMoUSSDWrapper(body) {
                structurePassed = false
                structureFailureReason = "Missing expected MoMo message structure."
            }
        } else if isBank {
            if !matchesBankStructure(body) {
                structurePassed = false
                structureFailureReason = "Missing expected bank message structure."
            }
        } else {
            if !matchesGenericStructure(body) {
                structurePassed = false
                structureFailureReason = "Missing expected transaction message structure."
            }
        }

        // --- Sequential Balance-Chain Verification ---
        let extractedNewBalance = extractBalanceFromMessage(body)
        let extractedAmount = extractAmountFromMessage(body) ?? 0.0
        let extractedFee = extractFeeFromMessage(body) ?? 0.0
        let isDebit = isDebitTransaction(lowerBody)

        let lastKnownBalance = TrustedSenderManager.shared.getLastBalance(for: sender)

        var balanceChainPassed = true
        var balanceFailureReason: String? = nil

        if let lastBal = lastKnownBalance, let newBal = extractedNewBalance, extractedAmount > 0 {
            let expectedBalance = isDebit ? (lastBal - extractedAmount - extractedFee) : (lastBal + extractedAmount)
            let diff = abs(newBal - expectedBalance)
            let tolerance = max(100.0, lastBal * 0.15)

            if diff > tolerance {
                balanceChainPassed = false
                balanceFailureReason = "Balance doesn't match expected amount based on your last transaction."
            }
        }

        // --- Sender Identity Verification ---
        let isKnownSender = TrustedSenderManager.shared.isKnownOrTrustedSender(sender)

        var failedReasons: [String] = []
        var detectedSignals: [String] = []

        if !structurePassed, let reason = structureFailureReason {
            failedReasons.append(reason)
            detectedSignals.append("Invalid Structure")
        }

        if !balanceChainPassed, let reason = balanceFailureReason {
            failedReasons.append(reason)
            detectedSignals.append("Balance Chain Discrepancy")
        }

        if !isKnownSender {
            detectedSignals.append("Unrecognized Sender (\(sender))")
        }

        // --- Classification Rules ---
        if !structurePassed {
            return ScamDetectionResult(
                classification: .likelyScam,
                isScam: true,
                needsReview: false,
                primaryReason: failedReasons.joined(separator: " "),
                detectedSignals: detectedSignals,
                extractedNewBalance: extractedNewBalance
            )
        }

        if !balanceChainPassed && !isKnownSender {
            return ScamDetectionResult(
                classification: .likelyScam,
                isScam: true,
                needsReview: false,
                primaryReason: failedReasons.joined(separator: " "),
                detectedSignals: detectedSignals,
                extractedNewBalance: extractedNewBalance
            )
        }

        if !isKnownSender {
            return ScamDetectionResult(
                classification: .needsReview,
                isScam: false,
                needsReview: true,
                primaryReason: "Sender not recognized.",
                detectedSignals: detectedSignals,
                extractedNewBalance: extractedNewBalance
            )
        }

        if !balanceChainPassed {
            let reason = balanceFailureReason ?? "Balance doesn't match expected amount based on your last transaction."
            return ScamDetectionResult(
                classification: .needsReview,
                isScam: false,
                needsReview: true,
                primaryReason: reason,
                detectedSignals: detectedSignals,
                extractedNewBalance: extractedNewBalance
            )
        }

        return ScamDetectionResult(
            classification: .confirmedReal,
            isScam: false,
            needsReview: false,
            primaryReason: "",
            detectedSignals: [],
            extractedNewBalance: extractedNewBalance
        )
    }

    private func isMoMoProviderOrClaim(lowerSender: String, lowerBody: String) -> Bool {
        let momoKeywords = ["momo", "m-money", "mtn", "mtnmomo", "mtnrwanda", "rwf", "ugx", "kes", "payment of", "transferred to"]
        return momoKeywords.contains { lowerSender.contains($0) || lowerBody.contains($0) }
    }

    private func isBankProviderOrClaim(lowerSender: String, lowerBody: String) -> Bool {
        let bankKeywords = ["127", "131", "251994", "cbe", "boa", "bank", "telebirr", "etb", "urubutopay", "dear"]
        return bankKeywords.contains { lowerSender.contains($0) || lowerBody.contains($0) }
    }

    private func matchesMoMoUSSDWrapper(_ body: String) -> Bool {
        let trimmed = body.trimmingCharacters(in: .whitespacesAndNewlines)
        let pattern = #"^\*\d{3,4}\*.*(?:\*EN#|\.EN#|\*FR#|\*KIN#|EN#|#)$"#
        if let regex = try? NSRegularExpression(pattern: pattern, options: [.caseInsensitive, .dotMatchesLineSeparators]) {
            let range = NSRange(location: 0, length: trimmed.utf16.count)
            if regex.firstMatch(in: trimmed, options: [], range: range) != nil {
                return true
            }
        }
        if trimmed.contains("*S*") || trimmed.contains("*R*") || trimmed.contains("*TxId:") || trimmed.contains("TxId:") {
            if trimmed.hasSuffix("EN#") || trimmed.hasSuffix("FR#") || trimmed.hasSuffix("#") {
                return true
            }
        }
        return false
    }

    private func matchesBankStructure(_ body: String) -> Bool {
        let lower = body.lowercased()
        let hasDear = lower.contains("dear ")
        let hasTxNumber = lower.contains("transaction number") || lower.contains("txn number") || lower.contains("txid") || lower.contains("ref") || lower.contains("by transaction")
        let hasBalance = lower.contains("balance is") || lower.contains("current balance") || lower.contains("new balance") || lower.contains("account")

        return (hasDear && hasTxNumber && hasBalance) || (hasDear && hasTxNumber) || (hasTxNumber && hasBalance)
    }

    private func matchesGenericStructure(_ body: String) -> Bool {
        let lower = body.lowercased()
        let hasAmount = extractAmountFromMessage(body) != nil
        let hasAction = lower.contains("paid") || lower.contains("received") || lower.contains("transferred") || lower.contains("debited") || lower.contains("credited")
        let hasBalance = lower.contains("balance") || lower.contains("bal:") || lower.contains("bal")
        return hasAmount && (hasAction || hasBalance)
    }

    private func isDebitTransaction(_ lowerBody: String) -> Bool {
        return lowerBody.contains("payment of") || lowerBody.contains("transferred to") ||
               lowerBody.contains("paid") || lowerBody.contains("debited") || lowerBody.contains("sent to")
    }

    public func extractBalanceFromMessage(_ body: String) -> Double? {
        let pattern = #"(?i)(?:new balance|current balance|bal|balance|balance is)[:\s]+(?:[A-Za-z]{3}\s*)?([0-9]{1,3}(?:,[0-9]{3})*(?:\.[0-9]{1,2})?|[0-9]+(?:\.[0-9]{1,2})?)"#
        if let regex = try? NSRegularExpression(pattern: pattern) {
            let nsString = body as NSString
            let range = NSRange(location: 0, length: nsString.length)
            if let match = regex.firstMatch(in: body, options: [], range: range), match.numberOfRanges > 1 {
                let matchedStr = nsString.substring(with: match.range(at: 1)).replacingOccurrences(of: ",", with: "")
                return Double(matchedStr)
            }
        }
        return nil
    }

    public func extractAmountFromMessage(_ body: String) -> Double? {
        let pattern = #"(?i)(?:payment of|received|transferred|paid|credited|debited|amount|sum of)\s+(?:[A-Za-z]{3}\s*)?([0-9]{1,3}(?:,[0-9]{3})*(?:\.[0-9]{1,2})?|[0-9]+(?:\.[0-9]{1,2})?)"#
        if let regex = try? NSRegularExpression(pattern: pattern) {
            let nsString = body as NSString
            let range = NSRange(location: 0, length: nsString.length)
            if let match = regex.firstMatch(in: body, options: [], range: range), match.numberOfRanges > 1 {
                let matchedStr = nsString.substring(with: match.range(at: 1)).replacingOccurrences(of: ",", with: "")
                return Double(matchedStr)
            }
        }
        return nil
    }

    public func extractFeeFromMessage(_ body: String) -> Double? {
        let pattern = #"(?i)(?:fee was|fee:)\s*(?:[A-Za-z]{3}\s*)?([0-9]+(?:\.[0-9]{1,2})?)"#
        if let regex = try? NSRegularExpression(pattern: pattern) {
            let nsString = body as NSString
            let range = NSRange(location: 0, length: nsString.length)
            if let match = regex.firstMatch(in: body, options: [], range: range), match.numberOfRanges > 1 {
                let matchedStr = nsString.substring(with: match.range(at: 1)).replacingOccurrences(of: ",", with: "")
                return Double(matchedStr)
            }
        }
        return nil
    }
}

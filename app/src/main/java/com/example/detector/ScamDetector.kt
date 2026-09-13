package com.example.detector

import android.content.Context
import com.example.data.ScamAlertEntity
import com.example.data.TransactionEntity
import java.util.regex.Pattern

enum class ScamClassification {
    CONFIRMED_REAL,
    NEEDS_REVIEW,
    LIKELY_SCAM
}

data class ScamDetectionResult(
    val classification: ScamClassification,
    val isScam: Boolean,
    val needsReview: Boolean,
    val primaryReason: String,
    val detectedSignals: List<String>,
    val extractedNewBalance: Double? = null
)

object ScamDetector {

    private val CALL_TO_ACTION_KEYWORDS = listOf(
        "click here", "click link", "verify your account", "verify account",
        "enter your pin", "enter pin", "share your pin", "call this number",
        "reply with", "reply code", "otp", "claim your", "claim now",
        "http://", "https://", "www.", ".com/", "bit.ly", "tinyurl",
        "verify here", "click to verify", "update your pin", "free bonus"
    )

    private val TRANSACTION_CLAIM_KEYWORDS = listOf(
        "paid", "received", "transferred", "payment", "txn", "txid",
        "debit", "credit", "confirmed", "m-pesa", "momo", "airtel money",
        "bank", "bal:", "balance:", "spent", "sent to", "you have paid",
        "you received", "received from", "deposit", "transfer", "rwf", "etb"
    )

    suspend fun detectScam(
        context: Context,
        sender: String,
        body: String,
        recentTransactions: List<TransactionEntity> = emptyList(),
        recentScams: List<ScamAlertEntity> = emptyList()
    ): ScamDetectionResult {
        // If user turned off scam filtering, pass messages through as confirmed real
        if (!TrustedSenderManager.isScamFilteringEnabled(context)) {
            val extractedNewBal = extractBalanceFromMessage(body)
            return ScamDetectionResult(
                classification = ScamClassification.CONFIRMED_REAL,
                isScam = false,
                needsReview = false,
                primaryReason = "Scam filtering is currently turned off.",
                detectedSignals = emptyList(),
                extractedNewBalance = extractedNewBal
            )
        }

        val lowerSender = sender.trim().lowercase()
        val lowerBody = body.lowercase()

        val isTransactionClaim = TRANSACTION_CLAIM_KEYWORDS.any { lowerBody.contains(it) }

        // Red Flag Check 1: Call to Action / Phishing / PIN Request
        val matchedCta = CALL_TO_ACTION_KEYWORDS.firstOrNull { lowerBody.contains(it) }
        if (matchedCta != null) {
            val reason = "Contains a suspicious link/request."
            return ScamDetectionResult(
                classification = ScamClassification.LIKELY_SCAM,
                isScam = true,
                needsReview = false,
                primaryReason = reason,
                detectedSignals = listOf("Phishing / Suspicious CTA ('$matchedCta')")
            )
        }

        // Red Flag Check 2: Duplicate Message / Replay Attack
        val now = System.currentTimeMillis()
        val fiveMinutesAgo = now - (5 * 60 * 1000L)
        val isDuplicateTx = recentTransactions.any { tx ->
            tx.dateEpochMillis >= fiveMinutesAgo &&
            (tx.paymentMethod.equals(sender, ignoreCase = true) || tx.description.contains(sender, ignoreCase = true)) &&
            lowerBody.contains(tx.amount.toInt().toString())
        }
        val isDuplicateScam = recentScams.any { scam ->
            scam.dateEpochMillis >= fiveMinutesAgo &&
            scam.sender.equals(sender, ignoreCase = true) &&
            scam.body.equals(body, ignoreCase = true)
        }

        if (isDuplicateTx || isDuplicateScam) {
            val reason = "Duplicate transaction message."
            return ScamDetectionResult(
                classification = ScamClassification.LIKELY_SCAM,
                isScam = true,
                needsReview = false,
                primaryReason = reason,
                detectedSignals = listOf("Duplicate Message Pattern")
            )
        }

        if (!isTransactionClaim) {
            // Not a transaction claim at all, and no CTA
            return ScamDetectionResult(
                classification = ScamClassification.CONFIRMED_REAL,
                isScam = false,
                needsReview = false,
                primaryReason = "",
                detectedSignals = emptyList()
            )
        }

        // --- Structural Fingerprint Verification ---
        val isMoMoProvider = isMoMoProviderOrClaim(lowerSender, lowerBody)
        val isBankProvider = isBankProviderOrClaim(lowerSender, lowerBody)

        var structurePassed = true
        var structureFailureReason: String? = null

        if (isMoMoProvider) {
            if (!matchesMoMoUSSDWrapper(body)) {
                structurePassed = false
                structureFailureReason = "Missing expected MoMo message structure."
            }
        } else if (isBankProvider) {
            if (!matchesBankStructure(body)) {
                structurePassed = false
                structureFailureReason = "Missing expected bank message structure."
            }
        } else {
            // Generic transaction claim
            if (!matchesGenericStructure(body)) {
                structurePassed = false
                structureFailureReason = "Missing expected transaction message structure."
            }
        }

        // --- Sequential Balance-Chain Verification ---
        val extractedNewBalance = extractBalanceFromMessage(body)
        val extractedAmount = extractAmountFromMessage(body) ?: 0.0
        val extractedFee = extractFeeFromMessage(body) ?: 0.0
        val isDebit = isDebitTransaction(lowerBody)

        val lastKnownBalance = TrustedSenderManager.getLastBalance(context, sender)

        var balanceChainPassed = true
        var balanceFailureReason: String? = null

        if (lastKnownBalance != null && extractedNewBalance != null && extractedAmount > 0) {
            val expectedBalance = if (isDebit) {
                lastKnownBalance - extractedAmount - extractedFee
            } else {
                lastKnownBalance + extractedAmount
            }

            val diff = Math.abs(extractedNewBalance - expectedBalance)
            val tolerance = Math.max(100.0, lastKnownBalance * 0.15)

            if (diff > tolerance) {
                balanceChainPassed = false
                balanceFailureReason = "Balance doesn't match expected amount based on your last transaction."
            }
        }

        // --- Sender Identity Verification ---
        val isKnownSender = TrustedSenderManager.isKnownOrTrustedSender(context, sender)

        // --- Combine Signals & Determine Classification ---
        val failedReasons = mutableListOf<String>()
        val detectedSignals = mutableListOf<String>()

        if (!structurePassed && structureFailureReason != null) {
            failedReasons.add(structureFailureReason)
            detectedSignals.add("Invalid Structure")
        }

        if (!balanceChainPassed && balanceFailureReason != null) {
            failedReasons.add(balanceFailureReason)
            detectedSignals.add("Balance Chain Discrepancy")
        }

        if (!isKnownSender) {
            detectedSignals.add("Unrecognized Sender ($sender)")
        }

        // Classification Rules:
        if (!structurePassed) {
            // Missing required structure for provider type -> LIKELY_SCAM
            val primaryReason = failedReasons.joinToString(" ")
            return ScamDetectionResult(
                classification = ScamClassification.LIKELY_SCAM,
                isScam = true,
                needsReview = false,
                primaryReason = primaryReason,
                detectedSignals = detectedSignals,
                extractedNewBalance = extractedNewBalance
            )
        }

        if (!balanceChainPassed && !isKnownSender) {
            // Both balance chain failed AND sender unknown -> LIKELY_SCAM
            val primaryReason = failedReasons.joinToString(" ")
            return ScamDetectionResult(
                classification = ScamClassification.LIKELY_SCAM,
                isScam = true,
                needsReview = false,
                primaryReason = primaryReason,
                detectedSignals = detectedSignals,
                extractedNewBalance = extractedNewBalance
            )
        }

        if (!isKnownSender) {
            // Correct structure, consistent balance, but unknown sender -> NEEDS_REVIEW
            val reason = "Sender not recognized."
            return ScamDetectionResult(
                classification = ScamClassification.NEEDS_REVIEW,
                isScam = false,
                needsReview = true,
                primaryReason = reason,
                detectedSignals = detectedSignals,
                extractedNewBalance = extractedNewBalance
            )
        }

        if (!balanceChainPassed) {
            // Known sender, correct structure, but balance math mismatch -> NEEDS_REVIEW
            val reason = balanceFailureReason ?: "Balance doesn't match expected amount based on your last transaction."
            return ScamDetectionResult(
                classification = ScamClassification.NEEDS_REVIEW,
                isScam = false,
                needsReview = true,
                primaryReason = reason,
                detectedSignals = detectedSignals,
                extractedNewBalance = extractedNewBalance
            )
        }

        // Known sender + Correct structure + Consistent balance chain -> CONFIRMED_REAL
        return ScamDetectionResult(
            classification = ScamClassification.CONFIRMED_REAL,
            isScam = false,
            needsReview = false,
            primaryReason = "",
            detectedSignals = emptyList(),
            extractedNewBalance = extractedNewBalance
        )
    }

    private fun isMoMoProviderOrClaim(lowerSender: String, lowerBody: String): Boolean {
        val momoKeywords = listOf("momo", "m-money", "mtn", "mtnmomo", "mtnrwanda", "rwf", "ugx", "kes", "payment of", "transferred to")
        return momoKeywords.any { lowerSender.contains(it) || lowerBody.contains(it) }
    }

    private fun isBankProviderOrClaim(lowerSender: String, lowerBody: String): Boolean {
        val bankKeywords = listOf("127", "131", "251994", "cbe", "boa", "bank", "telebirr", "etb", "urubutopay", "dear")
        return bankKeywords.any { lowerSender.contains(it) || lowerBody.contains(it) }
    }

    private fun matchesMoMoUSSDWrapper(body: String): Boolean {
        val trimmed = body.trim()
        val ussdWrapperPattern = Pattern.compile("(?s)^\\*\\d{3,4}\\*.*(?:\\*EN#|\\.EN#|\\*FR#|\\*KIN#|EN#|#)$", Pattern.CASE_INSENSITIVE)
        if (ussdWrapperPattern.matcher(trimmed).matches()) {
            return true
        }
        if (trimmed.contains("*S*") || trimmed.contains("*R*") || trimmed.contains("*TxId:") || trimmed.contains("TxId:")) {
            if (trimmed.endsWith("EN#") || trimmed.endsWith("FR#") || trimmed.endsWith("#")) {
                return true
            }
        }
        return false
    }

    private fun matchesBankStructure(body: String): Boolean {
        val lower = body.lowercase()
        val hasDear = lower.contains("dear ")
        val hasTxNumber = lower.contains("transaction number") || lower.contains("txn number") || lower.contains("txid") || lower.contains("ref") || lower.contains("by transaction")
        val hasBalance = lower.contains("balance is") || lower.contains("current balance") || lower.contains("new balance") || lower.contains("account")

        return (hasDear && hasTxNumber && hasBalance) || (hasDear && hasTxNumber) || (hasTxNumber && hasBalance)
    }

    private fun matchesGenericStructure(body: String): Boolean {
        val lower = body.lowercase()
        val hasAmount = extractAmountFromMessage(body) != null
        val hasAction = lower.contains("paid") || lower.contains("received") || lower.contains("transferred") || lower.contains("debited") || lower.contains("credited")
        val hasBalance = lower.contains("balance") || lower.contains("bal:") || lower.contains("bal")
        return hasAmount && (hasAction || hasBalance)
    }

    private fun isDebitTransaction(lowerBody: String): Boolean {
        return lowerBody.contains("payment of") || lowerBody.contains("transferred to") ||
               lowerBody.contains("paid") || lowerBody.contains("debited") || lowerBody.contains("sent to")
    }

    private fun extractBalanceFromMessage(body: String): Double? {
        val pattern = Pattern.compile("(?i)(?:new balance|current balance|bal|balance|balance is)[:\\s]+(?:[A-Za-z]{3}\\s*)?([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{1,2})?|[0-9]+(?:\\.[0-9]{1,2})?)")
        val matcher = pattern.matcher(body)
        if (matcher.find()) {
            val str = matcher.group(1)?.replace(",", "")
            return str?.toDoubleOrNull()
        }
        return null
    }

    private fun extractAmountFromMessage(body: String): Double? {
        val pattern = Pattern.compile("(?i)(?:payment of|received|transferred|paid|credited|debited|amount|sum of)\\s+(?:[A-Za-z]{3}\\s*)?([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{1,2})?|[0-9]+(?:\\.[0-9]{1,2})?)")
        val matcher = pattern.matcher(body)
        if (matcher.find()) {
            val str = matcher.group(1)?.replace(",", "")
            return str?.toDoubleOrNull()
        }
        return null
    }

    private fun extractFeeFromMessage(body: String): Double? {
        val pattern = Pattern.compile("(?i)(?:fee was|fee:)\\s*(?:[A-Za-z]{3}\\s*)?([0-9]+(?:\\.[0-9]{1,2})?)")
        val matcher = pattern.matcher(body)
        if (matcher.find()) {
            val str = matcher.group(1)?.replace(",", "")
            return str?.toDoubleOrNull()
        }
        return null
    }
}

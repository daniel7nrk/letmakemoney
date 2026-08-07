package com.example.detector

import com.example.data.ScamAlertEntity
import com.example.data.TransactionEntity
import java.util.regex.Pattern

data class ScamDetectionResult(
    val isScam: Boolean,
    val score: Int,
    val primaryReason: String,
    val detectedSignals: List<String>
)

object ScamDetector {

    // Configurable list of known verified MoMo/bank sender identifiers & shortcodes
    private val VERIFIED_SENDERS = listOf(
        "momo", "m-money", "mtn", "mtnmomo", "airtel", "airtelmoney",
        "m-pesa", "mpesa", "equity", "equitybank", "bk", "bank",
        "ecobank", "i&m", "kcb", "coopbank", "kcb bank", "stanbic",
        "promptpay", "bankofkigali", "kcb_bank"
    )

    private val TRANSACTION_KEYWORDS = listOf(
        "paid", "received", "transferred", "payment", "txn", "txid",
        "debit", "credit", "confirmed", "m-pesa", "momo", "airtel money",
        "bank", "bal:", "balance:", "spent", "sent to", "you have paid",
        "you received", "received from", "deposit", "transfer"
    )

    private val CALL_TO_ACTION_KEYWORDS = listOf(
        "click here", "click link", "verify your account", "verify account",
        "enter your pin", "enter pin", "share your pin", "call this number",
        "reply with", "reply code", "otp", "claim your", "claim now",
        "http://", "https://", "www.", ".com/", "bit.ly", "tinyurl",
        "verify here", "click to verify", "update your pin", "free bonus"
    )

    suspend fun detectScam(
        sender: String,
        body: String,
        recentTransactions: List<TransactionEntity>,
        recentScams: List<ScamAlertEntity>,
        currentAccountBalance: Double? = null
    ): ScamDetectionResult {
        val lowerSender = sender.trim().lowercase()
        val lowerBody = body.lowercase()

        val isTransactionClaim = TRANSACTION_KEYWORDS.any { lowerBody.contains(it) }

        if (!isTransactionClaim) {
            // Not claiming to be a transaction, check if it's purely spam/phishing CTA
            val hasCta = CALL_TO_ACTION_KEYWORDS.any { lowerBody.contains(it) }
            if (hasCta) {
                return ScamDetectionResult(
                    isScam = true,
                    score = 4,
                    primaryReason = "Suspicious promotional or phishing message urging action.",
                    detectedSignals = listOf("Call-To-Action Language")
                )
            }
            return ScamDetectionResult(
                isScam = false,
                score = 0,
                primaryReason = "",
                detectedSignals = emptyList()
            )
        }

        // It claims to be a transaction alert!
        val normalizedSender = lowerSender.replace(" ", "").replace("-", "")
        val isVerifiedSender = VERIFIED_SENDERS.any { verified ->
            normalizedSender.contains(verified)
        }

        val signals = mutableListOf<String>()
        val reasons = mutableListOf<String>()

        // Check Duplicate / Replay Pattern (Signal)
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
            signals.add("Duplicate Message Pattern")
            reasons.add("is a duplicate of a recently received transaction message")
            return ScamDetectionResult(
                isScam = true,
                score = 5,
                primaryReason = "This message is a duplicate of a recently received transaction message.",
                detectedSignals = signals
            )
        }

        // PRIMARY DOMINANT SIGNAL: Sender Verification
        if (!isVerifiedSender) {
            signals.add("Unverified Sender Identifier ($sender)")
            reasons.add("This message looks like a transaction alert but wasn't sent from a verified MoMo/bank source.")

            // Check if there are additional red flags like CTA or balance mismatch to add to explanation
            val matchedCta = CALL_TO_ACTION_KEYWORDS.firstOrNull { lowerBody.contains(it) }
            if (matchedCta != null) {
                signals.add("Call-To-Action Language ('$matchedCta')")
                reasons.add("asks you to click a link or take suspicious action")
            }

            if (currentAccountBalance != null && currentAccountBalance > 0) {
                val extractedBal = extractBalanceFromMessage(body)
                if (extractedBal != null && Math.abs(extractedBal - currentAccountBalance) > 50.0) {
                    signals.add("Balance Mismatch")
                    reasons.add("contains a balance statement that does not match your account")
                }
            }

            val explanation = reasons.joinToString(" ")
            return ScamDetectionResult(
                isScam = true,
                score = 5,
                primaryReason = explanation,
                detectedSignals = signals
            )
        }

        // Verified sender, but check for explicit phishing/CTA or balance anomaly
        val matchedCta = CALL_TO_ACTION_KEYWORDS.firstOrNull { lowerBody.contains(it) }
        if (matchedCta != null) {
            signals.add("Call-To-Action Language ('$matchedCta')")
            return ScamDetectionResult(
                isScam = true,
                score = 4,
                primaryReason = "Message from verified sender contains suspicious call-to-action language ('$matchedCta').",
                detectedSignals = signals
            )
        }

        // Verified sender + valid structure -> REAL TRANSACTION
        return ScamDetectionResult(
            isScam = false,
            score = 0,
            primaryReason = "",
            detectedSignals = emptyList()
        )
    }

    private fun extractBalanceFromMessage(body: String): Double? {
        val pattern = Pattern.compile("(?i)(?:bal|balance|new bal|new balance)[:\\s]+([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{1,2})?|[0-9]+(?:\\.[0-9]{1,2})?)")
        val matcher = pattern.matcher(body)
        if (matcher.find()) {
            val str = matcher.group(1)?.replace(",", "")
            return str?.toDoubleOrNull()
        }
        return null
    }
}

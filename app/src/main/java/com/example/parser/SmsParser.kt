package com.example.parser

import com.example.data.CategoryEntity
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.regex.Pattern

data class ParsedSmsResult(
    val amount: Double,
    val currency: String,
    val merchant: String,
    val description: String,
    val category: String,
    val status: String, // "PENDING" or "CONFIRMED"
    val fee: Double = 0.0,
    val feeCategory: String = "Transaction Fee",
    val shouldRecord: Boolean = true,
    val timestampEpochMillis: Long? = null
)

object SmsParser {

    private val TRANSACTION_KEYWORDS = listOf(
        "paid", "received", "transferred", "payment", "txn", "txid",
        "debit", "credit", "confirmed", "m-pesa", "momo", "airtel money",
        "bank", "bal:", "balance:", "spent"
    )

    // Common SMS terms that should NOT count as category-specific keyword matches
    private val GENERIC_SMS_WORDS = setOf(
        "momo", "paid", "received", "payment", "bank", "bill", "transfer",
        "transferred", "spent", "debit", "credit", "confirmed", "airtime",
        "deposit", "money", "sent", "you"
    )

    fun parseSms(
        sender: String,
        body: String,
        categories: List<CategoryEntity>,
        userCurrency: String
    ): ParsedSmsResult? {
        val lowerBody = body.lowercase()

        // Step 1: Check if SMS matches transaction-alert pattern
        val isTransaction = TRANSACTION_KEYWORDS.any { lowerBody.contains(it) }
        if (!isTransaction) {
            return null // Ignore non-transaction SMS completely
        }

        // Determine if message is incoming/received money (Record = No / No Leave it)
        val isReceived = lowerBody.contains("you have received") ||
                (lowerBody.contains("received") && lowerBody.contains("from") &&
                        !lowerBody.contains("payment") && !lowerBody.contains("transferred"))
        val shouldRecord = !isReceived

        // Step 2: Extract Amount & Currency
        val amountResult = extractAmountAndCurrency(body, userCurrency) ?: return null

        // Step 3: Extract Merchant / Counterparty
        val merchant = extractMerchant(body, sender)

        // Step 4: Extract Fee
        val fee = extractFee(body)

        // Step 5: Extract Date & Time
        val timestamp = extractDate(body)

        // Step 6: Keyword-based Category Auto-suggestion
        var matchedCategory = "Uncategorized"
        var status = "PENDING"

        for (cat in categories) {
            val keywordsList = cat.keywords.split(",")
                .map { it.trim().lowercase() }
                .filter { it.isNotEmpty() && !GENERIC_SMS_WORDS.contains(it) }

            val foundMatch = keywordsList.any { kw ->
                lowerBody.contains(kw) || merchant.lowercase().contains(kw)
            }
            if (foundMatch) {
                matchedCategory = cat.name
                status = "CONFIRMED"
                break
            }
        }

        // Auto-categorize known common patterns if still uncategorized
        if (matchedCategory == "Uncategorized") {
            if (merchant.equals("Airtime", ignoreCase = true)) {
                matchedCategory = categories.find { it.name.contains("Bill", ignoreCase = true) }?.name ?: "Bills"
                status = "CONFIRMED"
            } else if (merchant.equals("WASAC", ignoreCase = true)) {
                matchedCategory = categories.find { it.name.contains("Utilities", ignoreCase = true) }?.name ?: "Rent & Utilities"
                status = "CONFIRMED"
            }
        }

        if (matchedCategory == "Uncategorized") {
            status = "PENDING"
        }

        return ParsedSmsResult(
            amount = amountResult.first,
            currency = amountResult.second,
            merchant = merchant,
            description = "$merchant (SMS)",
            category = matchedCategory,
            status = status,
            fee = fee,
            feeCategory = "Transaction Fee",
            shouldRecord = shouldRecord,
            timestampEpochMillis = timestamp
        )
    }

    private fun extractAmountAndCurrency(body: String, defaultCurrency: String): Pair<Double, String>? {
        // High-Priority Pattern A: Payment phrase (e.g. "Your payment of 28108 RWF", "payment of 10,050 RWF to...")
        val paymentPattern = Pattern.compile("(?i)(?:your payment of|payment of)\\s+([0-9,]+(?:\\.[0-9]{1,2})?)\\s*(RWF|FRW|RF|KES|KSH|UGX|TZS|USD|BIF|SSP)?")
        val paymentMatcher = paymentPattern.matcher(body)
        if (paymentMatcher.find()) {
            val numStr = paymentMatcher.group(1)?.replace(",", "")
            val currGroup = paymentMatcher.group(2)?.uppercase() ?: defaultCurrency
            val parsedDouble = numStr?.toDoubleOrNull()
            if (parsedDouble != null && parsedDouble > 0) {
                return Pair(parsedDouble, normalizeCurrency(currGroup))
            }
        }

        // High-Priority Pattern B: Transfer phrase (e.g. "665 RWF transferred to...", "transferred to...")
        val transferPattern = Pattern.compile("(?i)([0-9,]+(?:\\.[0-9]{1,2})?)\\s*(RWF|FRW|RF|KES|KSH|UGX|TZS|USD|BIF|SSP)?\\s+transferred to")
        val transferMatcher = transferPattern.matcher(body)
        if (transferMatcher.find()) {
            val numStr = transferMatcher.group(1)?.replace(",", "")
            val currGroup = transferMatcher.group(2)?.uppercase() ?: defaultCurrency
            val parsedDouble = numStr?.toDoubleOrNull()
            if (parsedDouble != null && parsedDouble > 0) {
                return Pair(parsedDouble, normalizeCurrency(currGroup))
            }
        }

        // High-Priority Pattern C: Received phrase (e.g. "You have received 30000 RWF from...")
        val receivedPattern = Pattern.compile("(?i)(?:you have received|received)\\s+([0-9,]+(?:\\.[0-9]{1,2})?)\\s*(RWF|FRW|RF|KES|KSH|UGX|TZS|USD|BIF|SSP)?\\s+from")
        val receivedMatcher = receivedPattern.matcher(body)
        if (receivedMatcher.find()) {
            val numStr = receivedMatcher.group(1)?.replace(",", "")
            val currGroup = receivedMatcher.group(2)?.uppercase() ?: defaultCurrency
            val parsedDouble = numStr?.toDoubleOrNull()
            if (parsedDouble != null && parsedDouble > 0) {
                return Pair(parsedDouble, normalizeCurrency(currGroup))
            }
        }

        // Pattern 1: Amount followed by currency (e.g. "4,250 RWF", "4250.00 KES")
        val pattern1 = Pattern.compile("(?i)(?<![A-Za-z0-9])([0-9,]+(?:\\.[0-9]{1,2})?)\\s*(RWF|FRW|RF|KES|KSH|UGX|TZS|USD|BIF|SSP)(?![A-Za-z0-9])")
        val matcher1 = pattern1.matcher(body)
        if (matcher1.find()) {
            val numberStr = matcher1.group(1)?.replace(",", "")
            val currGroup = matcher1.group(2)?.uppercase() ?: defaultCurrency
            val parsedDouble = numberStr?.toDoubleOrNull()
            if (parsedDouble != null && parsedDouble > 0) {
                return Pair(parsedDouble, normalizeCurrency(currGroup))
            }
        }

        // Pattern 2: Currency followed by amount (e.g. "RWF 4,250", "KSH 4250")
        val pattern2 = Pattern.compile("(?i)(RWF|FRW|RF|KES|KSH|UGX|TZS|USD|BIF|SSP)\\s*([0-9,]+(?:\\.[0-9]{1,2})?)")
        val matcher2 = pattern2.matcher(body)
        if (matcher2.find()) {
            val currGroup = matcher2.group(1)?.uppercase() ?: defaultCurrency
            val numberStr = matcher2.group(2)?.replace(",", "")
            val parsedDouble = numberStr?.toDoubleOrNull()
            if (parsedDouble != null && parsedDouble > 0) {
                return Pair(parsedDouble, normalizeCurrency(currGroup))
            }
        }

        // Pattern 3: Transaction verb followed by number (e.g. "paid 4,250", "received 15000")
        val pattern3 = Pattern.compile("(?i)(?:paid|received|transferred|spent|sent|debited|credited|sum of|amount of)\\s+([0-9,]+(?:\\.[0-9]{1,2})?)")
        val matcher3 = pattern3.matcher(body)
        if (matcher3.find()) {
            val numberStr = matcher3.group(1)?.replace(",", "")
            val parsedDouble = numberStr?.toDoubleOrNull()
            if (parsedDouble != null && parsedDouble > 0) {
                return Pair(parsedDouble, defaultCurrency)
            }
        }

        return null
    }

    private fun normalizeCurrency(curr: String): String {
        return when (curr.uppercase()) {
            "FRW", "RF" -> "RWF"
            "KSH" -> "KES"
            else -> curr.uppercase()
        }
    }

    private fun extractFee(body: String): Double {
        // Matches: "Fee 0 RWF", "Fee 300 RWF", "Fee was 20 RWF", ".Fee: 20RWF.", ".Fee: 100RWF.", "Fee: 20 RWF"
        val pattern = Pattern.compile("(?i)(?:fee\\s+was|fee:|fee)\\s*([0-9,]+(?:\\.[0-9]{1,2})?)\\s*(?:RWF|FRW|RF|KES|KSH|UGX|TZS|USD|BIF|SSP)?")
        val matcher = pattern.matcher(body)
        if (matcher.find()) {
            val numStr = matcher.group(1)?.replace(",", "")
            return numStr?.toDoubleOrNull() ?: 0.0
        }
        return 0.0
    }

    private fun extractDate(body: String): Long? {
        val pattern = Pattern.compile("(?i)(?:at|completed at)\\s+([0-9]{4}-[0-9]{2}-[0-9]{2}\\s+[0-9]{2}:[0-9]{2}:[0-9]{2})")
        val matcher = pattern.matcher(body)
        if (matcher.find()) {
            val dateStr = matcher.group(1)
            if (!dateStr.isNullOrBlank()) {
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                    val parsedDate = sdf.parse(dateStr)
                    if (parsedDate != null) {
                        return parsedDate.time
                    }
                } catch (_: Exception) {}
            }
        }
        return null
    }

    private fun extractMerchant(body: String, sender: String): String {
        // Rule 1: "Your payment of X to <Merchant> with token / was completed / at..."
        val paymentPattern = Pattern.compile("(?i)(?:your payment of|payment of)\\s+[0-9,.]+\\s*[A-Za-z]+\\s+to\\s+([^,\n*]+?)(?=\\s+\\b(?:with|token|was|has|at|on|ET|TxId)\\b|\\.|\\*|$)")
        val paymentMatcher = paymentPattern.matcher(body)
        if (paymentMatcher.find()) {
            val candidate = cleanMerchantName(paymentMatcher.group(1))
            if (candidate.isNotBlank() && !isPhoneNumber(candidate)) {
                return candidate
            }
        }

        // Rule 2: "transferred to <Name> (25078...) at..." or "sent to <Name> (25078...) at..."
        val transferPattern = Pattern.compile("(?i)(?:transferred to|sent to)\\s+([A-Za-z0-9\\s'.-]+?)(?:\\s*\\([0-9*+ ]+\\))?\\s+(?=\\b(?:at|on|date|fee)\\b|\\.|\\*|$)")
        val transferMatcher = transferPattern.matcher(body)
        if (transferMatcher.find()) {
            val candidate = cleanMerchantName(transferMatcher.group(1))
            if (candidate.isNotBlank() && !isPhoneNumber(candidate)) {
                return candidate
            }
        }

        // Rule 3: "received X from <Name> (25078...) at..."
        val receivedPattern = Pattern.compile("(?i)(?:received from|from)\\s+([A-Za-z0-9\\s'.-]+?)(?:\\s*\\([0-9*+ ]+\\))?\\s+(?=\\b(?:at|on|date|on your)\\b|\\.|\\*|$)")
        val receivedMatcher = receivedPattern.matcher(body)
        if (receivedMatcher.find()) {
            var candidate = cleanMerchantName(receivedMatcher.group(1))
            // Remove repeated names like "Mike s Dailly ltd Mike s Dailly ltd"
            val parts = candidate.split("  ").filter { it.isNotBlank() }
            if (parts.size == 2 && parts[0].equals(parts[1], ignoreCase = true)) {
                candidate = parts[0]
            }
            if (candidate.isNotBlank() && !isPhoneNumber(candidate)) {
                return candidate
            }
        }

        // Fallback generic patterns
        val fallbackPatterns = listOf(
            Pattern.compile("(?i)(?:paid to|payment to|transferred to|sent to|to)\\s+([A-Za-z0-9&\\s'-]{2,25}?)(?=\\s+\\b(?:on|at|date|ref|txid|bal|balance|new|financial|your|is|was|for|with)\\b|\\.|\\,|\\*|$)"),
            Pattern.compile("(?i)(?:from|received from)\\s+([A-Za-z0-9&\\s'-]{2,25}?)(?=\\s+\\b(?:on|at|date|ref|txid|bal|balance|new|financial|your|is|was|for)\\b|\\.|\\,|\\*|$)")
        )

        for (p in fallbackPatterns) {
            val m = p.matcher(body)
            if (m.find()) {
                val candidate = cleanMerchantName(m.group(1))
                if (candidate.isNotBlank() && !isPhoneNumber(candidate)) {
                    return candidate
                }
            }
        }

        val cleanedSender = sender.trim()
        if (cleanedSender.isNotBlank() && !isPhoneNumber(cleanedSender)) {
            return cleanedSender
        }

        return "Mobile Money Merchant"
    }

    private fun cleanMerchantName(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        // Remove trailing or leading phone numbers in parentheses e.g. "(250786542519)"
        var cleaned = raw.replace(Regex("\\([0-9*+ ]+\\)"), "").trim()
        cleaned = cleaned.trimEnd('.', ',', '-', '*', ' ', '`')
        if (cleaned.length > 30) cleaned = cleaned.take(30).trim()
        return if (cleaned.equals("me", ignoreCase = true)) "" else cleaned
    }

    private fun isPhoneNumber(text: String): Boolean {
        val digitsOnly = text.filter { it.isDigit() }
        return digitsOnly.length >= 7 && (digitsOnly.length.toDouble() / text.length.coerceAtLeast(1)) > 0.6
    }
}

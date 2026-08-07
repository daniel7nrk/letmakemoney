package com.example.parser

import com.example.data.CategoryEntity
import java.util.regex.Pattern

data class ParsedSmsResult(
    val amount: Double,
    val currency: String,
    val merchant: String,
    val description: String,
    val category: String,
    val status: String // "PENDING" or "CONFIRMED"
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

        // Step 2: Extract Amount & Currency
        val amountResult = extractAmountAndCurrency(body, userCurrency) ?: return null

        // Step 3: Extract Merchant / Counterparty
        val merchant = extractMerchant(body, sender)

        // Step 4: Keyword-based Category Auto-suggestion
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

        // Uncategorized or pending items prompt the user for a description and category review
        if (matchedCategory == "Uncategorized") {
            status = "PENDING"
        }

        return ParsedSmsResult(
            amount = amountResult.first,
            currency = amountResult.second,
            merchant = merchant,
            description = "$merchant (SMS)",
            category = matchedCategory,
            status = status
        )
    }

    private fun extractAmountAndCurrency(body: String, defaultCurrency: String): Pair<Double, String>? {
        // Pattern 1: Amount followed by currency (e.g. "4,250 RWF", "4250.00 KES")
        val pattern1 = Pattern.compile("(?i)(?<![A-Za-z0-9])([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{1,2})?|[0-9]+(?:\\.[0-9]{1,2})?)\\s*(RWF|FRW|RF|KES|KSH|UGX|TZS|USD|BIF|SSP)(?![A-Za-z0-9])")
        val matcher1 = pattern1.matcher(body)
        if (matcher1.find()) {
            val numberStr = matcher1.group(1)?.replace(",", "")
            val currGroup = matcher1.group(2)?.uppercase() ?: defaultCurrency
            val parsedDouble = numberStr?.toDoubleOrNull()
            if (parsedDouble != null && parsedDouble > 0) {
                val stdCurrency = when (currGroup) {
                    "FRW", "RF" -> "RWF"
                    "KSH" -> "KES"
                    else -> currGroup
                }
                return Pair(parsedDouble, stdCurrency)
            }
        }

        // Pattern 2: Currency followed by amount (e.g. "RWF 4,250", "KSH 4250")
        val pattern2 = Pattern.compile("(?i)(RWF|FRW|RF|KES|KSH|UGX|TZS|USD|BIF|SSP)\\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{1,2})?|[0-9]+(?:\\.[0-9]{1,2})?)")
        val matcher2 = pattern2.matcher(body)
        if (matcher2.find()) {
            val currGroup = matcher2.group(1)?.uppercase() ?: defaultCurrency
            val numberStr = matcher2.group(2)?.replace(",", "")
            val parsedDouble = numberStr?.toDoubleOrNull()
            if (parsedDouble != null && parsedDouble > 0) {
                val stdCurrency = when (currGroup) {
                    "FRW", "RF" -> "RWF"
                    "KSH" -> "KES"
                    else -> currGroup
                }
                return Pair(parsedDouble, stdCurrency)
            }
        }

        // Pattern 3: Transaction verb followed by number (e.g. "paid 4,250", "received 15000")
        val pattern3 = Pattern.compile("(?i)(?:paid|received|transferred|spent|sent|debited|credited|sum of|amount of)\\s+([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{1,2})?|[0-9]+(?:\\.[0-9]{1,2})?)")
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

    private fun extractMerchant(body: String, sender: String): String {
        val patterns = listOf(
            Pattern.compile("(?i)(?:paid to|payment to|transferred to|sent to|to)\\s+([A-Za-z0-9&\\s'-]{2,25}?)(?=\\s+(?:on|at|date|ref|txid|bal|balance|new|financial|your|is|was|for|\\.|\\,|$))"),
            Pattern.compile("(?i)(?:from|received from)\\s+([A-Za-z0-9&\\s'-]{2,25}?)(?=\\s+(?:on|at|date|ref|txid|bal|balance|new|financial|your|is|was|for|\\.|\\,|$))")
        )

        for (p in patterns) {
            val m = p.matcher(body)
            if (m.find()) {
                val candidate = m.group(1)?.trim()
                if (!candidate.isNullOrBlank()) {
                    val cleaned = candidate.take(25).trimEnd('.', ',', ' ')
                    if (cleaned.length >= 2 && !cleaned.equals("me", ignoreCase = true)) {
                        return cleaned
                    }
                }
            }
        }

        return sender.ifBlank { "Mobile Money Merchant" }
    }
}

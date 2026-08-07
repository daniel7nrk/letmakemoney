package com.example.ui.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatUtil {

    fun formatCurrency(amount: Double, currencyCode: String): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
            maximumFractionDigits = 2
        }
        val formattedNum = formatter.format(Math.abs(amount))
        val prefix = if (amount < 0) "- " else ""
        return "$prefix$currencyCode $formattedNum"
    }

    fun formatDate(epochMillis: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(epochMillis))
    }

    fun formatTime(epochMillis: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(epochMillis))
    }

    fun formatDateTime(epochMillis: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
        return sdf.format(Date(epochMillis))
    }
}

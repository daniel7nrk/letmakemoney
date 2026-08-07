package com.example.model

enum class Country(
    val countryName: String,
    val currencyCode: String,
    val currencyName: String,
    val flagUrl: String
) {
    RWANDA("Rwanda", "RWF", "Rwandan Franc", "https://flagcdn.com/w320/rw.png"),
    KENYA("Kenya", "KES", "Kenyan Shilling", "https://flagcdn.com/w320/ke.png"),
    UGANDA("Uganda", "UGX", "Ugandan Shilling", "https://flagcdn.com/w320/ug.png"),
    TANZANIA("Tanzania", "TZS", "Tanzanian Shilling", "https://flagcdn.com/w320/tz.png"),
    BURUNDI("Burundi", "BIF", "Burundian Franc", "https://flagcdn.com/w320/bi.png"),
    SOUTH_SUDAN("South Sudan", "SSP", "South Sudanese Pound", "https://flagcdn.com/w320/ss.png");

    companion object {
        fun fromCurrencyCode(code: String): Country {
            return entries.find { it.currencyCode.equals(code, ignoreCase = true) } ?: RWANDA
        }
    }
}

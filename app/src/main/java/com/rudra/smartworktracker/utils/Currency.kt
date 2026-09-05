package com.rudra.smartworktracker.utils

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyManager {
    private var currentCurrency: String = "BDT"
    private var currentLocale: Locale = Locale.US

    fun init(currencyCode: String) {
        currentCurrency = currencyCode
        currentLocale = Locale.US
    }

    fun setCurrency(currencyCode: String) {
        currentCurrency = currencyCode
    }

    fun getCurrencyCode(): String = currentCurrency

    fun format(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(currentLocale)
        return try {
            formatter.currency = Currency.getInstance(currentCurrency)
            formatter.format(amount)
        } catch (e: Exception) {
            String.format("%s %.2f", currentCurrency, amount)
        }
    }

    fun formatCompact(amount: Double): String {
        return when {
            amount >= 1_000_000 -> String.format("%s %.1fM", currentCurrency, amount / 1_000_000)
            amount >= 1_000 -> String.format("%s %.1fK", currentCurrency, amount / 1_000)
            else -> String.format("%s %.2f", currentCurrency, amount)
        }
    }

    fun symbol(): String {
        return try {
            Currency.getInstance(currentCurrency).symbol
        } catch (e: Exception) {
            currentCurrency
        }
    }
}

val SUPPORTED_CURRENCIES = listOf(
    CurrencyOption("BDT", "Bangladeshi Taka", "৳"),
    CurrencyOption("USD", "US Dollar", "$"),
    CurrencyOption("EUR", "Euro", "€"),
    CurrencyOption("GBP", "British Pound", "£"),
    CurrencyOption("INR", "Indian Rupee", "₹"),
    CurrencyOption("JPY", "Japanese Yen", "¥"),
    CurrencyOption("CAD", "Canadian Dollar", "CA$"),
    CurrencyOption("AUD", "Australian Dollar", "A$"),
    CurrencyOption("SGD", "Singapore Dollar", "S$"),
    CurrencyOption("AED", "UAE Dirham", "د.إ"),
    CurrencyOption("SAR", "Saudi Riyal", "﷼"),
    CurrencyOption("MYR", "Malaysian Ringgit", "RM"),
    CurrencyOption("THB", "Thai Baht", "฿"),
    CurrencyOption("IDR", "Indonesian Rupiah", "Rp"),
    CurrencyOption("PHP", "Philippine Peso", "₱"),
    CurrencyOption("PKR", "Pakistani Rupee", "₨"),
    CurrencyOption("LKR", "Sri Lankan Rupee", "Rs"),
    CurrencyOption("NPR", "Nepalese Rupee", "Rs"),
    CurrencyOption("CNY", "Chinese Yuan", "¥"),
    CurrencyOption("KRW", "South Korean Won", "₩"),
    CurrencyOption("TRY", "Turkish Lira", "₺"),
    CurrencyOption("RUB", "Russian Ruble", "₽"),
    CurrencyOption("BRL", "Brazilian Real", "R$"),
    CurrencyOption("MXN", "Mexican Peso", "MX$"),
    CurrencyOption("ZAR", "South African Rand", "R"),
    CurrencyOption("EGP", "Egyptian Pound", "E£"),
    CurrencyOption("NGN", "Nigerian Naira", "₦"),
    CurrencyOption("KES", "Kenyan Shilling", "KSh"),
    CurrencyOption("GHS", "Ghanaian Cedi", "GH₵"),
    CurrencyOption("CHF", "Swiss Franc", "CHF")
)

data class CurrencyOption(
    val code: String,
    val name: String,
    val symbol: String
) {
    val displayName: String get() = "$symbol $code — $name"
}

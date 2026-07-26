package com.example.util

import java.text.DecimalFormat

enum class Currency(
    val code: String,
    val displayNameArabic: String,
    val symbolArabic: String,
    val flagEmoji: String
) {
    YER("YER", "الريال اليمني", "ر.ي", "🇾🇪"),
    SAR("SAR", "الريال السعودي", "ر.س", "🇸🇦"),
    USD("USD", "الدولار الأمريكي", "$", "🇺🇸")
}

data class CurrencyRates(
    val yerPerSar: Double = 140.0, // 1 SAR = ~140 YER
    val yerPerUsd: Double = 530.0  // 1 USD = ~530 YER
)

object CurrencyConverter {

    /**
     * Converts an amount from YER to target currency using current rates
     */
    fun convertFromYer(
        amountInYer: Double,
        targetCurrency: Currency,
        rates: CurrencyRates = CurrencyRates()
    ): Double {
        return when (targetCurrency) {
            Currency.YER -> amountInYer
            Currency.SAR -> if (rates.yerPerSar > 0) amountInYer / rates.yerPerSar else 0.0
            Currency.USD -> if (rates.yerPerUsd > 0) amountInYer / rates.yerPerUsd else 0.0
        }
    }

    /**
     * Converts an amount from source currency to YER
     */
    fun convertToYer(
        amountInSource: Double,
        sourceCurrency: Currency,
        rates: CurrencyRates = CurrencyRates()
    ): Double {
        return when (sourceCurrency) {
            Currency.YER -> amountInSource
            Currency.SAR -> amountInSource * rates.yerPerSar
            Currency.USD -> amountInSource * rates.yerPerUsd
        }
    }

    /**
     * Converts an amount directly from one currency to another
     */
    fun convert(
        amount: Double,
        fromCurrency: Currency,
        toCurrency: Currency,
        rates: CurrencyRates = CurrencyRates()
    ): Double {
        if (fromCurrency == toCurrency) return amount
        val yerAmount = convertToYer(amount, fromCurrency, rates)
        return convertFromYer(yerAmount, toCurrency, rates)
    }
}

object CurrencyFormatter {
    private val integerFormat = DecimalFormat("#,###")
    private val decimalFormat = DecimalFormat("#,##0.00")

    fun formatPrice(
        priceInYer: Double,
        targetCurrency: Currency,
        rates: CurrencyRates = CurrencyRates()
    ): String {
        val converted = CurrencyConverter.convertFromYer(priceInYer, targetCurrency, rates)
        return formatAmount(converted, targetCurrency)
    }

    fun formatAmount(amount: Double, currency: Currency): String {
        return when (currency) {
            Currency.YER -> "${integerFormat.format(amount.toLong())} ${currency.symbolArabic}"
            Currency.SAR, Currency.USD -> "${decimalFormat.format(amount)} ${currency.symbolArabic}"
        }
    }

    fun formatDualPrice(
        priceInYer: Double,
        activeCurrency: Currency,
        rates: CurrencyRates = CurrencyRates()
    ): String {
        val primary = formatPrice(priceInYer, activeCurrency, rates)
        return if (activeCurrency != Currency.YER) {
            val secondaryYer = formatPrice(priceInYer, Currency.YER, rates)
            "$primary ($secondaryYer)"
        } else {
            primary
        }
    }
}


package com.walter.mbogo.utility

import java.text.NumberFormat
import java.util.Currency

fun formatCurrency(amount: Long): String {
    val format: NumberFormat = NumberFormat.getCurrencyInstance()
    format.setMaximumFractionDigits(0)
    format.setCurrency(Currency.getInstance("KES"))
    return format.format(amount)
}
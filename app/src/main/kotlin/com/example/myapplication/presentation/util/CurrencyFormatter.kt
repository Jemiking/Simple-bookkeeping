package com.example.myapplication.presentation.util

import java.text.NumberFormat
import java.util.Locale

fun Double.formatCurrency(): String {
    val format = NumberFormat.getCurrencyInstance(Locale.CHINA)
    return format.format(this)
}

fun Float.formatCurrency(): String {
    return this.toDouble().formatCurrency()
} 
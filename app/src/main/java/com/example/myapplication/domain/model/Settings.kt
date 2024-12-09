package com.example.myapplication.domain.model

data class Settings(
    val theme: Theme = Theme.SYSTEM,
    val language: Language = Language.SYSTEM,
    val currency: Currency = Currency.CNY,
    val startDayOfMonth: Int = 1,
    val showDecimal: Boolean = true,
    val enableBiometric: Boolean = false,
    val enableBackup: Boolean = false,
    val backupFrequency: BackupFrequency = BackupFrequency.WEEKLY,
    val backupPath: String = "",
    val enableNotification: Boolean = true,
    val notificationTime: String = "20:00",
    val enableBudgetAlert: Boolean = true,
    val budgetAlertThreshold: Int = 80
)

enum class Theme {
    LIGHT, DARK, SYSTEM
}

enum class Language {
    SYSTEM,
    CHINESE_SIMPLIFIED,
    CHINESE_TRADITIONAL,
    ENGLISH
}

enum class Currency {
    CNY, USD, EUR, GBP, JPY, HKD
}

enum class BackupFrequency {
    DAILY, WEEKLY, MONTHLY, NEVER
} 
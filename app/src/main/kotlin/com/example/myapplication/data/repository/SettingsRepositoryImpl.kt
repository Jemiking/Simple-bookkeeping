package com.example.myapplication.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.domain.model.*
import com.example.myapplication.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase
) : SettingsRepository {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _settings = MutableStateFlow(loadSettings())
    override fun getSettings(): Flow<Settings> = _settings.asStateFlow()

    override suspend fun updateTheme(theme: Theme) {
        prefs.edit {
            putString(KEY_THEME, theme.name)
        }
        _settings.update { it.copy(theme = theme) }
    }

    override suspend fun updateLanguage(language: Language) {
        prefs.edit {
            putString(KEY_LANGUAGE, language.name)
        }
        _settings.update { it.copy(language = language) }
    }

    override suspend fun updateCurrency(currency: Currency) {
        prefs.edit {
            putString(KEY_CURRENCY, currency.name)
        }
        _settings.update { it.copy(currency = currency) }
    }

    override suspend fun updateStartDayOfMonth(day: Int) {
        prefs.edit {
            putInt(KEY_START_DAY, day)
        }
        _settings.update { it.copy(startDayOfMonth = day) }
    }

    override suspend fun updateShowDecimal(show: Boolean) {
        prefs.edit {
            putBoolean(KEY_SHOW_DECIMAL, show)
        }
        _settings.update { it.copy(showDecimal = show) }
    }

    override suspend fun updateBiometric(enable: Boolean) {
        prefs.edit {
            putBoolean(KEY_BIOMETRIC, enable)
        }
        _settings.update { it.copy(enableBiometric = enable) }
    }

    override suspend fun updateBackup(
        enable: Boolean,
        frequency: BackupFrequency?,
        path: String?
    ) {
        prefs.edit {
            putBoolean(KEY_BACKUP_ENABLE, enable)
            frequency?.let { putString(KEY_BACKUP_FREQUENCY, it.name) }
            path?.let { putString(KEY_BACKUP_PATH, it) }
        }
        _settings.update {
            it.copy(
                enableBackup = enable,
                backupFrequency = frequency ?: it.backupFrequency,
                backupPath = path ?: it.backupPath
            )
        }
    }

    override suspend fun updateNotification(enable: Boolean, time: String?) {
        prefs.edit {
            putBoolean(KEY_NOTIFICATION_ENABLE, enable)
            time?.let { putString(KEY_NOTIFICATION_TIME, it) }
        }
        _settings.update {
            it.copy(
                enableNotification = enable,
                notificationTime = time ?: it.notificationTime
            )
        }
    }

    override suspend fun updateBudgetAlert(enable: Boolean, threshold: Int?) {
        prefs.edit {
            putBoolean(KEY_BUDGET_ALERT_ENABLE, enable)
            threshold?.let { putInt(KEY_BUDGET_ALERT_THRESHOLD, it) }
        }
        _settings.update {
            it.copy(
                enableBudgetAlert = enable,
                budgetAlertThreshold = threshold ?: it.budgetAlertThreshold
            )
        }
    }

    override suspend fun exportData(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val exportFile = File(path)
            database.backup(exportFile)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun importData(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val importFile = File(path)
            database.restore(importFile)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearData(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            database.clearAllTables()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun backup(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val settings = _settings.value
            if (!settings.enableBackup) {
                return@withContext Result.failure(IllegalStateException("备份未启用"))
            }

            val backupFile = File(settings.backupPath)
            database.backup(backupFile)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun restore(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val backupFile = File(path)
            database.restore(backupFile)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun loadSettings(): Settings {
        return Settings(
            theme = prefs.getString(KEY_THEME, null)?.let {
                try {
                    Theme.valueOf(it)
                } catch (e: Exception) {
                    Theme.SYSTEM
                }
            } ?: Theme.SYSTEM,
            language = prefs.getString(KEY_LANGUAGE, null)?.let {
                try {
                    Language.valueOf(it)
                } catch (e: Exception) {
                    Language.SYSTEM
                }
            } ?: Language.SYSTEM,
            currency = prefs.getString(KEY_CURRENCY, null)?.let {
                try {
                    Currency.valueOf(it)
                } catch (e: Exception) {
                    Currency.CNY
                }
            } ?: Currency.CNY,
            startDayOfMonth = prefs.getInt(KEY_START_DAY, 1),
            showDecimal = prefs.getBoolean(KEY_SHOW_DECIMAL, true),
            enableBiometric = prefs.getBoolean(KEY_BIOMETRIC, false),
            enableBackup = prefs.getBoolean(KEY_BACKUP_ENABLE, false),
            backupFrequency = prefs.getString(KEY_BACKUP_FREQUENCY, null)?.let {
                try {
                    BackupFrequency.valueOf(it)
                } catch (e: Exception) {
                    BackupFrequency.WEEKLY
                }
            } ?: BackupFrequency.WEEKLY,
            backupPath = prefs.getString(KEY_BACKUP_PATH, "") ?: "",
            enableNotification = prefs.getBoolean(KEY_NOTIFICATION_ENABLE, true),
            notificationTime = prefs.getString(KEY_NOTIFICATION_TIME, "20:00") ?: "20:00",
            enableBudgetAlert = prefs.getBoolean(KEY_BUDGET_ALERT_ENABLE, true),
            budgetAlertThreshold = prefs.getInt(KEY_BUDGET_ALERT_THRESHOLD, 80)
        )
    }

    companion object {
        private const val PREFS_NAME = "settings"
        private const val KEY_THEME = "theme"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_CURRENCY = "currency"
        private const val KEY_START_DAY = "start_day"
        private const val KEY_SHOW_DECIMAL = "show_decimal"
        private const val KEY_BIOMETRIC = "biometric"
        private const val KEY_BACKUP_ENABLE = "backup_enable"
        private const val KEY_BACKUP_FREQUENCY = "backup_frequency"
        private const val KEY_BACKUP_PATH = "backup_path"
        private const val KEY_NOTIFICATION_ENABLE = "notification_enable"
        private const val KEY_NOTIFICATION_TIME = "notification_time"
        private const val KEY_BUDGET_ALERT_ENABLE = "budget_alert_enable"
        private const val KEY_BUDGET_ALERT_THRESHOLD = "budget_alert_threshold"
    }
} 
package com.example.myapplication.domain.usecase.settings

import com.example.myapplication.domain.model.*
import com.example.myapplication.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend fun updateTheme(theme: Theme) {
        settingsRepository.updateTheme(theme)
    }

    suspend fun updateLanguage(language: Language) {
        settingsRepository.updateLanguage(language)
    }

    suspend fun updateCurrency(currency: Currency) {
        settingsRepository.updateCurrency(currency)
    }

    suspend fun updateStartDayOfMonth(day: Int) {
        if (day !in 1..31) {
            throw IllegalArgumentException("日期必须在1到31之间")
        }
        settingsRepository.updateStartDayOfMonth(day)
    }

    suspend fun updateShowDecimal(show: Boolean) {
        settingsRepository.updateShowDecimal(show)
    }

    suspend fun updateBiometric(enable: Boolean) {
        settingsRepository.updateBiometric(enable)
    }

    suspend fun updateBackup(
        enable: Boolean,
        frequency: BackupFrequency? = null,
        path: String? = null
    ) {
        if (enable && path.isNullOrBlank()) {
            throw IllegalArgumentException("备份路径不能为空")
        }
        settingsRepository.updateBackup(enable, frequency, path)
    }

    suspend fun updateNotification(
        enable: Boolean,
        time: String? = null
    ) {
        if (enable && !time.isNullOrBlank()) {
            // 验证时间格式 HH:mm
            if (!time.matches(Regex("^([01]?[0-9]|2[0-3]):[0-5][0-9]$"))) {
                throw IllegalArgumentException("时间格式不正确，应为HH:mm")
            }
        }
        settingsRepository.updateNotification(enable, time)
    }

    suspend fun updateBudgetAlert(
        enable: Boolean,
        threshold: Int? = null
    ) {
        if (enable && threshold != null) {
            if (threshold !in 1..100) {
                throw IllegalArgumentException("预算提醒阈值必须在1到100之间")
            }
        }
        settingsRepository.updateBudgetAlert(enable, threshold)
    }
} 
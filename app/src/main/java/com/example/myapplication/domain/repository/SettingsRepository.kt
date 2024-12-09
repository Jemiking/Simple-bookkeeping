package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.*
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<Settings>
    
    suspend fun updateTheme(theme: Theme)
    
    suspend fun updateLanguage(language: Language)
    
    suspend fun updateCurrency(currency: Currency)
    
    suspend fun updateStartDayOfMonth(day: Int)
    
    suspend fun updateShowDecimal(show: Boolean)
    
    suspend fun updateBiometric(enable: Boolean)
    
    suspend fun updateBackup(
        enable: Boolean,
        frequency: BackupFrequency? = null,
        path: String? = null
    )
    
    suspend fun updateNotification(
        enable: Boolean,
        time: String? = null
    )
    
    suspend fun updateBudgetAlert(
        enable: Boolean,
        threshold: Int? = null
    )
    
    suspend fun exportData(path: String): Result<Unit>
    
    suspend fun importData(path: String): Result<Unit>
    
    suspend fun clearData(): Result<Unit>
    
    suspend fun backup(): Result<Unit>
    
    suspend fun restore(path: String): Result<Unit>
} 
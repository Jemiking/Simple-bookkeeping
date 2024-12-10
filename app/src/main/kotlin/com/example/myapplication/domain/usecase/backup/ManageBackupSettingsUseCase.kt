package com.example.myapplication.domain.usecase.backup

import com.example.myapplication.domain.model.BackupSettings
import com.example.myapplication.domain.repository.BackupRepository
import javax.inject.Inject

class ManageBackupSettingsUseCase @Inject constructor(
    private val repository: BackupRepository
) {
    suspend fun getSettings(): Result<BackupSettings> {
        return try {
            val settings = repository.getBackupSettings()
            Result.success(settings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSettings(settings: BackupSettings): Result<Unit> {
        return try {
            repository.updateBackupSettings(settings)
            if (settings.autoBackupEnabled) {
                repository.scheduleNextAutoBackup()
            } else {
                repository.cancelScheduledAutoBackup()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun validateBackupLocation(): Result<Boolean> {
        return repository.validateBackupLocation()
    }

    suspend fun getAvailableStorage(): Result<Long> {
        return try {
            val available = repository.getAvailableStorage()
            Result.success(available)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBackupsSize(): Result<Long> {
        return try {
            val size = repository.getBackupsSize()
            Result.success(size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 
package com.example.myapplication.domain.usecase.backup

import com.example.myapplication.domain.model.BackupResult
import com.example.myapplication.domain.repository.BackupRepository
import javax.inject.Inject

class ManageAutoBackupUseCase @Inject constructor(
    private val repository: BackupRepository
) {
    suspend fun performAutoBackup(): Result<BackupResult> {
        return repository.performAutoBackup()
    }

    suspend fun scheduleNextAutoBackup(): Result<Unit> {
        return try {
            repository.scheduleNextAutoBackup()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelScheduledAutoBackup(): Result<Unit> {
        return try {
            repository.cancelScheduledAutoBackup()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cleanupOldBackups(keepCount: Int): Result<Unit> {
        return repository.deleteOldBackups(keepCount)
    }
} 
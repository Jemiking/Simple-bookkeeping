package com.example.myapplication.domain.usecase.backup

import android.net.Uri
import com.example.myapplication.domain.repository.BackupRepository
import javax.inject.Inject

class ValidateBackupUseCase @Inject constructor(
    private val repository: BackupRepository
) {
    suspend fun validateBackup(uri: Uri): Result<Boolean> {
        return repository.validateBackup(uri)
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
package com.example.myapplication.domain.usecase.backup

import android.net.Uri
import com.example.myapplication.domain.model.BackupResult
import com.example.myapplication.domain.repository.BackupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RestoreDataUseCase @Inject constructor(
    private val repository: BackupRepository
) {
    suspend fun restore(uri: Uri): Result<BackupResult> {
        return repository.restoreBackup(uri)
    }

    fun getRestoreProgress(): Flow<Result<BackupProgress>> {
        return repository.getRestoreProgress()
            .map { Result.success(it) }
            .catch { e -> emit(Result.failure(e)) }
    }

    suspend fun validateBackup(uri: Uri): Result<Boolean> {
        return repository.validateBackup(uri)
    }
} 
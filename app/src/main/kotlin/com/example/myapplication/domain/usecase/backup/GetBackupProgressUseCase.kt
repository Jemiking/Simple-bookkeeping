package com.example.myapplication.domain.usecase.backup

import com.example.myapplication.domain.model.BackupProgress
import com.example.myapplication.domain.repository.BackupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetBackupProgressUseCase @Inject constructor(
    private val repository: BackupRepository
) {
    fun getBackupProgress(): Flow<Result<BackupProgress>> {
        return repository.getBackupProgress()
            .map { Result.success(it) }
            .catch { e -> emit(Result.failure(e)) }
    }

    fun getRestoreProgress(): Flow<Result<BackupProgress>> {
        return repository.getRestoreProgress()
            .map { Result.success(it) }
            .catch { e -> emit(Result.failure(e)) }
    }
} 
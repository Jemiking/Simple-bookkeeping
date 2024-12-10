package com.example.myapplication.domain.usecase.backup

import com.example.myapplication.domain.repository.BackupRepository
import javax.inject.Inject

class CleanupBackupsUseCase @Inject constructor(
    private val repository: BackupRepository
) {
    suspend fun deleteBackup(backupId: Long): Result<Unit> {
        return repository.deleteBackup(backupId)
    }

    suspend fun deleteBackups(backupIds: List<Long>): Result<Unit> {
        return repository.deleteBackups(backupIds)
    }

    suspend fun deleteOldBackups(keepCount: Int): Result<Unit> {
        return repository.deleteOldBackups(keepCount)
    }
} 
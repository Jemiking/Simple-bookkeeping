package com.example.myapplication.domain.usecase.backup

import com.example.myapplication.domain.model.BackupResult
import com.example.myapplication.domain.repository.BackupRepository
import javax.inject.Inject

class CreateBackupUseCase @Inject constructor(
    private val repository: BackupRepository
) {
    suspend operator fun invoke(description: String? = null): Result<BackupResult> {
        return repository.createBackup(description)
    }
} 
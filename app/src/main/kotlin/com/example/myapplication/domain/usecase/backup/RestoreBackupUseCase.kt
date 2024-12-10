package com.example.myapplication.domain.usecase.backup

import android.net.Uri
import com.example.myapplication.domain.model.BackupResult
import com.example.myapplication.domain.repository.BackupRepository
import javax.inject.Inject

class RestoreBackupUseCase @Inject constructor(
    private val repository: BackupRepository
) {
    suspend operator fun invoke(uri: Uri): Result<BackupResult> {
        return repository.restoreBackup(uri)
    }
} 
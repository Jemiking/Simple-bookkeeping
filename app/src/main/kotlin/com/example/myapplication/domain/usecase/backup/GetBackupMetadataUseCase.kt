package com.example.myapplication.domain.usecase.backup

import com.example.myapplication.domain.model.BackupMetadata
import com.example.myapplication.domain.repository.BackupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject

class GetBackupMetadataUseCase @Inject constructor(
    private val repository: BackupRepository
) {
    suspend fun getById(backupId: Long): Result<BackupMetadata?> {
        return try {
            val metadata = repository.getBackupMetadata(backupId)
            Result.success(metadata)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAll(): Flow<Result<List<BackupMetadata>>> {
        return repository.getAllBackupMetadata()
            .map { Result.success(it) }
            .catch { e -> emit(Result.failure(e)) }
    }

    fun getAutoBackups(): Flow<Result<List<BackupMetadata>>> {
        return repository.getAutoBackupMetadata()
            .map { Result.success(it) }
            .catch { e -> emit(Result.failure(e)) }
    }

    fun getByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<Result<List<BackupMetadata>>> {
        return repository.getBackupMetadataByDateRange(startDate, endDate)
            .map { Result.success(it) }
            .catch { e -> emit(Result.failure(e)) }
    }
} 
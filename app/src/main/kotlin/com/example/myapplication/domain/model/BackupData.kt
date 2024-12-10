package com.example.myapplication.domain.model

import java.time.LocalDateTime

data class BackupMetadata(
    val id: Long = 0,
    val filename: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val size: Long,
    val version: String,
    val description: String? = null,
    val isAutoBackup: Boolean = false
)

data class BackupData(
    val metadata: BackupMetadata,
    val exportData: ExportData
)

sealed class BackupError : Exception() {
    data class FileCreationError(override val message: String) : BackupError()
    data class WriteError(override val message: String) : BackupError()
    data class ReadError(override val message: String) : BackupError()
    data class InvalidBackupFile(override val message: String) : BackupError()
    data class StoragePermissionDenied(override val message: String) : BackupError()
    data class InsufficientStorage(override val message: String) : BackupError()
    data class DatabaseError(override val message: String) : BackupError()
}

data class BackupProgress(
    val totalSteps: Int,
    val currentStep: Int,
    val currentOperation: String,
    val progress: Float = currentStep.toFloat() / totalSteps.toFloat()
)

data class BackupSettings(
    val autoBackupEnabled: Boolean = false,
    val autoBackupInterval: Int = 7, // 天数
    val keepBackupCount: Int = 5,
    val backupLocation: String? = null,
    val includeImages: Boolean = true,
    val compressBackup: Boolean = true,
    val encryptBackup: Boolean = false,
    val encryptionKey: String? = null
)

data class BackupResult(
    val isSuccess: Boolean,
    val metadata: BackupMetadata? = null,
    val error: BackupError? = null
) 
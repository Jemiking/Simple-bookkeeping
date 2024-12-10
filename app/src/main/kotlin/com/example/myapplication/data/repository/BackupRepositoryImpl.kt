package com.example.myapplication.data.repository

import android.content.Context
import android.net.Uri
import com.example.myapplication.data.local.dao.BackupMetadataDao
import com.example.myapplication.data.local.dao.BackupSettingsDao
import com.example.myapplication.data.local.entity.BackupMetadataEntity
import com.example.myapplication.data.local.entity.BackupSettingsEntity
import com.example.myapplication.domain.model.*
import com.example.myapplication.domain.repository.BackupRepository
import com.example.myapplication.domain.repository.DataRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.*
import java.io.*
import java.time.LocalDateTime
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject

class BackupRepositoryImpl @Inject constructor(
    private val context: Context,
    private val backupMetadataDao: BackupMetadataDao,
    private val backupSettingsDao: BackupSettingsDao,
    private val dataRepository: DataRepository
) : BackupRepository {

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    private val backupProgressFlow = MutableStateFlow(
        BackupProgress(
            totalSteps = 0,
            currentStep = 0,
            currentOperation = ""
        )
    )

    private val restoreProgressFlow = MutableStateFlow(
        BackupProgress(
            totalSteps = 0,
            currentStep = 0,
            currentOperation = ""
        )
    )

    override suspend fun createBackup(description: String?): Result<BackupResult> {
        return try {
            // 获取备份设置
            val settings = getBackupSettings()
            
            // 更新进度
            updateBackupProgress(1, 4, "准备备份数据")
            
            // 获取所有数据
            val exportDataResult = dataRepository.getAllData()
            if (exportDataResult.isFailure) {
                return Result.failure(BackupError.DatabaseError("获取数据失败: ${exportDataResult.exceptionOrNull()?.message}"))
            }
            val exportData = exportDataResult.getOrNull()!!
            
            // 更新进度
            updateBackupProgress(2, 4, "创建备份文件")
            
            // 创建备份文件
            val backupDir = File(context.filesDir, "backups")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }
            
            val filename = "backup_${LocalDateTime.now().toString().replace(":", "-")}.zip"
            val backupFile = File(backupDir, filename)
            
            // 更新进度
            updateBackupProgress(3, 4, "写入备份数据")
            
            // 写入数据
            ZipOutputStream(FileOutputStream(backupFile)).use { zipOut ->
                // 写入元数据
                val metadataJson = gson.toJson(exportData)
                zipOut.putNextEntry(ZipEntry("data.json"))
                zipOut.write(metadataJson.toByteArray())
                zipOut.closeEntry()

                // 如果需要，写入图片文件
                if (settings.includeImages) {
                    val imagesDir = File(context.filesDir, "images")
                    if (imagesDir.exists()) {
                        imagesDir.walk().forEach { file ->
                            if (file.isFile) {
                                val relativePath = file.relativeTo(imagesDir).path
                                zipOut.putNextEntry(ZipEntry("images/$relativePath"))
                                file.inputStream().use { input ->
                                    input.copyTo(zipOut)
                                }
                                zipOut.closeEntry()
                            }
                        }
                    }
                }
            }
            
            // 保存元数据
            val metadata = BackupMetadataEntity(
                filename = filename,
                createdAt = LocalDateTime.now(),
                size = backupFile.length(),
                version = "1.0",
                description = description,
                isAutoBackup = false
            )
            val metadataId = backupMetadataDao.insert(metadata)
            
            // 更新进度
            updateBackupProgress(4, 4, "完成备份")
            
            Result.success(
                BackupResult(
                    isSuccess = true,
                    metadata = metadata.toDomain(),
                    error = null
                )
            )
        } catch (e: Exception) {
            Result.failure(BackupError.DatabaseError("创建备份失败: ${e.message}"))
        }
    }

    override suspend fun restoreBackup(uri: Uri): Result<BackupResult> {
        return try {
            // 验证备份文件
            updateRestoreProgress(1, 5, "验证备份文件")
            val isValid = validateBackup(uri).getOrNull()
            if (isValid != true) {
                return Result.failure(BackupError.InvalidBackupFile("无效的备份文件"))
            }
            
            // 读取备份数据
            updateRestoreProgress(2, 5, "读取备份数据")
            val backupData = readBackupData(uri)
            
            // 验证数据完整性
            updateRestoreProgress(3, 5, "验证数据完整性")
            if (!validateBackupData(backupData)) {
                return Result.failure(BackupError.InvalidBackupFile("备份数据不完整或已损坏"))
            }
            
            // 恢复数据
            updateRestoreProgress(4, 5, "恢复数据")
            val importResult = dataRepository.importData(backupData)
            if (importResult.isFailure) {
                return Result.failure(BackupError.DatabaseError("恢复数据失败: ${importResult.exceptionOrNull()?.message}"))
            }
            
            // 恢复图片文件
            val settings = getBackupSettings()
            if (settings.includeImages) {
                restoreImages(uri)
            }
            
            // 完成恢复
            updateRestoreProgress(5, 5, "完成恢复")
            
            Result.success(
                BackupResult(
                    isSuccess = true,
                    metadata = null,
                    error = null
                )
            )
        } catch (e: Exception) {
            Result.failure(BackupError.DatabaseError("恢复备份失败: ${e.message}"))
        }
    }

    private suspend fun readBackupData(uri: Uri): ExportData {
        context.contentResolver.openInputStream(uri)?.use { input ->
            ZipInputStream(input).use { zipIn ->
                var entry = zipIn.nextEntry
                while (entry != null) {
                    if (entry.name == "data.json") {
                        val json = zipIn.bufferedReader().use { it.readText() }
                        return gson.fromJson(json, ExportData::class.java)
                    }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }
        }
        throw BackupError.InvalidBackupFile("备份文件中未找到数据")
    }

    private suspend fun validateBackupData(data: ExportData): Boolean {
        return try {
            // 验证版本兼容性
            if (data.version != "1.0") {
                return false
            }

            // 验证必要数据是否存在
            if (data.transactions.isEmpty() && data.accounts.isEmpty() && data.categories.isEmpty()) {
                return false
            }

            // 验证数据关联性
            val accountIds = data.accounts.map { it.id }.toSet()
            val categoryIds = data.categories.map { it.id }.toSet()

            // 验证交易记录的账户和分类是否存在
            data.transactions.all { transaction ->
                accountIds.contains(transaction.accountId) &&
                categoryIds.contains(transaction.categoryId)
            }
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun restoreImages(uri: Uri) {
        val imagesDir = File(context.filesDir, "images")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }

        context.contentResolver.openInputStream(uri)?.use { input ->
            ZipInputStream(input).use { zipIn ->
                var entry = zipIn.nextEntry
                while (entry != null) {
                    if (entry.name.startsWith("images/")) {
                        val imagePath = entry.name.removePrefix("images/")
                        val imageFile = File(imagesDir, imagePath)
                        imageFile.parentFile?.mkdirs()
                        FileOutputStream(imageFile).use { output ->
                            zipIn.copyTo(output)
                        }
                    }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }
        }
    }

    override suspend fun deleteBackup(backupId: Long): Result<Unit> {
        return try {
            val metadata = backupMetadataDao.getById(backupId)
            if (metadata != null) {
                val backupFile = File(context.filesDir, "backups/${metadata.filename}")
                if (backupFile.exists()) {
                    backupFile.delete()
                }
                backupMetadataDao.deleteById(backupId)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(BackupError.DatabaseError("删除备份失败: ${e.message}"))
        }
    }

    override suspend fun deleteBackups(backupIds: List<Long>): Result<Unit> {
        return try {
            backupIds.forEach { id ->
                val metadata = backupMetadataDao.getById(id)
                if (metadata != null) {
                    val backupFile = File(context.filesDir, "backups/${metadata.filename}")
                    if (backupFile.exists()) {
                        backupFile.delete()
                    }
                }
            }
            backupMetadataDao.deleteByIds(backupIds)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(BackupError.DatabaseError("删除��份失败: ${e.message}"))
        }
    }

    override suspend fun deleteOldBackups(keepCount: Int): Result<Unit> {
        return try {
            val count = backupMetadataDao.getCount()
            if (count > keepCount) {
                backupMetadataDao.deleteOldBackups(keepCount)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(BackupError.DatabaseError("删除旧备份失败: ${e.message}"))
        }
    }

    override suspend fun getBackupMetadata(backupId: Long): BackupMetadata? {
        return backupMetadataDao.getById(backupId)?.toDomain()
    }

    override fun getAllBackupMetadata(): Flow<List<BackupMetadata>> {
        return backupMetadataDao.getAll().map { list -> list.map { it.toDomain() } }
    }

    override fun getAutoBackupMetadata(): Flow<List<BackupMetadata>> {
        return backupMetadataDao.getAutoBackups().map { list -> list.map { it.toDomain() } }
    }

    override fun getBackupMetadataByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<BackupMetadata>> {
        return backupMetadataDao.getByDateRange(startDate, endDate)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getBackupSettings(): BackupSettings {
        return backupSettingsDao.getSettings()?.toDomain() ?: BackupSettings()
    }

    override suspend fun updateBackupSettings(settings: BackupSettings) {
        backupSettingsDao.insertOrUpdate(settings.toEntity())
    }

    override suspend fun performAutoBackup(): Result<BackupResult> {
        val settings = getBackupSettings()
        if (!settings.autoBackupEnabled) {
            return Result.failure(BackupError.DatabaseError("自动备份未启用"))
        }
        return createBackup("自动备份")
    }

    override suspend fun scheduleNextAutoBackup() {
        // TODO: 实现自动备份调度逻辑
    }

    override suspend fun cancelScheduledAutoBackup() {
        // TODO: 实现取消自动备份调度逻辑
    }

    override fun getBackupProgress(): Flow<BackupProgress> = backupProgressFlow.asStateFlow()

    override fun getRestoreProgress(): Flow<BackupProgress> = restoreProgressFlow.asStateFlow()

    override suspend fun validateBackup(uri: Uri): Result<Boolean> {
        return try {
            var isValid = false
            context.contentResolver.openInputStream(uri)?.use { input ->
                ZipInputStream(input).use { zipIn ->
                    var entry = zipIn.nextEntry
                    while (entry != null) {
                        if (entry.name == "data.json") {
                            val json = zipIn.bufferedReader().use { it.readText() }
                            val data = gson.fromJson(json, ExportData::class.java)
                            isValid = validateBackupData(data)
                            break
                        }
                        zipIn.closeEntry()
                        entry = zipIn.nextEntry
                    }
                }
            }
            Result.success(isValid)
        } catch (e: Exception) {
            Result.failure(BackupError.InvalidBackupFile("验证备份文件失败: ${e.message}"))
        }
    }

    override suspend fun validateBackupLocation(): Result<Boolean> {
        return try {
            val backupDir = File(context.filesDir, "backups")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }
            Result.success(backupDir.exists() && backupDir.canWrite())
        } catch (e: Exception) {
            Result.failure(BackupError.StoragePermissionDenied("验证备份位置失败: ${e.message}"))
        }
    }

    override suspend fun getAvailableStorage(): Long {
        val backupDir = File(context.filesDir, "backups")
        return backupDir.freeSpace
    }

    override suspend fun getBackupsSize(): Long {
        return backupMetadataDao.getTotalSize() ?: 0L
    }

    private fun updateBackupProgress(currentStep: Int, totalSteps: Int, operation: String) {
        backupProgressFlow.value = BackupProgress(
            totalSteps = totalSteps,
            currentStep = currentStep,
            currentOperation = operation
        )
    }

    private fun updateRestoreProgress(currentStep: Int, totalSteps: Int, operation: String) {
        restoreProgressFlow.value = BackupProgress(
            totalSteps = totalSteps,
            currentStep = currentStep,
            currentOperation = operation
        )
    }

    private fun BackupMetadataEntity.toDomain(): BackupMetadata {
        return BackupMetadata(
            id = id,
            filename = filename,
            createdAt = createdAt,
            size = size,
            version = version,
            description = description,
            isAutoBackup = isAutoBackup
        )
    }

    private fun BackupSettingsEntity.toDomain(): BackupSettings {
        return BackupSettings(
            autoBackupEnabled = autoBackupEnabled,
            autoBackupInterval = autoBackupInterval,
            keepBackupCount = keepBackupCount,
            backupLocation = backupLocation,
            includeImages = includeImages,
            compressBackup = compressBackup,
            encryptBackup = encryptBackup,
            encryptionKey = encryptionKey
        )
    }

    private fun BackupSettings.toEntity(): BackupSettingsEntity {
        return BackupSettingsEntity(
            autoBackupEnabled = autoBackupEnabled,
            autoBackupInterval = autoBackupInterval,
            keepBackupCount = keepBackupCount,
            backupLocation = backupLocation,
            includeImages = includeImages,
            compressBackup = compressBackup,
            encryptBackup = encryptBackup,
            encryptionKey = encryptionKey
        )
    }
} 
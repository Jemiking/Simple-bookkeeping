package com.example.myapplication.domain.usecase.settings

import com.example.myapplication.domain.repository.SettingsRepository
import javax.inject.Inject

class DataManagementUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend fun exportData(path: String): Result<Unit> {
        if (path.isBlank()) {
            return Result.failure(IllegalArgumentException("导出路径不能为空"))
        }
        return settingsRepository.exportData(path)
    }

    suspend fun importData(path: String): Result<Unit> {
        if (path.isBlank()) {
            return Result.failure(IllegalArgumentException("导入路径不能为空"))
        }
        if (!path.endsWith(".db")) {
            return Result.failure(IllegalArgumentException("只支持导入数据库文件"))
        }
        return settingsRepository.importData(path)
    }

    suspend fun clearData(): Result<Unit> {
        return settingsRepository.clearData()
    }

    suspend fun backup(): Result<Unit> {
        return settingsRepository.backup()
    }

    suspend fun restore(path: String): Result<Unit> {
        if (path.isBlank()) {
            return Result.failure(IllegalArgumentException("备份文件路径不能为空"))
        }
        if (!path.endsWith(".db")) {
            return Result.failure(IllegalArgumentException("只支持恢复数据库文件"))
        }
        return settingsRepository.restore(path)
    }
} 
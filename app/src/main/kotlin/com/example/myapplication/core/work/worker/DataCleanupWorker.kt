package com.example.myapplication.core.work.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myapplication.domain.repository.BackupRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class DataCleanupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val backupRepository: BackupRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // 获取最大备份数量
            val maxBackupCount = inputData.getInt(KEY_MAX_BACKUP_COUNT, DEFAULT_MAX_BACKUPS)
            
            // 删除旧备份
            backupRepository.deleteOldBackups(maxBackupCount)
                .onSuccess {
                    Result.success()
                }
                .onFailure {
                    Result.retry()
                }
                .getOrDefault(Result.failure())
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val KEY_MAX_BACKUP_COUNT = "key_max_backup_count"
        private const val DEFAULT_MAX_BACKUPS = 5
    }
} 
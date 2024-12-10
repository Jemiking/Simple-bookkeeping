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
class AutoBackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val backupRepository: BackupRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // 执行自动备份
            backupRepository.performAutoBackup()
                .onSuccess {
                    // 更新下次备份时间
                    backupRepository.scheduleNextAutoBackup()
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
} 
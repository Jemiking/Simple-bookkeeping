package com.example.myapplication.core.work.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myapplication.domain.repository.DataRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class DataSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val dataRepository: DataRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // 执行数据同步
            dataRepository.syncData()
                .onSuccess {
                    Result.success()
                }
                .onFailure {
                    if (runAttemptCount < MAX_RETRIES) {
                        Result.retry()
                    } else {
                        Result.failure()
                    }
                }
                .getOrDefault(Result.failure())
        } catch (e: Exception) {
            if (runAttemptCount < MAX_RETRIES) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        private const val MAX_RETRIES = 3
    }
} 
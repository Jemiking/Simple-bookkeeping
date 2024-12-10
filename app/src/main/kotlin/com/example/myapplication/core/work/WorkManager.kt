package com.example.myapplication.core.work

import android.content.Context
import androidx.work.*
import com.example.myapplication.core.work.worker.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = androidx.work.WorkManager.getInstance(context)

    // 自动备份任务
    fun scheduleAutoBackup(intervalHours: Int) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresCharging(true)
            .setRequiresBatteryNotLow(true)
            .build()

        val backupRequest = PeriodicWorkRequestBuilder<AutoBackupWorker>(
            intervalHours.toLong(), TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .addTag(TAG_AUTO_BACKUP)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_AUTO_BACKUP,
            ExistingPeriodicWorkPolicy.UPDATE,
            backupRequest
        )
    }

    fun cancelAutoBackup() {
        workManager.cancelUniqueWork(WORK_AUTO_BACKUP)
    }

    // 数据同步任务
    fun scheduleSyncData(intervalMinutes: Int) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<DataSyncWorker>(
            intervalMinutes.toLong(), TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag(TAG_DATA_SYNC)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_DATA_SYNC,
            ExistingPeriodicWorkPolicy.UPDATE,
            syncRequest
        )
    }

    fun cancelDataSync() {
        workManager.cancelUniqueWork(WORK_DATA_SYNC)
    }

    // 预算提醒任务
    fun scheduleBudgetAlert(thresholdPercent: Int) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val data = workDataOf(
            KEY_BUDGET_THRESHOLD to thresholdPercent
        )

        val budgetRequest = PeriodicWorkRequestBuilder<BudgetAlertWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .setInputData(data)
            .addTag(TAG_BUDGET_ALERT)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_BUDGET_ALERT,
            ExistingPeriodicWorkPolicy.UPDATE,
            budgetRequest
        )
    }

    fun cancelBudgetAlert() {
        workManager.cancelUniqueWork(WORK_BUDGET_ALERT)
    }

    // 数据清理任务
    fun scheduleDataCleanup(maxBackupCount: Int) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresCharging(true)
            .build()

        val data = workDataOf(
            KEY_MAX_BACKUP_COUNT to maxBackupCount
        )

        val cleanupRequest = PeriodicWorkRequestBuilder<DataCleanupWorker>(
            7, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .setInputData(data)
            .addTag(TAG_DATA_CLEANUP)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_DATA_CLEANUP,
            ExistingPeriodicWorkPolicy.UPDATE,
            cleanupRequest
        )
    }

    fun cancelDataCleanup() {
        workManager.cancelUniqueWork(WORK_DATA_CLEANUP)
    }

    // 一次性任务
    fun scheduleOneTimeBackup() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresCharging(true)
            .setRequiresBatteryNotLow(true)
            .build()

        val backupRequest = OneTimeWorkRequestBuilder<AutoBackupWorker>()
            .setConstraints(constraints)
            .addTag(TAG_ONE_TIME_BACKUP)
            .build()

        workManager.enqueue(backupRequest)
    }

    fun scheduleOneTimeSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<DataSyncWorker>()
            .setConstraints(constraints)
            .addTag(TAG_ONE_TIME_SYNC)
            .build()

        workManager.enqueue(syncRequest)
    }

    // 获取任务状态
    fun getWorkInfo(tag: String) = workManager.getWorkInfosByTagLiveData(tag)

    companion object {
        // Work Names
        private const val WORK_AUTO_BACKUP = "auto_backup_work"
        private const val WORK_DATA_SYNC = "data_sync_work"
        private const val WORK_BUDGET_ALERT = "budget_alert_work"
        private const val WORK_DATA_CLEANUP = "data_cleanup_work"

        // Tags
        private const val TAG_AUTO_BACKUP = "tag_auto_backup"
        private const val TAG_DATA_SYNC = "tag_data_sync"
        private const val TAG_BUDGET_ALERT = "tag_budget_alert"
        private const val TAG_DATA_CLEANUP = "tag_data_cleanup"
        private const val TAG_ONE_TIME_BACKUP = "tag_one_time_backup"
        private const val TAG_ONE_TIME_SYNC = "tag_one_time_sync"

        // Keys
        private const val KEY_BUDGET_THRESHOLD = "key_budget_threshold"
        private const val KEY_MAX_BACKUP_COUNT = "key_max_backup_count"
    }
} 
package com.example.myapplication.core.work.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myapplication.R
import com.example.myapplication.domain.repository.BudgetRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

@HiltWorker
class BudgetAlertWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val budgetRepository: BudgetRepository
) : CoroutineWorker(appContext, workerParams) {

    private val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // 获取预算阈值
            val thresholdPercent = inputData.getInt(KEY_BUDGET_THRESHOLD, DEFAULT_THRESHOLD)
            
            // 检查预算使用情况
            val now = LocalDateTime.now()
            budgetRepository.checkBudgetStatus(now)
                .onSuccess { budgets ->
                    // 筛选超出阈值的预算
                    val exceededBudgets = budgets.filter { budget ->
                        budget.usedPercentage >= thresholdPercent
                    }

                    if (exceededBudgets.isNotEmpty()) {
                        // 创建通知渠道
                        createNotificationChannel()

                        // 发送通知
                        exceededBudgets.forEach { budget ->
                            val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_notification)
                                .setContentTitle("预算提醒")
                                .setContentText("${budget.name} 已使用 ${budget.usedPercentage}%")
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setAutoCancel(true)
                                .build()

                            notificationManager.notify(budget.id.toInt(), notification)
                        }
                    }

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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "预算提醒"
            val descriptionText = "显示预算使用情况的提醒"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "budget_alert_channel"
        private const val KEY_BUDGET_THRESHOLD = "key_budget_threshold"
        private const val DEFAULT_THRESHOLD = 80
    }
} 
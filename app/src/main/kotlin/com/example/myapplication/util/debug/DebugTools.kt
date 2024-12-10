package com.example.myapplication.util.debug

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object DebugTools {
    private const val TAG = "DebugTools"
    private const val LOG_FILE_PREFIX = "app_log_"
    private const val LOG_FILE_EXTENSION = ".txt"
    private const val MAX_LOG_FILES = 5
    private const val MAX_LOG_SIZE = 5 * 1024 * 1024 // 5MB

    // 日志记录
    fun logInfo(message: String) {
        Log.i(TAG, message)
        writeToLogFile("INFO", message)
    }

    fun logError(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
        writeToLogFile("ERROR", "$message\n${throwable?.stackTraceToString() ?: ""}")
    }

    fun logDebug(message: String) {
        Log.d(TAG, message)
        writeToLogFile("DEBUG", message)
    }

    fun logWarning(message: String) {
        Log.w(TAG, message)
        writeToLogFile("WARNING", message)
    }

    // 性能监控
    @Composable
    fun <T> MonitorFlow(
        flow: Flow<T>,
        tag: String = "Flow",
        onCollect: (T) -> Unit
    ) {
        LaunchedEffect(flow) {
            flow
                .onEach { value ->
                    logDebug("[$tag] Collected value: $value")
                }
                .catch { throwable ->
                    logError("[$tag] Error in flow", throwable)
                }
                .collect { value ->
                    onCollect(value)
                }
        }
    }

    @Composable
    fun MonitorRecomposition(tag: String = "Recomposition") {
        if (!LocalInspectionMode.current) {
            val count = remember { mutableStateOf(0) }
            LaunchedEffect(Unit) {
                count.value++
                logDebug("[$tag] Recomposed ${count.value} times")
            }
        }
    }

    // 设备信息
    fun getDeviceInfo(context: Context): String {
        return buildString {
            appendLine("Device Information:")
            appendLine("- Manufacturer: ${Build.MANUFACTURER}")
            appendLine("- Model: ${Build.MODEL}")
            appendLine("- Android Version: ${Build.VERSION.RELEASE}")
            appendLine("- API Level: ${Build.VERSION.SDK_INT}")
            appendLine("- App Version: ${context.packageManager.getPackageInfo(context.packageName, 0).versionName}")
            appendLine("- Screen Resolution: ${context.resources.displayMetrics.run { "${widthPixels}x${heightPixels}" }}")
            appendLine("- Screen Density: ${context.resources.displayMetrics.density}")
        }
    }

    // 内存信息
    fun getMemoryInfo(): String {
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory() / 1024 / 1024
        val totalMemory = runtime.totalMemory() / 1024 / 1024
        val freeMemory = runtime.freeMemory() / 1024 / 1024
        val usedMemory = totalMemory - freeMemory

        return buildString {
            appendLine("Memory Information:")
            appendLine("- Max Memory: $maxMemory MB")
            appendLine("- Total Memory: $totalMemory MB")
            appendLine("- Used Memory: $usedMemory MB")
            appendLine("- Free Memory: $freeMemory MB")
        }
    }

    // 日志文件管理
    private fun writeToLogFile(level: String, message: String) {
        try {
            val context = getApplicationContext()
            val logDir = File(context.filesDir, "logs")
            if (!logDir.exists()) {
                logDir.mkdirs()
            }

            // 检查并清理旧日志文件
            cleanOldLogFiles(logDir)

            // 创建新日志文件
            val timestamp = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val logFile = File(logDir, "${LOG_FILE_PREFIX}${timestamp}${LOG_FILE_EXTENSION}")
            val timeString = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())

            // 写入日志
            logFile.appendText("$timeString [$level] $message\n")

            // 检查文件大小
            if (logFile.length() > MAX_LOG_SIZE) {
                val backupFile = File(logDir, "${logFile.name}.1")
                logFile.renameTo(backupFile)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error writing to log file", e)
        }
    }

    private fun cleanOldLogFiles(logDir: File) {
        val logFiles = logDir.listFiles { file ->
            file.name.startsWith(LOG_FILE_PREFIX) && file.name.endsWith(LOG_FILE_EXTENSION)
        }

        if (logFiles != null && logFiles.size > MAX_LOG_FILES) {
            logFiles.sortBy { it.lastModified() }
            for (i in 0 until logFiles.size - MAX_LOG_FILES) {
                logFiles[i].delete()
            }
        }
    }

    private lateinit var applicationContext: Context

    fun init(context: Context) {
        applicationContext = context.applicationContext
    }

    private fun getApplicationContext(): Context {
        if (!::applicationContext.isInitialized) {
            throw IllegalStateException("DebugTools not initialized. Call init() first.")
        }
        return applicationContext
    }
} 
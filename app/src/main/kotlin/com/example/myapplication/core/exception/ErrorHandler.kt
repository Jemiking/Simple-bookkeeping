package com.example.myapplication.core.exception

import android.content.Context
import android.util.Log
import com.example.myapplication.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ErrorHandler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _errorFlow = MutableStateFlow<AppException?>(null)
    val errorFlow: StateFlow<AppException?> = _errorFlow

    private val _errorCount = MutableStateFlow(0)
    val errorCount: StateFlow<Int> = _errorCount

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val logFile: File by lazy {
        File(context.filesDir, "error.log")
    }

    fun handleError(throwable: Throwable, tag: String? = null) {
        val appException = AppException.from(throwable)
        
        // 记录错误日志
        logError(appException, tag)
        
        // 更新错误计数
        _errorCount.value = _errorCount.value + 1
        
        // 发送错误通知
        _errorFlow.value = appException

        // 处理关键错误
        if (AppException.isCritical(appException)) {
            handleCriticalError(appException)
        }
    }

    fun clearError() {
        _errorFlow.value = null
    }

    private fun logError(exception: AppException, tag: String? = null) {
        val logTag = tag ?: "App"
        val timestamp = dateFormat.format(Date())
        
        val errorMessage = buildString {
            append("[$timestamp] ")
            append("Error(${exception.errorCode}): ${exception.message}")
            exception.cause?.let { cause ->
                append("\nCause: ${cause.message}")
                append("\nStacktrace: ${Log.getStackTraceString(cause)}")
            }
            append("\n")
        }

        // 开发环境日志
        if (BuildConfig.DEBUG) {
            Log.e(logTag, errorMessage)
        }

        // 写入文件日志
        try {
            logFile.appendText(errorMessage)
        } catch (e: Exception) {
            Log.e(logTag, "Failed to write error log: ${e.message}")
        }

        // TODO: 集成Crash reporting工具(如Firebase Crashlytics)
    }

    private fun handleCriticalError(exception: AppException) {
        when (exception) {
            is AppException.DatabaseException -> {
                if (exception.errorCode == AppException.DatabaseException.MIGRATION_ERROR) {
                    // TODO: 提示用户重新安装应用
                }
            }
            is AppException.AuthenticationException -> {
                if (exception.errorCode == AppException.AuthenticationException.TOKEN_EXPIRED) {
                    // TODO: 重新登录
                }
            }
            is AppException.ResourceException -> {
                if (exception.errorCode == AppException.ResourceException.STORAGE_FULL) {
                    // TODO: 提示用户清理存储空间
                }
            }
            else -> {
                // 处理其他关键错误
            }
        }
    }

    fun getErrorMessage(exception: AppException): String {
        val baseMessage = when (exception) {
            is AppException.NetworkException -> when (exception.errorCode) {
                AppException.NetworkException.HOST_ERROR -> "无法连接到服务器"
                AppException.NetworkException.TIMEOUT_ERROR -> "连接超时"
                else -> "网络连接失败"
            }
            is AppException.DatabaseException -> when (exception.errorCode) {
                AppException.DatabaseException.CONSTRAINT_ERROR -> "数据约束冲突"
                AppException.DatabaseException.MIGRATION_ERROR -> "数据库版本不兼容"
                else -> "数据操作失败"
            }
            is AppException.ValidationException -> when (exception.errorCode) {
                AppException.ValidationException.REQUIRED_FIELD_ERROR -> "必填字段为空"
                AppException.ValidationException.FORMAT_ERROR -> "数据格式错误"
                AppException.ValidationException.RANGE_ERROR -> "数据超出范围"
                else -> exception.message
            }
            is AppException.AuthenticationException -> when (exception.errorCode) {
                AppException.AuthenticationException.TOKEN_EXPIRED -> "登录已过期"
                AppException.AuthenticationException.INVALID_CREDENTIALS -> "用户名或密码错误"
                AppException.AuthenticationException.PERMISSION_DENIED -> "权限不足"
                else -> "认证失败"
            }
            is AppException.BusinessException -> when (exception.errorCode) {
                AppException.BusinessException.INSUFFICIENT_BALANCE -> "余额不足"
                AppException.BusinessException.ACCOUNT_LOCKED -> "账户已锁定"
                AppException.BusinessException.OPERATION_NOT_ALLOWED -> "操作不允许"
                else -> exception.message
            }
            is AppException.ResourceException -> when (exception.errorCode) {
                AppException.ResourceException.FILE_NOT_FOUND -> "文件不存在"
                AppException.ResourceException.STORAGE_FULL -> "存储空间不足"
                AppException.ResourceException.ACCESS_DENIED -> "访问被拒绝"
                else -> "资源访问失败"
            }
            is AppException.ConcurrencyException -> when (exception.errorCode) {
                AppException.ConcurrencyException.OPTIMISTIC_LOCK -> "数据已被修改"
                AppException.ConcurrencyException.DEADLOCK -> "操作冲突"
                AppException.ConcurrencyException.TIMEOUT -> "操作超时"
                else -> "并发操作失败"
            }
            is AppException.UnknownException -> "发生未知错误"
        }

        return if (BuildConfig.DEBUG) {
            "$baseMessage (${exception.errorCode})"
        } else {
            baseMessage
        }
    }

    fun getRecoverySuggestion(exception: AppException): String? {
        return when (exception) {
            is AppException.NetworkException -> when (exception.errorCode) {
                AppException.NetworkException.HOST_ERROR -> "请检查网络连接是否正常"
                AppException.NetworkException.TIMEOUT_ERROR -> "请检查网络信号是否良好"
                else -> "请检查网络设置"
            }
            is AppException.DatabaseException -> when (exception.errorCode) {
                AppException.DatabaseException.MIGRATION_ERROR -> "请尝试重新安装应用"
                else -> "请尝试重启应用"
            }
            is AppException.ValidationException -> when (exception.errorCode) {
                AppException.ValidationException.REQUIRED_FIELD_ERROR -> "请填写必填字段"
                AppException.ValidationException.FORMAT_ERROR -> "请检查输入格式"
                AppException.ValidationException.RANGE_ERROR -> "请检查输入范围"
                else -> null
            }
            is AppException.AuthenticationException -> when (exception.errorCode) {
                AppException.AuthenticationException.TOKEN_EXPIRED -> "请重新登录"
                AppException.AuthenticationException.INVALID_CREDENTIALS -> "请检查用户名和密码"
                else -> "请重新登录"
            }
            is AppException.BusinessException -> when (exception.errorCode) {
                AppException.BusinessException.INSUFFICIENT_BALANCE -> "请检查账户余额"
                AppException.BusinessException.ACCOUNT_LOCKED -> "请联系客服解锁"
                else -> null
            }
            is AppException.ResourceException -> when (exception.errorCode) {
                AppException.ResourceException.STORAGE_FULL -> "请清理存储空间"
                AppException.ResourceException.ACCESS_DENIED -> "请检查访问权限"
                else -> null
            }
            is AppException.ConcurrencyException -> when (exception.errorCode) {
                AppException.ConcurrencyException.OPTIMISTIC_LOCK -> "请刷新后重试"
                AppException.ConcurrencyException.DEADLOCK -> "请稍后重试"
                else -> "请重试操作"
            }
            is AppException.UnknownException -> "请尝试重启应用"
        }
    }

    fun canRetry(exception: AppException): Boolean {
        return AppException.needsRetry(exception)
    }

    fun clearErrorLog() {
        try {
            logFile.delete()
            logFile.createNewFile()
        } catch (e: Exception) {
            Log.e("ErrorHandler", "Failed to clear error log: ${e.message}")
        }
    }

    fun getErrorLog(): String {
        return try {
            logFile.readText()
        } catch (e: Exception) {
            Log.e("ErrorHandler", "Failed to read error log: ${e.message}")
            ""
        }
    }
} 
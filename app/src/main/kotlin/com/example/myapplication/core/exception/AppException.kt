package com.example.myapplication.core.exception

sealed class AppException(
    override val message: String,
    override val cause: Throwable? = null,
    val errorCode: Int = 0
) : Exception(message, cause) {

    class NetworkException(
        message: String = "网络连接失败",
        cause: Throwable? = null,
        errorCode: Int = NETWORK_ERROR
    ) : AppException(message, cause, errorCode) {
        companion object {
            const val NETWORK_ERROR = 1001
            const val TIMEOUT_ERROR = 1002
            const val HOST_ERROR = 1003
        }
    }

    class DatabaseException(
        message: String = "数据库操作失败",
        cause: Throwable? = null,
        errorCode: Int = DATABASE_ERROR
    ) : AppException(message, cause, errorCode) {
        companion object {
            const val DATABASE_ERROR = 2001
            const val TRANSACTION_ERROR = 2002
            const val MIGRATION_ERROR = 2003
            const val CONSTRAINT_ERROR = 2004
        }
    }

    class ValidationException(
        message: String = "数据验证失败",
        cause: Throwable? = null,
        errorCode: Int = VALIDATION_ERROR
    ) : AppException(message, cause, errorCode) {
        companion object {
            const val VALIDATION_ERROR = 3001
            const val REQUIRED_FIELD_ERROR = 3002
            const val FORMAT_ERROR = 3003
            const val RANGE_ERROR = 3004
        }
    }

    class AuthenticationException(
        message: String = "认证失败",
        cause: Throwable? = null,
        errorCode: Int = AUTH_ERROR
    ) : AppException(message, cause, errorCode) {
        companion object {
            const val AUTH_ERROR = 4001
            const val TOKEN_EXPIRED = 4002
            const val INVALID_CREDENTIALS = 4003
            const val PERMISSION_DENIED = 4004
        }
    }

    class BusinessException(
        message: String,
        cause: Throwable? = null,
        errorCode: Int = BUSINESS_ERROR
    ) : AppException(message, cause, errorCode) {
        companion object {
            const val BUSINESS_ERROR = 5001
            const val INSUFFICIENT_BALANCE = 5002
            const val ACCOUNT_LOCKED = 5003
            const val OPERATION_NOT_ALLOWED = 5004
        }
    }

    class ResourceException(
        message: String = "资源访问失败",
        cause: Throwable? = null,
        errorCode: Int = RESOURCE_ERROR
    ) : AppException(message, cause, errorCode) {
        companion object {
            const val RESOURCE_ERROR = 6001
            const val FILE_NOT_FOUND = 6002
            const val STORAGE_FULL = 6003
            const val ACCESS_DENIED = 6004
        }
    }

    class ConcurrencyException(
        message: String = "并发操作冲突",
        cause: Throwable? = null,
        errorCode: Int = CONCURRENCY_ERROR
    ) : AppException(message, cause, errorCode) {
        companion object {
            const val CONCURRENCY_ERROR = 7001
            const val OPTIMISTIC_LOCK = 7002
            const val DEADLOCK = 7003
            const val TIMEOUT = 7004
        }
    }

    class UnknownException(
        message: String = "未知错误",
        cause: Throwable? = null,
        errorCode: Int = UNKNOWN_ERROR
    ) : AppException(message, cause, errorCode) {
        companion object {
            const val UNKNOWN_ERROR = 9999
        }
    }

    companion object {
        fun from(throwable: Throwable): AppException {
            return when (throwable) {
                is AppException -> throwable
                
                // 网络错误
                is java.net.UnknownHostException -> 
                    NetworkException(errorCode = NetworkException.HOST_ERROR, cause = throwable)
                is java.net.SocketTimeoutException -> 
                    NetworkException(errorCode = NetworkException.TIMEOUT_ERROR, cause = throwable)
                is java.io.IOException -> 
                    NetworkException(cause = throwable)
                
                // 数据库错误
                is android.database.sqlite.SQLiteException -> {
                    when {
                        throwable.message?.contains("UNIQUE constraint failed") == true ->
                            DatabaseException(errorCode = DatabaseException.CONSTRAINT_ERROR, cause = throwable)
                        throwable.message?.contains("no such table") == true ->
                            DatabaseException(errorCode = DatabaseException.MIGRATION_ERROR, cause = throwable)
                        else -> DatabaseException(cause = throwable)
                    }
                }
                
                // 验证错误
                is IllegalArgumentException ->
                    ValidationException(throwable.message ?: "参数验证失败", throwable)
                is IllegalStateException ->
                    ValidationException(throwable.message ?: "状态验证失败", throwable)
                
                // 资源错误
                is java.io.FileNotFoundException ->
                    ResourceException(errorCode = ResourceException.FILE_NOT_FOUND, cause = throwable)
                is java.io.EOFException ->
                    ResourceException(errorCode = ResourceException.ACCESS_DENIED, cause = throwable)
                
                // 其他错误
                else -> UnknownException(throwable.message ?: "未知错误", throwable)
            }
        }

        fun isNetworkError(exception: AppException): Boolean {
            return exception is NetworkException
        }

        fun isAuthError(exception: AppException): Boolean {
            return exception is AuthenticationException
        }

        fun isValidationError(exception: AppException): Boolean {
            return exception is ValidationException
        }

        fun isBusinessError(exception: AppException): Boolean {
            return exception is BusinessException
        }

        fun needsRetry(exception: AppException): Boolean {
            return when (exception) {
                is NetworkException -> true
                is DatabaseException -> exception.errorCode != DatabaseException.CONSTRAINT_ERROR
                is ConcurrencyException -> true
                else -> false
            }
        }

        fun isCritical(exception: AppException): Boolean {
            return when (exception) {
                is DatabaseException -> exception.errorCode == DatabaseException.MIGRATION_ERROR
                is AuthenticationException -> exception.errorCode == AuthenticationException.TOKEN_EXPIRED
                is ResourceException -> exception.errorCode == ResourceException.STORAGE_FULL
                else -> false
            }
        }
    }
} 
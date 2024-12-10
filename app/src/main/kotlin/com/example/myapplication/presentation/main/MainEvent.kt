package com.example.myapplication.presentation.main

import com.example.myapplication.data.local.entity.TransactionType
import java.time.YearMonth

sealed class MainEvent {
    // 账户相关
    data class SelectAccount(val accountId: Long?) : MainEvent()
    
    // 月份选择
    data class SelectMonth(val yearMonth: YearMonth) : MainEvent()
    
    // 交易相关
    data object ShowAddTransactionDialog : MainEvent()
    data object HideAddTransactionDialog : MainEvent()
    data class AddTransaction(
        val amount: Double,
        val type: TransactionType,
        val categoryId: Long,
        val accountId: Long,
        val note: String?
    ) : MainEvent()
    data class DeleteTransaction(val transactionId: Long) : MainEvent()
    
    // 筛选相关
    data object ShowFilterDialog : MainEvent()
    data object HideFilterDialog : MainEvent()
    data class ApplyFilter(
        val type: TransactionType?,
        val categoryId: Long?,
        val accountId: Long?
    ) : MainEvent()
    data object ClearFilter : MainEvent()
    
    // 错误处理
    data object DismissError : MainEvent()
    
    // 刷新数据
    data object Refresh : MainEvent()
} 
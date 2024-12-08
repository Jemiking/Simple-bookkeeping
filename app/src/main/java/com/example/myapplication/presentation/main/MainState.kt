package com.example.myapplication.presentation.main

import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.model.Budget
import com.example.myapplication.domain.model.Transaction
import java.time.YearMonth

data class MainState(
    // 账户相关
    val accounts: List<Account> = emptyList(),
    val totalBalance: Double = 0.0,
    val selectedAccountId: Long? = null,
    
    // 收支统计
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val monthlyBalance: Double = 0.0,
    val selectedMonth: YearMonth = YearMonth.now(),
    
    // 预算相关
    val monthlyBudget: Budget? = null,
    val budgetProgress: Float = 0f,
    val isOverBudget: Boolean = false,
    
    // 交易记录
    val recentTransactions: List<Transaction> = emptyList(),
    
    // UI状态
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAddTransactionDialogVisible: Boolean = false,
    val isFilterDialogVisible: Boolean = false
) 
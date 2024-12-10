package com.example.myapplication.presentation.transaction

import com.example.myapplication.data.local.entity.TransactionType
import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.model.Category
import java.time.LocalDateTime

data class AddTransactionState(
    // 基本信息
    val amount: String = "0",
    val type: TransactionType = TransactionType.EXPENSE,
    val selectedCategory: Category? = null,
    val selectedAccount: Account? = null,
    val note: String = "",
    val date: LocalDateTime = LocalDateTime.now(),
    
    // 数据列表
    val categories: List<Category> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val recentCategories: List<Category> = emptyList(),
    
    // UI状态
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDatePickerVisible: Boolean = false,
    val isAccountPickerVisible: Boolean = false,
    
    // 验证状态
    val amountError: String? = null,
    val categoryError: String? = null,
    val accountError: String? = null
) 
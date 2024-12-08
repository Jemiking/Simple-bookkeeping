package com.example.myapplication.presentation.transaction

import com.example.myapplication.data.local.entity.TransactionType
import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.model.Category
import java.time.LocalDateTime

sealed class AddTransactionEvent {
    // 基本信息修改
    data class AmountChanged(val amount: String) : AddTransactionEvent()
    data class TypeChanged(val type: TransactionType) : AddTransactionEvent()
    data class CategorySelected(val category: Category) : AddTransactionEvent()
    data class AccountSelected(val account: Account) : AddTransactionEvent()
    data class NoteChanged(val note: String) : AddTransactionEvent()
    data class DateChanged(val date: LocalDateTime) : AddTransactionEvent()
    
    // UI事件
    data object ToggleDatePicker : AddTransactionEvent()
    data object ToggleAccountPicker : AddTransactionEvent()
    data object DismissError : AddTransactionEvent()
    
    // 保存事件
    data object SaveTransaction : AddTransactionEvent()
    
    // 数字键盘事件
    data class NumberPressed(val number: Int) : AddTransactionEvent()
    data object DecimalPressed : AddTransactionEvent()
    data object BackspacePressed : AddTransactionEvent()
    data object ClearPressed : AddTransactionEvent()
} 
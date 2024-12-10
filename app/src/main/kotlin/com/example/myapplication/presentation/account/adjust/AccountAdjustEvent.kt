package com.example.myapplication.presentation.account.adjust

import com.example.myapplication.domain.model.Account

sealed class AccountAdjustEvent {
    // 选择要调整余额的账户
    data class SelectAccount(val account: Account) : AccountAdjustEvent()

    // 更新新的余额值
    data class UpdateNewBalance(val balance: String) : AccountAdjustEvent()

    // 更新调整说明
    data class UpdateNote(val note: String) : AccountAdjustEvent()

    // 显示账户选择器
    data object ShowAccountSelector : AccountAdjustEvent()

    // 隐藏账户选择器
    data object HideAccountSelector : AccountAdjustEvent()

    // 执行余额调整
    data object AdjustBalance : AccountAdjustEvent()

    // 返回上一页
    data object NavigateBack : AccountAdjustEvent()
}
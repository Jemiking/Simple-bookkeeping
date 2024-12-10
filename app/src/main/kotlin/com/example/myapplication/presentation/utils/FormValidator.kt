package com.example.myapplication.presentation.utils

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}

object FormValidator {
    fun validateAmount(amount: String): ValidationResult {
        return when {
            amount.isEmpty() -> ValidationResult.Error("请输入金额")
            amount.toDoubleOrNull() == null -> ValidationResult.Error("请输入有效的金额")
            amount.toDouble() <= 0 -> ValidationResult.Error("金额必须大于0")
            amount.contains(Regex("[^0-9.]")) -> ValidationResult.Error("金额只能包含数字和小数点")
            amount.count { it == '.' } > 1 -> ValidationResult.Error("金额格式不正确")
            amount.substringAfter(".", "").length > 2 -> ValidationResult.Error("金额最多支持两位小数")
            else -> ValidationResult.Success
        }
    }

    fun validateAccountSelection(accountId: Long?, toAccountId: Long?, isTransfer: Boolean): ValidationResult {
        return when {
            accountId == null -> ValidationResult.Error("请选择���户")
            isTransfer && toAccountId == null -> ValidationResult.Error("请选择转入账户")
            isTransfer && accountId == toAccountId -> ValidationResult.Error("转入账户不能与转出账户相同")
            else -> ValidationResult.Success
        }
    }

    fun validateCategorySelection(categoryId: Long?, isTransfer: Boolean): ValidationResult {
        return when {
            !isTransfer && categoryId == null -> ValidationResult.Error("请选择分类")
            else -> ValidationResult.Success
        }
    }

    fun validateNote(note: String): ValidationResult {
        return when {
            note.length > 200 -> ValidationResult.Error("备注不能超过200个字符")
            else -> ValidationResult.Success
        }
    }

    fun validateDate(date: String): ValidationResult {
        // 这里可以添加日期格式验证
        return ValidationResult.Success
    }

    fun validateAll(
        amount: String,
        accountId: Long?,
        toAccountId: Long?,
        categoryId: Long?,
        isTransfer: Boolean,
        note: String
    ): List<ValidationResult> {
        return listOf(
            validateAmount(amount),
            validateAccountSelection(accountId, toAccountId, isTransfer),
            validateCategorySelection(categoryId, isTransfer),
            validateNote(note)
        )
    }
} 
package com.example.myapplication.data.local

import com.example.myapplication.data.local.entity.AccountEntity
import com.example.myapplication.data.local.entity.AccountType
import com.example.myapplication.data.local.entity.CategoryEntity
import com.example.myapplication.data.local.entity.TransactionType

object DefaultData {
    // 默认支出分类
    val defaultExpenseCategories = listOf(
        CategoryEntity(
            name = "餐饮",
            icon = "restaurant",
            color = 0xFFE74C3C.toInt(),
            type = TransactionType.EXPENSE,
            orderIndex = 0,
            isDefault = true
        ),
        CategoryEntity(
            name = "交通",
            icon = "directions_car",
            color = 0xFF3498DB.toInt(),
            type = TransactionType.EXPENSE,
            orderIndex = 1,
            isDefault = true
        ),
        CategoryEntity(
            name = "购物",
            icon = "shopping_cart",
            color = 0xFF9B59B6.toInt(),
            type = TransactionType.EXPENSE,
            orderIndex = 2,
            isDefault = true
        ),
        CategoryEntity(
            name = "居住",
            icon = "home",
            color = 0xFF1ABC9C.toInt(),
            type = TransactionType.EXPENSE,
            orderIndex = 3,
            isDefault = true
        ),
        CategoryEntity(
            name = "娱乐",
            icon = "sports_esports",
            color = 0xFFF1C40F.toInt(),
            type = TransactionType.EXPENSE,
            orderIndex = 4,
            isDefault = true
        )
    )

    // 默认收入分类
    val defaultIncomeCategories = listOf(
        CategoryEntity(
            name = "工资",
            icon = "work",
            color = 0xFF2ECC71.toInt(),
            type = TransactionType.INCOME,
            orderIndex = 0,
            isDefault = true
        ),
        CategoryEntity(
            name = "奖金",
            icon = "card_giftcard",
            color = 0xFFF39C12.toInt(),
            type = TransactionType.INCOME,
            orderIndex = 1,
            isDefault = true
        ),
        CategoryEntity(
            name = "理财",
            icon = "account_balance",
            color = 0xFF3498DB.toInt(),
            type = TransactionType.INCOME,
            orderIndex = 2,
            isDefault = true
        ),
        CategoryEntity(
            name = "其他收入",
            icon = "add_circle",
            color = 0xFF95A5A6.toInt(),
            type = TransactionType.INCOME,
            orderIndex = 3,
            isDefault = true
        )
    )

    // 默认账户
    val defaultAccounts = listOf(
        AccountEntity(
            name = "现金",
            type = AccountType.CASH,
            balance = 0.0,
            icon = "money",
            color = 0xFF2ECC71.toInt(),
            orderIndex = 0,
            isDefault = true
        ),
        AccountEntity(
            name = "银行卡",
            type = AccountType.BANK_CARD,
            balance = 0.0,
            icon = "credit_card",
            color = 0xFF3498DB.toInt(),
            orderIndex = 1,
            isDefault = true
        ),
        AccountEntity(
            name = "支付宝",
            type = AccountType.ALIPAY,
            balance = 0.0,
            icon = "account_balance_wallet",
            color = 0xFF1ABC9C.toInt(),
            orderIndex = 2,
            isDefault = true
        ),
        AccountEntity(
            name = "微信",
            type = AccountType.WECHAT,
            balance = 0.0,
            icon = "chat",
            color = 0xFF2ECC71.toInt(),
            orderIndex = 3,
            isDefault = true
        )
    )
} 
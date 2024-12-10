package com.example.myapplication.data.local.migration

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.data.local.dao.AccountDao
import com.example.myapplication.data.local.dao.CategoryDao
import com.example.myapplication.data.local.entity.AccountEntity
import com.example.myapplication.data.local.entity.CategoryEntity
import com.example.myapplication.domain.model.AccountType
import com.example.myapplication.domain.model.CategoryType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * 数据库回调
 * 用于在数据库创建和打开时执行一些操作
 * 例如：初始化默认数据、数据验证和修复
 */
class DatabaseCallback @Inject constructor(
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        CoroutineScope(Dispatchers.IO).launch {
            initDefaultData()
        }
    }

    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        CoroutineScope(Dispatchers.IO).launch {
            validateAndRepairData()
        }
    }

    private suspend fun initDefaultData() {
        // 检查是否已经存在数据
        val existingAccounts = accountDao.getAllAccounts().first()
        val existingCategories = categoryDao.getAllCategories().first()

        if (existingAccounts.isEmpty()) {
            initDefaultAccounts()
        }

        if (existingCategories.isEmpty()) {
            initDefaultCategories()
        }
    }

    private suspend fun initDefaultAccounts() {
        val defaultAccounts = listOf(
            AccountEntity(
                name = "现金",
                type = AccountType.CASH.name,
                balance = 0.0,
                currency = "CNY",
                icon = "cash",
                color = "#4CAF50",
                note = "现金账户",
                isArchived = false,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            AccountEntity(
                name = "银行卡",
                type = AccountType.BANK_CARD.name,
                balance = 0.0,
                currency = "CNY",
                icon = "credit-card",
                color = "#2196F3",
                note = "银行卡账户",
                isArchived = false,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            AccountEntity(
                name = "支付宝",
                type = AccountType.ALIPAY.name,
                balance = 0.0,
                currency = "CNY",
                icon = "alipay",
                color = "#1976D2",
                note = "支付宝账户",
                isArchived = false,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            AccountEntity(
                name = "微信",
                type = AccountType.WECHAT.name,
                balance = 0.0,
                currency = "CNY",
                icon = "wechat",
                color = "#4CAF50",
                note = "微信账户",
                isArchived = false,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )

        defaultAccounts.forEach { account ->
            accountDao.insert(account)
        }
    }

    private suspend fun initDefaultCategories() {
        // 初始化默认支出分类
        val defaultExpenseCategories = listOf(
            CategoryEntity(
                name = "餐饮",
                type = CategoryType.EXPENSE.name,
                icon = "restaurant",
                color = "#F44336",
                order = 0,
                isArchived = false,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            CategoryEntity(
                name = "交通",
                type = CategoryType.EXPENSE.name,
                icon = "directions-car",
                color = "#2196F3",
                order = 1,
                isArchived = false,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            CategoryEntity(
                name = "购物",
                type = CategoryType.EXPENSE.name,
                icon = "shopping-cart",
                color = "#4CAF50",
                order = 2,
                isArchived = false,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            CategoryEntity(
                name = "娱乐",
                type = CategoryType.EXPENSE.name,
                icon = "local-play",
                color = "#9C27B0",
                order = 3,
                isArchived = false,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            CategoryEntity(
                name = "居住",
                type = CategoryType.EXPENSE.name,
                icon = "home",
                color = "#795548",
                order = 4,
                isArchived = false,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            CategoryEntity(
                name = "通讯",
                type = CategoryType.EXPENSE.name,
                icon = "phone",
                color = "#FF9800",
                order = 5,
                isArchived = false,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            CategoryEntity(
                name = "医疗",
                type = CategoryType.EXPENSE.name,
                icon = "local-hospital",
                color = "#E91E63",
                order = 6,
                isArchived = false,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            CategoryEntity(
                name = "教育",
                type = CategoryType.EXPENSE.name,
                icon = "school",
                color = "#3F51B5",
                order = 7,
                isArchived = false,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )

        defaultExpenseCategories.forEach { category ->
            categoryDao.insert(category)
        }

        // 初始化默认收入分类
        val defaultIncomeCategories = listOf(
            CategoryEntity(
                name = "工资",
                type = CategoryType.INCOME.name,
                icon = "work",
                color = "#4CAF50",
                order = 0,
                isArchived = false,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            CategoryEntity(
                name = "奖金",
                type = CategoryType.INCOME.name,
                icon = "star",
                color = "#FFC107",
                order = 1,
                isArchived = false,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            CategoryEntity(
                name = "投资",
                type = CategoryType.INCOME.name,
                icon = "trending-up",
                color = "#2196F3",
                order = 2,
                isArchived = false,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            CategoryEntity(
                name = "兼职",
                type = CategoryType.INCOME.name,
                icon = "business-center",
                color = "#9C27B0",
                order = 3,
                isArchived = false,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            CategoryEntity(
                name = "其他",
                type = CategoryType.INCOME.name,
                icon = "more-horiz",
                color = "#607D8B",
                order = 4,
                isArchived = false,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )

        defaultIncomeCategories.forEach { category ->
            categoryDao.insert(category)
        }

        // 初始化默认转账分类
        val defaultTransferCategory = CategoryEntity(
            name = "转账",
            type = CategoryType.TRANSFER.name,
            icon = "swap-horiz",
            color = "#607D8B",
            order = 0,
            isArchived = false,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        categoryDao.insert(defaultTransferCategory)
    }

    private suspend fun validateAndRepairData() {
        // 验证账户数据
        validateAccounts()
        
        // 验证分类数据
        validateCategories()
        
        // 修复分类顺序
        repairCategoryOrder()
    }

    private suspend fun validateAccounts() {
        val accounts = accountDao.getAllAccounts().first()
        
        // 确保至少有一个默认账户
        if (accounts.isEmpty()) {
            initDefaultAccounts()
            return
        }

        // 验证账户类型
        accounts.forEach { account ->
            try {
                AccountType.valueOf(account.type)
            } catch (e: IllegalArgumentException) {
                // 如果账户类型无效，将其设置为OTHER
                accountDao.update(account.copy(type = AccountType.OTHER.name))
            }
        }
    }

    private suspend fun validateCategories() {
        val categories = categoryDao.getAllCategories().first()
        
        // 确保至少有基本分类
        if (categories.isEmpty()) {
            initDefaultCategories()
            return
        }

        // 验证分类类型
        categories.forEach { category ->
            try {
                CategoryType.valueOf(category.type)
            } catch (e: IllegalArgumentException) {
                // 如果分类类型无效，将其设置为EXPENSE
                categoryDao.update(category.copy(type = CategoryType.EXPENSE.name))
            }
        }

        // 确保存在转账分类
        val hasTransferCategory = categories.any { it.type == CategoryType.TRANSFER.name }
        if (!hasTransferCategory) {
            val transferCategory = CategoryEntity(
                name = "转账",
                type = CategoryType.TRANSFER.name,
                icon = "swap-horiz",
                color = "#607D8B",
                order = categories.size,
                isArchived = false,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            categoryDao.insert(transferCategory)
        }
    }

    private suspend fun repairCategoryOrder() {
        // 修复每种类型的分类顺序
        CategoryType.values().forEach { type ->
            val categories = categoryDao.getCategoriesByType(type.name).first()
            categories.forEachIndexed { index, category ->
                if (category.order != index) {
                    categoryDao.update(category.copy(order = index))
                }
            }
        }
    }
} 
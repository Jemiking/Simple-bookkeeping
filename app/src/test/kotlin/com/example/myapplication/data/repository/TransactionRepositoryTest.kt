package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.AccountDao
import com.example.myapplication.data.local.dao.TransactionDao
import com.example.myapplication.data.local.entity.TransactionEntity
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.model.TransactionType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.time.YearMonth

class TransactionRepositoryTest {
    private lateinit var transactionDao: TransactionDao
    private lateinit var accountDao: AccountDao
    private lateinit var repository: TransactionRepositoryImpl

    @Before
    fun setup() {
        transactionDao = mockk(relaxed = true)
        accountDao = mockk(relaxed = true)
        repository = TransactionRepositoryImpl(transactionDao, accountDao)
    }

    @Test
    fun `创建交易时更新账户余额`() = runTest {
        // 准备测试数据
        val transaction = Transaction(
            id = 0,
            amount = 1000.0,
            type = TransactionType.INCOME,
            accountId = 1,
            categoryId = 1,
            date = LocalDateTime.now(),
            note = "测试"
        )

        // Mock DAO响应
        coEvery { accountDao.getAccountBalance(1) } returns 2000.0
        coEvery { transactionDao.insert(any()) } returns 1

        // 执行测试
        val result = repository.createTransaction(transaction)

        // 验证结果
        assertEquals(1L, result)

        // 验证调用
        coVerify {
            transactionDao.insert(any())
            accountDao.getAccountBalance(1)
            accountDao.updateAccountBalance(1, 3000.0)
        }
    }

    @Test
    fun `更新交易时正确处理账户余额`() = runTest {
        // 准备测试数据
        val oldTransaction = Transaction(
            id = 1,
            amount = 1000.0,
            type = TransactionType.INCOME,
            accountId = 1,
            categoryId = 1,
            date = LocalDateTime.now(),
            note = "原始交易"
        )

        val newTransaction = oldTransaction.copy(
            amount = 1500.0,
            note = "���新后的交易"
        )

        // Mock DAO响应
        coEvery { transactionDao.getTransactionById(1) } returns oldTransaction.toEntity()
        coEvery { accountDao.getAccountBalance(1) } returns 2000.0

        // 执行测试
        repository.updateTransaction(newTransaction)

        // 验证调用
        coVerify {
            transactionDao.update(any())
            accountDao.getAccountBalance(1)
            accountDao.updateAccountBalance(1, 2500.0)
        }
    }

    @Test
    fun `删除交易时更新账户余额`() = runTest {
        // 准备测试数据
        val transaction = Transaction(
            id = 1,
            amount = 1000.0,
            type = TransactionType.INCOME,
            accountId = 1,
            categoryId = 1,
            date = LocalDateTime.now(),
            note = "测试"
        )

        // Mock DAO响应
        coEvery { accountDao.getAccountBalance(1) } returns 2000.0

        // 执行测试
        repository.deleteTransaction(transaction)

        // 验证调用
        coVerify {
            transactionDao.delete(any())
            accountDao.getAccountBalance(1)
            accountDao.updateAccountBalance(1, 1000.0)
        }
    }

    @Test
    fun `按日期范围获取交易记录`() = runTest {
        // 准备测试数据
        val startDate = LocalDateTime.now().minusDays(7)
        val endDate = LocalDateTime.now()
        val transactions = listOf(
            TransactionEntity(
                id = 1,
                amount = 1000.0,
                type = "INCOME",
                accountId = 1,
                categoryId = 1,
                date = LocalDateTime.now().toString(),
                note = "测试1"
            ),
            TransactionEntity(
                id = 2,
                amount = 500.0,
                type = "EXPENSE",
                accountId = 1,
                categoryId = 2,
                date = LocalDateTime.now().minusDays(1).toString(),
                note = "测试2"
            )
        )

        // Mock DAO响应
        coEvery {
            transactionDao.getTransactionsByDateRange(
                startDate = startDate.toString(),
                endDate = endDate.toString()
            )
        } returns flowOf(transactions)

        // 执行测试
        val result = repository.getTransactionsByDateRange(startDate, endDate).first()

        // 验证结果
        assertEquals(2, result.size)
        assertEquals(1000.0, result[0].amount, 0.01)
        assertEquals(500.0, result[1].amount, 0.01)
    }

    @Test
    fun `按账户获取交易记录`() = runTest {
        // 准备测试数据
        val accountId = 1L
        val transactions = listOf(
            TransactionEntity(
                id = 1,
                amount = 1000.0,
                type = "INCOME",
                accountId = accountId,
                categoryId = 1,
                date = LocalDateTime.now().toString(),
                note = "测试1"
            ),
            TransactionEntity(
                id = 2,
                amount = 500.0,
                type = "EXPENSE",
                accountId = accountId,
                categoryId = 2,
                date = LocalDateTime.now().minusDays(1).toString(),
                note = "测试2"
            )
        )

        // Mock DAO响应
        coEvery {
            transactionDao.getTransactionsByAccount(accountId)
        } returns flowOf(transactions)

        // 执行测试
        val result = repository.getTransactionsByAccount(accountId).first()

        // 验证结果
        assertEquals(2, result.size)
        result.forEach { transaction ->
            assertEquals(accountId, transaction.accountId)
        }
    }

    @Test
    fun `按分类获取交易记录`() = runTest {
        // 准备测试数据
        val categoryId = 1L
        val transactions = listOf(
            TransactionEntity(
                id = 1,
                amount = 1000.0,
                type = "INCOME",
                accountId = 1,
                categoryId = categoryId,
                date = LocalDateTime.now().toString(),
                note = "测试1"
            ),
            TransactionEntity(
                id = 2,
                amount = 500.0,
                type = "EXPENSE",
                accountId = 2,
                categoryId = categoryId,
                date = LocalDateTime.now().minusDays(1).toString(),
                note = "测试2"
            )
        )

        // Mock DAO响应
        coEvery {
            transactionDao.getTransactionsByCategory(categoryId)
        } returns flowOf(transactions)

        // 执行测试
        val result = repository.getTransactionsByCategory(categoryId).first()

        // 验证结果
        assertEquals(2, result.size)
        result.forEach { transaction ->
            assertEquals(categoryId, transaction.categoryId)
        }
    }

    @Test
    fun `搜索交易记录`() = runTest {
        // 准备测试数据
        val query = "测试"
        val transactions = listOf(
            TransactionEntity(
                id = 1,
                amount = 1000.0,
                type = "INCOME",
                accountId = 1,
                categoryId = 1,
                date = LocalDateTime.now().toString(),
                note = "测试查询1"
            ),
            TransactionEntity(
                id = 2,
                amount = 500.0,
                type = "EXPENSE",
                accountId = 2,
                categoryId = 2,
                date = LocalDateTime.now().minusDays(1).toString(),
                note = "测试查询2"
            )
        )

        // Mock DAO响应
        coEvery {
            transactionDao.searchTransactionsWithRelations(query)
        } returns flowOf(transactions)

        // 执行测试
        val result = repository.searchTransactions(query).first()

        // 验证结果
        assertEquals(2, result.size)
        result.forEach { transaction ->
            assert(transaction.note.contains(query))
        }
    }
} 
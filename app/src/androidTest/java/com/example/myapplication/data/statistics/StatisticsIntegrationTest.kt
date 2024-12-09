package com.example.myapplication.data.statistics

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.dao.TransactionDao
import com.example.myapplication.data.local.entity.TransactionEntity
import com.example.myapplication.data.local.entity.TransactionType
import com.example.myapplication.domain.repository.TransactionRepository
import com.example.myapplication.data.repository.TransactionRepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.time.YearMonth
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@LargeTest
class StatisticsIntegrationTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var transactionDao: TransactionDao
    private lateinit var repository: TransactionRepository

    @Before
    fun setup() {
        // 使用内存数据库进行测试
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        transactionDao = database.transactionDao()
        repository = TransactionRepositoryImpl(transactionDao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testLoadMonthlyStatistics() = runTest {
        // 准备测试数据
        val currentMonth = YearMonth.now()
        val transactions = listOf(
            createTransactionEntity(1000.0, TransactionType.INCOME, currentMonth),
            createTransactionEntity(500.0, TransactionType.EXPENSE, currentMonth),
            createTransactionEntity(1500.0, TransactionType.INCOME, currentMonth.minusMonths(1))
        )
        
        // 插入测试数据
        transactions.forEach { transaction ->
            transactionDao.insert(transaction)
        }

        // 获取当月统计数据
        val monthStart = currentMonth.atDay(1).atStartOfDay()
        val monthEnd = currentMonth.atEndOfMonth().atTime(23, 59, 59)
        
        val monthlyTransactions = repository
            .getTransactionsByDateRange(monthStart, monthEnd)
            .first()

        // 验证结果
        assertEquals(2, monthlyTransactions.size)
        assertEquals(1000.0, monthlyTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount })
        assertEquals(500.0, monthlyTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount })
    }

    @Test
    fun testLargeDataSetPerformance() = runTest {
        // 准备大量测试数据
        val currentMonth = YearMonth.now()
        val transactions = List(1000) { index ->
            createTransactionEntity(
                amount = 100.0 + index,
                type = if (index % 2 == 0) TransactionType.INCOME else TransactionType.EXPENSE,
                yearMonth = currentMonth
            )
        }
        
        // 批量插入数据
        transactions.forEach { transaction ->
            transactionDao.insert(transaction)
        }

        // 测试查询性能
        val startTime = System.currentTimeMillis()
        
        val monthStart = currentMonth.atDay(1).atStartOfDay()
        val monthEnd = currentMonth.atEndOfMonth().atTime(23, 59, 59)
        
        val result = repository
            .getTransactionsByDateRange(monthStart, monthEnd)
            .first()
            
        val endTime = System.currentTimeMillis()
        
        // 验证结果
        assertEquals(1000, result.size)
        assertTrue(endTime - startTime < 1000) // 确保查询时间在1秒内
    }

    @Test
    fun testConcurrentTransactions() = runTest {
        val currentMonth = YearMonth.now()
        
        // 模拟并发插入
        val jobs = List(100) { index ->
            async {
                val transaction = createTransactionEntity(
                    amount = 100.0 + index,
                    type = TransactionType.INCOME,
                    yearMonth = currentMonth
                )
                transactionDao.insert(transaction)
            }
        }
        
        // 等待所有插入完成
        jobs.awaitAll()

        // 验证数据一致性
        val monthStart = currentMonth.atDay(1).atStartOfDay()
        val monthEnd = currentMonth.atEndOfMonth().atTime(23, 59, 59)
        
        val result = repository
            .getTransactionsByDateRange(monthStart, monthEnd)
            .first()
            
        assertEquals(100, result.size)
        assertTrue(result.all { it.type == TransactionType.INCOME })
    }

    @Test
    fun testTransactionRollback() = runTest {
        val currentMonth = YearMonth.now()
        
        try {
            database.runInTransaction {
                // 正常插入
                transactionDao.insert(
                    createTransactionEntity(1000.0, TransactionType.INCOME, currentMonth)
                )
                
                // 触发异常
                throw IllegalStateException("测试回滚")
            }
        } catch (e: IllegalStateException) {
            // 预期的异常
        }

        // 验证数据已回滚
        val monthStart = currentMonth.atDay(1).atStartOfDay()
        val monthEnd = currentMonth.atEndOfMonth().atTime(23, 59, 59)
        
        val result = repository
            .getTransactionsByDateRange(monthStart, monthEnd)
            .first()
            
        assertTrue(result.isEmpty())
    }

    private fun createTransactionEntity(
        amount: Double,
        type: TransactionType,
        yearMonth: YearMonth
    ): TransactionEntity {
        return TransactionEntity(
            id = 0, // Room会自动生成ID
            amount = amount,
            type = type,
            categoryId = 1L,
            accountId = 1L,
            date = LocalDateTime.of(
                yearMonth.year,
                yearMonth.month,
                1,
                0,
                0
            ),
            note = "Test transaction"
        )
    }
} 
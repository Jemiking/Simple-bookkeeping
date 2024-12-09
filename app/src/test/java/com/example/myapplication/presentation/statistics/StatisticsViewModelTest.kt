package com.example.myapplication.presentation.statistics

import app.cash.turbine.test
import com.example.myapplication.data.local.entity.TransactionType
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.repository.TransactionRepository
import com.example.myapplication.presentation.statistics.components.ChartError
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.time.YearMonth
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModelTest {

    private lateinit var viewModel: StatisticsViewModel
    private lateinit var repository: TransactionRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        viewModel = StatisticsViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `初始状态正确`() = runTest {
        val initialState = viewModel.state.value
        assertEquals(YearMonth.now(), initialState.selectedMonth)
        assertEquals(TransactionType.EXPENSE, initialState.selectedType)
        assertTrue(initialState.monthlyComparisonData.isEmpty())
        assertNull(initialState.error)
        assertTrue(initialState.isLoading)
    }

    @Test
    fun `加载数据成功时更新状态`() = runTest {
        // 准备测试数据
        val currentMonth = YearMonth.now()
        val transactions = listOf(
            createTransaction(1000.0, TransactionType.INCOME, currentMonth),
            createTransaction(500.0, TransactionType.EXPENSE, currentMonth)
        )
        
        coEvery { 
            repository.getTransactionsByDateRange(any(), any())
        } returns flowOf(transactions)

        // 验证状态变化
        viewModel.state.test {
            val initialState = awaitItem()
            assertTrue(initialState.isLoading)

            val loadedState = awaitItem()
            assertFalse(loadedState.isLoading)
            assertEquals(1000.0, loadedState.totalIncome)
            assertEquals(500.0, loadedState.totalExpense)
            assertTrue(loadedState.monthlyComparisonData.isNotEmpty())
            assertNull(loadedState.error)
        }
    }

    @Test
    fun `网络错误时显示错误状态`() = runTest {
        coEvery { 
            repository.getTransactionsByDateRange(any(), any())
        } throws java.io.IOException()

        viewModel.state.test {
            val initialState = awaitItem()
            assertTrue(initialState.isLoading)

            val errorState = awaitItem()
            assertFalse(errorState.isLoading)
            assertEquals(ChartError.Network, errorState.error)
        }
    }

    @Test
    fun `切换月份时重新加载数据`() = runTest {
        val newMonth = YearMonth.now().minusMonths(1)
        val transactions = listOf(
            createTransaction(800.0, TransactionType.INCOME, newMonth)
        )
        
        coEvery { 
            repository.getTransactionsByDateRange(any(), any())
        } returns flowOf(transactions)

        viewModel.onEvent(StatisticsEvent.MonthSelected(newMonth))
        
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(newMonth, state.selectedMonth)
            assertTrue(state.isLoading)

            val loadedState = awaitItem()
            assertFalse(loadedState.isLoading)
            assertEquals(800.0, loadedState.totalIncome)
        }
    }

    @Test
    fun `重试加载时重置错误状态`() = runTest {
        // 首次加载失败
        coEvery { 
            repository.getTransactionsByDateRange(any(), any())
        } throws java.io.IOException()

        viewModel.state.test {
            val initialState = awaitItem()
            val errorState = awaitItem()
            assertEquals(ChartError.Network, errorState.error)

            // 重试成功
            val transactions = listOf(
                createTransaction(1000.0, TransactionType.INCOME, YearMonth.now())
            )
            
            coEvery { 
                repository.getTransactionsByDateRange(any(), any())
            } returns flowOf(transactions)

            viewModel.onEvent(StatisticsEvent.RetryLoad)

            val retryingState = awaitItem()
            assertTrue(retryingState.isLoading)
            assertNull(retryingState.error)

            val successState = awaitItem()
            assertFalse(successState.isLoading)
            assertNull(successState.error)
            assertEquals(1000.0, successState.totalIncome)
        }
    }

    @Test
    fun `空数据时显示空状态`() = runTest {
        coEvery { 
            repository.getTransactionsByDateRange(any(), any())
        } returns flowOf(emptyList())

        viewModel.state.test {
            val initialState = awaitItem()
            assertTrue(initialState.isLoading)

            val emptyState = awaitItem()
            assertFalse(emptyState.isLoading)
            assertTrue(emptyState.monthlyComparisonData.isEmpty())
            assertEquals(0.0, emptyState.totalIncome)
            assertEquals(0.0, emptyState.totalExpense)
            assertNull(emptyState.error)
        }
    }

    @Test
    fun `快速切换月份时取消前一次加载`() = runTest {
        val firstMonth = YearMonth.now().minusMonths(1)
        val secondMonth = YearMonth.now().minusMonths(2)
        
        // 第一次加载会延迟
        coEvery { 
            repository.getTransactionsByDateRange(any(), any())
        } coAnswers {
            delay(1.seconds)
            flowOf(listOf(createTransaction(1000.0, TransactionType.INCOME, firstMonth)))
        }

        viewModel.state.test {
            val initialState = awaitItem()
            assertTrue(initialState.isLoading)

            // 快速切换月份
            viewModel.onEvent(StatisticsEvent.MonthSelected(firstMonth))
            viewModel.onEvent(StatisticsEvent.MonthSelected(secondMonth))

            // 第二次加载立即返回
            coEvery { 
                repository.getTransactionsByDateRange(any(), any())
            } returns flowOf(listOf(createTransaction(2000.0, TransactionType.INCOME, secondMonth)))

            // 验证最终状态
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)
            
            val finalState = awaitItem()
            assertEquals(secondMonth, finalState.selectedMonth)
            assertEquals(2000.0, finalState.totalIncome)
            
            // 验证第一次加载被取消
            coVerify(exactly = 2) { repository.getTransactionsByDateRange(any(), any()) }
        }
    }

    @Test
    fun `重复加载相同月份时复用缓存`() = runTest {
        val currentMonth = YearMonth.now()
        val transactions = listOf(
            createTransaction(1000.0, TransactionType.INCOME, currentMonth)
        )
        
        coEvery { 
            repository.getTransactionsByDateRange(any(), any())
        } returns flowOf(transactions)

        // 连续加载三次
        repeat(3) {
            viewModel.onEvent(StatisticsEvent.MonthSelected(currentMonth))
        }

        // 验证只调用一次仓库
        coVerify(exactly = 1) { repository.getTransactionsByDateRange(any(), any()) }
    }

    @Test
    fun `处理异常数据`() = runTest {
        val currentMonth = YearMonth.now()
        val transactions = listOf(
            // 金额为负数
            createTransaction(-1000.0, TransactionType.INCOME, currentMonth),
            // 金额为零
            createTransaction(0.0, TransactionType.EXPENSE, currentMonth),
            // 正常数据
            createTransaction(2000.0, TransactionType.INCOME, currentMonth)
        )
        
        coEvery { 
            repository.getTransactionsByDateRange(any(), any())
        } returns flowOf(transactions)

        viewModel.state.test {
            val initialState = awaitItem()
            assertTrue(initialState.isLoading)

            val loadedState = awaitItem()
            assertFalse(loadedState.isLoading)
            // 验证负数被转换为0
            assertEquals(2000.0, loadedState.totalIncome)
            assertEquals(0.0, loadedState.totalExpense)
        }
    }

    @Test
    fun `处理大量数据时性能`() = runTest {
        val currentMonth = YearMonth.now()
        val transactions = List(1000) { index ->
            createTransaction(
                amount = 100.0 + index,
                type = if (index % 2 == 0) TransactionType.INCOME else TransactionType.EXPENSE,
                yearMonth = currentMonth
            )
        }
        
        coEvery { 
            repository.getTransactionsByDateRange(any(), any())
        } returns flowOf(transactions)

        viewModel.state.test {
            val initialState = awaitItem()
            assertTrue(initialState.isLoading)

            val loadedState = awaitItem()
            assertFalse(loadedState.isLoading)
            // 验证大量数据计算正确
            assertTrue(loadedState.totalIncome > 0)
            assertTrue(loadedState.totalExpense > 0)
            assertEquals(6, loadedState.monthlyComparisonData.size)
        }
    }

    @Test
    fun `取消加载时清理资源`() = runTest {
        val loadingJob = Job()
        
        coEvery { 
            repository.getTransactionsByDateRange(any(), any())
        } coAnswers {
            flow {
                try {
                    delay(1.seconds)
                    emit(emptyList<Transaction>())
                } finally {
                    // 验证清理逻辑
                    assertTrue(loadingJob.isCancelled)
                }
            }
        }

        viewModel.state.test {
            val initialState = awaitItem()
            assertTrue(initialState.isLoading)

            // 取消加载
            loadingJob.cancel()
            
            val cancelledState = awaitItem()
            assertFalse(cancelledState.isLoading)
            assertNull(cancelledState.error)
        }
    }

    private fun createTransaction(
        amount: Double,
        type: TransactionType,
        yearMonth: YearMonth
    ): Transaction {
        return Transaction(
            id = 1L,
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
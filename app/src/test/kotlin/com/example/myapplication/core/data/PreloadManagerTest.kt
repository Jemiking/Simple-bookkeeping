package com.example.myapplication.core.data

import android.content.Context
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.dao.AccountDao
import com.example.myapplication.data.local.dao.CategoryDao
import com.example.myapplication.data.local.dao.TransactionDao
import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.model.Transaction
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class PreloadManagerTest {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var database: AppDatabase

    @MockK
    private lateinit var transactionDao: TransactionDao

    @MockK
    private lateinit var accountDao: AccountDao

    @MockK
    private lateinit var categoryDao: CategoryDao

    private lateinit var preloadManager: PreloadManager
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        // Mock DAO setup
        every { database.transactionDao() } returns transactionDao
        every { database.accountDao() } returns accountDao
        every { database.categoryDao() } returns categoryDao

        // Mock database executor
        every { database.queryExecutor } returns mockk(relaxed = true)

        preloadManager = PreloadManager(context, database)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `test preload success`() = runTest {
        // Mock data
        val transactions = listOf(mockk<Transaction>())
        val accounts = listOf(mockk<Account>())
        val categories = listOf(mockk<Category>())

        // Setup mock responses
        coEvery { 
            transactionDao.getTransactionsByDateRange(any(), any()) 
        } returns flowOf(transactions)
        coEvery { accountDao.getAllAccounts() } returns flowOf(accounts)
        coEvery { categoryDao.getAllCategories() } returns flowOf(categories)

        // Start preloading
        preloadManager.startPreloading()
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify state transitions
        val states = mutableListOf<PreloadState>()
        preloadManager.preloadState.collect { states.add(it) }

        assertTrue(states.any { it is PreloadState.Loading })
        assertTrue(states.last() is PreloadState.Completed)

        // Verify cache content
        assertEquals(transactions, preloadManager.getTransactionCache().value)
        assertEquals(accounts, preloadManager.getAccountCache().value)
        assertEquals(categories, preloadManager.getCategoryCache().value)
    }

    @Test
    fun `test preload error handling`() = runTest {
        // Setup mock error
        val error = RuntimeException("Test error")
        coEvery { 
            transactionDao.getTransactionsByDateRange(any(), any()) 
        } throws error

        // Start preloading
        preloadManager.startPreloading()
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify error state
        val states = mutableListOf<PreloadState>()
        preloadManager.preloadState.collect { states.add(it) }

        assertTrue(states.last() is PreloadState.Error)
        assertEquals(
            "加载交易数据失败",
            (states.last() as PreloadState.Error).message
        )
    }

    @Test
    fun `test cache validity`() = runTest {
        assertFalse(preloadManager.isCacheValid())

        // Mock successful preload
        coEvery { 
            transactionDao.getTransactionsByDateRange(any(), any()) 
        } returns flowOf(emptyList())
        coEvery { accountDao.getAllAccounts() } returns flowOf(emptyList())
        coEvery { categoryDao.getAllCategories() } returns flowOf(emptyList())

        preloadManager.startPreloading()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(preloadManager.isCacheValid())
    }

    @Test
    fun `test cache invalidation`() = runTest {
        // Setup initial cache state
        coEvery { 
            transactionDao.getTransactionsByDateRange(any(), any()) 
        } returns flowOf(listOf(mockk()))
        
        preloadManager.startPreloading()
        testDispatcher.scheduler.advanceUntilIdle()

        // Invalidate cache
        preloadManager.invalidateCache()
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify cache is cleared
        assertTrue(preloadManager.getTransactionCache().value.isEmpty())
        assertFalse(preloadManager.isCacheValid())
    }

    @Test
    fun `test performance metrics`() = runTest {
        // Setup mock data
        coEvery { 
            transactionDao.getTransactionsByDateRange(any(), any()) 
        } returns flowOf(emptyList())
        coEvery { accountDao.getAllAccounts() } returns flowOf(emptyList())
        coEvery { categoryDao.getAllCategories() } returns flowOf(emptyList())

        // Perform some operations
        preloadManager.startPreloading()
        testDispatcher.scheduler.advanceUntilIdle()

        preloadManager.getTransactionCache() // Cache hit
        preloadManager.getTransactionCache() // Cache hit

        // Get metrics
        val metrics = preloadManager.getPerformanceMetrics()

        assertTrue(metrics.containsKey("loadingTimes"))
        assertTrue(metrics.containsKey("cacheHits"))
        assertTrue(metrics.containsKey("cacheMisses"))
        assertTrue(metrics.containsKey("hitRate"))

        assertEquals(2, metrics["cacheHits"])
        assertTrue((metrics["hitRate"] as Double) > 0.0)
    }
} 
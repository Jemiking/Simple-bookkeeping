package com.example.myapplication.presentation.statistics

import android.os.SystemClock
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myapplication.MainActivity
import com.example.myapplication.data.local.entity.TransactionType
import com.example.myapplication.domain.repository.TransactionRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.YearMonth
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class StatisticsPerformanceTest {

    @get:Rule(order = 0)
    val benchmarkRule = BenchmarkRule()

    @get:Rule(order = 1)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var repository: TransactionRepository

    @Before
    fun setup() {
        hiltRule.inject()
        // 清理测试数据
        runBlocking {
            repository.deleteAllTransactions()
        }
    }

    @Test
    fun testChartRenderingPerformance() {
        // 准备大量测试数据
        val dataSize = listOf(100, 500, 1000, 5000)
        
        dataSize.forEach { size ->
            // 添加测试数据
            runBlocking {
                repeat(size) { index ->
                    repository.insertTransaction(
                        createTestTransaction(
                            amount = 100.0 + index,
                            type = if (index % 2 == 0) TransactionType.INCOME else TransactionType.EXPENSE,
                            yearMonth = YearMonth.now()
                        )
                    )
                }
            }

            benchmarkRule.measureRepeated {
                composeTestRule.apply {
                    // 测量图表渲染时间
                    val renderTime = measureTimeMillis {
                        onNodeWithTag("statistics_chart").assertExists()
                        waitForIdle()
                    }

                    // 记录性能数据
                    androidx.benchmark.Stats.Builder()
                        .setName("Chart Rendering Time ($size items)")
                        .setValue(renderTime)
                        .build()
                }
            }

            // 清理数据
            runBlocking {
                repository.deleteAllTransactions()
            }
        }
    }

    @Test
    fun testChartInteractionPerformance() {
        // 准备测试数据
        runBlocking {
            repeat(1000) { index ->
                repository.insertTransaction(
                    createTestTransaction(
                        amount = 100.0 + index,
                        type = if (index % 2 == 0) TransactionType.INCOME else TransactionType.EXPENSE,
                        yearMonth = YearMonth.now()
                    )
                )
            }
        }

        composeTestRule.apply {
            // 等待图表加载
            onNodeWithTag("statistics_chart").assertExists()
            waitForIdle()

            // 测试缩放性能
            benchmarkRule.measureRepeated {
                val zoomTime = measureTimeMillis {
                    onNodeWithTag("statistics_chart").performTouchInput {
                        pinch(
                            start1 = centerX - 100f to centerY,
                            end1 = centerX - 50f to centerY,
                            start2 = centerX + 100f to centerY,
                            end2 = centerX + 50f to centerY
                        )
                    }
                    waitForIdle()
                }

                androidx.benchmark.Stats.Builder()
                    .setName("Chart Zoom Response Time")
                    .setValue(zoomTime)
                    .build()
            }

            // 测试滑动性能
            benchmarkRule.measureRepeated {
                val scrollTime = measureTimeMillis {
                    onNodeWithTag("statistics_chart").performTouchInput {
                        swipeLeft()
                    }
                    waitForIdle()
                }

                androidx.benchmark.Stats.Builder()
                    .setName("Chart Scroll Response Time")
                    .setValue(scrollTime)
                    .build()
            }
        }
    }

    @Test
    fun testMemoryUsage() {
        val runtime = Runtime.getRuntime()
        val memoryUsage = mutableListOf<Long>()

        // 准备测试数据
        runBlocking {
            repeat(5000) { index ->
                repository.insertTransaction(
                    createTestTransaction(
                        amount = 100.0 + index,
                        type = if (index % 2 == 0) TransactionType.INCOME else TransactionType.EXPENSE,
                        yearMonth = YearMonth.now()
                    )
                )
            }
        }

        composeTestRule.apply {
            // 记录初始内存
            memoryUsage.add(runtime.totalMemory() - runtime.freeMemory())

            // 加载图表
            onNodeWithTag("statistics_chart").assertExists()
            waitForIdle()
            
            // 记录图表渲染后内存
            memoryUsage.add(runtime.totalMemory() - runtime.freeMemory())

            // 执行交互操作
            repeat(10) {
                onNodeWithTag("statistics_chart").performTouchInput {
                    swipeLeft()
                    waitForIdle()
                }
                // 记录每次交互后的内存
                memoryUsage.add(runtime.totalMemory() - runtime.freeMemory())
            }

            // 分析内存使用
            val maxMemory = memoryUsage.maxOrNull() ?: 0
            val avgMemory = memoryUsage.average()

            androidx.benchmark.Stats.Builder()
                .setName("Memory Usage")
                .setValue(maxMemory)
                .build()

            androidx.benchmark.Stats.Builder()
                .setName("Average Memory Usage")
                .setValue(avgMemory.toLong())
                .build()
        }
    }

    @Test
    fun testFrameRate() {
        composeTestRule.apply {
            // 准备测试数据
            runBlocking {
                repeat(1000) { index ->
                    repository.insertTransaction(
                        createTestTransaction(
                            amount = 100.0 + index,
                            type = if (index % 2 == 0) TransactionType.INCOME else TransactionType.EXPENSE,
                            yearMonth = YearMonth.now()
                        )
                    )
                }
            }

            // 等待图表加载
            onNodeWithTag("statistics_chart").assertExists()
            waitForIdle()

            // 测量帧率
            val frameStats = mutableListOf<Long>()
            val testDuration = 5000L // 5秒
            val startTime = SystemClock.elapsedRealtime()

            while (SystemClock.elapsedRealtime() - startTime < testDuration) {
                val frameStart = SystemClock.elapsedRealtime()
                
                // 执行动画或交互
                onNodeWithTag("statistics_chart").performTouchInput {
                    swipeLeft(durationMillis = 100)
                }
                
                val frameDuration = SystemClock.elapsedRealtime() - frameStart
                frameStats.add(frameDuration)
            }

            // 计算平均帧率
            val avgFrameDuration = frameStats.average()
            val fps = 1000.0 / avgFrameDuration

            androidx.benchmark.Stats.Builder()
                .setName("Average FPS")
                .setValue(fps.toLong())
                .build()
        }
    }
} 
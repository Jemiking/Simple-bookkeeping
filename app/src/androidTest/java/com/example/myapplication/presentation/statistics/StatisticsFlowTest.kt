package com.example.myapplication.presentation.statistics

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
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
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class StatisticsFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var repository: TransactionRepository

    @Before
    fun setup() {
        hiltRule.inject()
        // ���理测试数据
        runBlocking {
            repository.deleteAllTransactions()
        }
    }

    @Test
    fun testCompleteStatisticsFlow() {
        composeTestRule.apply {
            // 1. 初始状态验证
            verifyInitialState()

            // 2. 添加测试数据
            addTestData()

            // 3. 执行统计分析流程
            performStatisticsAnalysis()

            // 4. 验证分析结果
            verifyAnalysisResults()

            // 5. 测试数据导出
            testDataExport()
        }
    }

    private fun ComposeTestRule.verifyInitialState() {
        // 验证初始界面
        onNodeWithText("统计").assertIsDisplayed()
        onNodeWithText("支出").assertIsSelected()
        onNodeWithTag("statistics_chart").assertIsDisplayed()
        
        // 验证空状态提示
        onNodeWithText("暂无收支数据").assertIsDisplayed()
    }

    private fun addTestData() = runBlocking {
        // 添加本月数据
        val currentMonth = YearMonth.now()
        repository.insertTransaction(createTestTransaction(1000.0, TransactionType.INCOME, currentMonth))
        repository.insertTransaction(createTestTransaction(500.0, TransactionType.EXPENSE, currentMonth))
        
        // 添加上月数据
        val lastMonth = currentMonth.minusMonths(1)
        repository.insertTransaction(createTestTransaction(2000.0, TransactionType.INCOME, lastMonth))
        repository.insertTransaction(createTestTransaction(1500.0, TransactionType.EXPENSE, lastMonth))
    }

    private fun ComposeTestRule.performStatisticsAnalysis() {
        // 1. 切换月份查看数据
        onNodeWithTag("month_picker").performClick()
        waitForIdle()
        
        val lastMonth = YearMonth.now().minusMonths(1)
        val lastMonthText = lastMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月"))
        onNodeWithText(lastMonthText).performClick()
        waitForIdle()
        
        // 2. 切换收支类型
        onNodeWithText("收入").performClick()
        waitForIdle()
        
        // 3. 切换图表类型
        onNodeWithTag("chart_type_toggle").performClick()
        waitForIdle()
        
        // 4. 查看详细数据
        onNodeWithTag("statistics_chart").performTouchInput {
            click(centerX, centerY)
        }
        waitForIdle()
        
        // 5. 应用筛选
        onNodeWithTag("filter_button").performClick()
        waitForIdle()
        onNodeWithText("确定").performClick()
        waitForIdle()
    }

    private fun ComposeTestRule.verifyAnalysisResults() {
        // 1. 验证总金额
        onNodeWithTag("total_income").assertTextContains("3000")  // 1000 + 2000
        onNodeWithTag("total_expense").assertTextContains("2000") // 500 + 1500
        
        // 2. 验证图表数据
        onNodeWithTag("statistics_chart").assertExists()
        
        // 3. 验证月度对比
        onNodeWithTag("monthly_comparison").assertExists()
        
        // 4. 验证分类统计
        onNodeWithTag("category_statistics").assertExists()
    }

    private fun ComposeTestRule.testDataExport() {
        // 1. 打开导出选项
        onNodeWithTag("more_options").performClick()
        onNodeWithText("导出数据").performClick()
        
        // 2. 选择导出范围
        onNodeWithText("本月").performClick()
        
        // 3. 确认导出
        onNodeWithText("确定").performClick()
        
        // 4. 验证导出成功提示
        onNodeWithText("导出成功").assertIsDisplayed()
    }

    @Test
    fun testFilterAndSort() {
        composeTestRule.apply {
            // 1. 添加测试数据
            addTestData()
            
            // 2. 打开筛选
            onNodeWithTag("filter_button").performClick()
            
            // 3. 设置日期范围
            onNodeWithText("选择日期范围").performClick()
            onNodeWithText("本月").performClick()
            
            // 4. 选择分类
            onNodeWithText("选择分类").performClick()
            onNodeWithText("全部分类").performClick()
            
            // 5. 应用筛选
            onNodeWithText("确定").performClick()
            
            // 6. 验证筛选结果
            waitForIdle()
            onNodeWithTag("total_income").assertTextContains("1000")
            onNodeWithTag("total_expense").assertTextContains("500")
        }
    }

    @Test
    fun testChartInteractions() {
        composeTestRule.apply {
            // 1. 添加测试数据
            addTestData()
            
            // 2. 等待图表加载
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithTag("loading_indicator").fetchSemanticsNodes().isEmpty()
            }
            
            // 3. 测试图表交互
            onNodeWithTag("statistics_chart").performTouchInput {
                // 点击
                click(centerX, centerY)
                waitForIdle()
                
                // 缩放
                pinch(
                    start1 = centerX - 100f to centerY,
                    end1 = centerX - 50f to centerY,
                    start2 = centerX + 100f to centerY,
                    end2 = centerX + 50f to centerY
                )
                waitForIdle()
                
                // 滑动
                swipeLeft()
                waitForIdle()
            }
            
            // 4. 验证交互响应
            onNodeWithTag("chart_details").assertIsDisplayed()
        }
    }

    @Test
    fun testDataRefresh() {
        composeTestRule.apply {
            // 1. 添加初始数据
            addTestData()
            waitForIdle()
            
            // 2. 记录初始状态
            val initialIncome = onNodeWithTag("total_income").fetchSemanticsNode().text
            
            // 3. 添加新数据
            runBlocking {
                repository.insertTransaction(
                    createTestTransaction(500.0, TransactionType.INCOME, YearMonth.now())
                )
            }
            
            // 4. 下拉刷新
            onNodeWithTag("statistics_content").performTouchInput {
                swipeDown()
            }
            waitForIdle()
            
            // 5. 验证数据更新
            onNodeWithTag("total_income").assertTextContains("1500") // 1000 + 500
        }
    }
} 
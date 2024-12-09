package com.example.myapplication.presentation.statistics

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.MainActivity
import com.example.myapplication.data.local.entity.TransactionType
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.presentation.statistics.components.ChartError
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class StatisticsScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun testInitialState() {
        composeTestRule.apply {
            // 验证标题显示
            onNodeWithText("统计").assertIsDisplayed()
            
            // 验证默认选中当前月份
            val currentMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy年MM月"))
            onNodeWithText(currentMonth).assertIsDisplayed()
            
            // 验证默认选中支出类型
            onNodeWithText("支出").assertIsSelected()
            
            // 验证图表显示
            onNodeWithTag("statistics_chart").assertIsDisplayed()
        }
    }

    @Test
    fun testMonthSelection() {
        composeTestRule.apply {
            // 点击月份选择器
            onNodeWithTag("month_picker").performClick()
            
            // 选择上个月
            val lastMonth = YearMonth.now().minusMonths(1)
            val lastMonthText = lastMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月"))
            onNodeWithText(lastMonthText).performClick()
            
            // 验证月份更新
            onNodeWithText(lastMonthText).assertIsDisplayed()
            
            // 验证加载状态
            onNodeWithTag("loading_indicator").assertIsDisplayed()
            
            // 等待加载完成
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithTag("loading_indicator").fetchSemanticsNodes().isEmpty()
            }
        }
    }

    @Test
    fun testTypeToggle() {
        composeTestRule.apply {
            // 初始状态为支出
            onNodeWithText("支出").assertIsSelected()
            
            // 切换到收入
            onNodeWithText("收入").performClick()
            onNodeWithText("收入").assertIsSelected()
            
            // 验证图表更新
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithTag("loading_indicator").fetchSemanticsNodes().isEmpty()
            }
            
            // 切换回支出
            onNodeWithText("支出").performClick()
            onNodeWithText("支出").assertIsSelected()
        }
    }

    @Test
    fun testChartTypeToggle() {
        composeTestRule.apply {
            // 默认显示饼图
            onNodeWithTag("pie_chart").assertIsDisplayed()
            
            // 切换到趋势图
            onNodeWithTag("chart_type_toggle").performClick()
            onNodeWithTag("trend_chart").assertIsDisplayed()
            
            // 切换回饼图
            onNodeWithTag("chart_type_toggle").performClick()
            onNodeWithTag("pie_chart").assertIsDisplayed()
        }
    }

    @Test
    fun testEmptyState() {
        // 模拟空数据状态
        composeTestRule.apply {
            onNodeWithText("暂无收支数据").assertIsDisplayed()
            onNodeWithText("开始记录您的第一笔交易").assertIsDisplayed()
        }
    }

    @Test
    fun testErrorState() {
        // 模拟错误状态
        composeTestRule.apply {
            // 网络错误
            onNodeWithText("网络连接失败").assertIsDisplayed()
            onNodeWithText("请检查网络连接后重试").assertIsDisplayed()
            
            // 点击重试
            onNodeWithText("重试").performClick()
            
            // 验证加载状态
            onNodeWithTag("loading_indicator").assertIsDisplayed()
        }
    }

    @Test
    fun testDataUpdate() {
        composeTestRule.apply {
            // 等待数据加载
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithTag("loading_indicator").fetchSemanticsNodes().isEmpty()
            }
            
            // 验证收入金额
            onNodeWithTag("total_income").assertTextContains("¥")
            
            // 验证支出金额
            onNodeWithTag("total_expense").assertTextContains("¥")
            
            // 验证图表数据
            onNodeWithTag("statistics_chart").assertExists()
        }
    }

    @Test
    fun testScreenRotation() {
        composeTestRule.apply {
            // 记录当前状态
            val currentMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy年MM月"))
            
            // 旋转屏幕
            activity.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            
            // 验证状态保持
            onNodeWithText(currentMonth).assertIsDisplayed()
            onNodeWithText("支出").assertIsSelected()
            onNodeWithTag("statistics_chart").assertIsDisplayed()
            
            // 恢复竖屏
            activity.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    @Test
    fun testChartInteraction() {
        composeTestRule.apply {
            // 等待图表加载
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithTag("loading_indicator").fetchSemanticsNodes().isEmpty()
            }
            
            // 点击图表区域
            onNodeWithTag("statistics_chart").performTouchInput {
                click(centerX, centerY)
            }
            
            // 验证详情显示
            onNodeWithTag("chart_details").assertIsDisplayed()
        }
    }
} 
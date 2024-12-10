package com.example.myapplication.presentation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.myapplication.core.data.PreloadManager
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class MainActivityTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var preloadManager: PreloadManager

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun testDataLoadingFlow() {
        // 验证初始加载状态
        composeTestRule.onNodeWithTag("loading_container").assertExists()
        composeTestRule.onNodeWithTag("progress_indicator").assertExists()

        // 等待加载完成
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            try {
                composeTestRule
                    .onAllNodesWithTag("loading_container")
                    .fetchSemanticsNodes().isEmpty()
            } catch (e: Exception) {
                false
            }
        }

        // 验证主界面是否显示
        composeTestRule.onNodeWithTag("main_screen").assertExists()
    }

    @Test
    fun testErrorHandling() {
        // 强制触发错误
        composeTestRule.runOnUiThread {
            preloadManager.clearCache()
        }

        // 验证错误状态
        composeTestRule.onNodeWithTag("error_icon").assertExists()
        composeTestRule.onNodeWithTag("retry_button").assertExists()

        // 测试重试功能
        composeTestRule.onNodeWithTag("retry_button").performClick()

        // 等待重新加载完成
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            try {
                composeTestRule
                    .onAllNodesWithTag("loading_container")
                    .fetchSemanticsNodes().isEmpty()
            } catch (e: Exception) {
                false
            }
        }

        // 验证主界面是否显示
        composeTestRule.onNodeWithTag("main_screen").assertExists()
    }

    @Test
    fun testLoadingPerformance() {
        val startTime = System.currentTimeMillis()

        // 等待加载完成
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            try {
                composeTestRule
                    .onAllNodesWithTag("loading_container")
                    .fetchSemanticsNodes().isEmpty()
            } catch (e: Exception) {
                false
            }
        }

        val loadTime = System.currentTimeMillis() - startTime

        // 验证加载时间是否在可接受范围内 (5秒)
        assert(loadTime < 5000) {
            "加载时间过长: $loadTime ms"
        }

        // 检查性能指标
        composeTestRule.runOnUiThread {
            val metrics = preloadManager.getPerformanceMetrics()
            assert(metrics["hitRate"] as Double > 0.8) {
                "缓存命中率过低: ${metrics["hitRate"]}"
            }
        }
    }
} 
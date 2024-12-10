package com.example.myapplication.presentation.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.myapplication.core.data.PreloadState
import com.example.myapplication.presentation.theme.AppTheme
import org.junit.Rule
import org.junit.Test

class LoadingViewTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLoadingState() {
        composeTestRule.setContent {
            AppTheme {
                LoadingView(
                    state = PreloadState.Loading(50)
                )
            }
        }

        // 验证进度指示器和文本是否显示
        composeTestRule.onNodeWithTag("progress_indicator").assertExists()
        composeTestRule.onNodeWithText("正在加载 50%").assertExists()
    }

    @Test
    fun testErrorState() {
        val errorMessage = "测试错误消息"
        composeTestRule.setContent {
            AppTheme {
                LoadingView(
                    state = PreloadState.Error(errorMessage)
                )
            }
        }

        // 验证错��图标、消息和重试按钮是否显示
        composeTestRule.onNodeWithTag("error_icon").assertExists()
        composeTestRule.onNodeWithText(errorMessage).assertExists()
        composeTestRule.onNodeWithText("重试").assertExists()
    }

    @Test
    fun testNotStartedState() {
        composeTestRule.setContent {
            AppTheme {
                LoadingView(
                    state = PreloadState.NotStarted
                )
            }
        }

        // 验证空状态
        composeTestRule.onNodeWithTag("loading_container").assertExists()
        composeTestRule.onNodeWithTag("progress_indicator").assertDoesNotExist()
    }

    @Test
    fun testCompletedState() {
        composeTestRule.setContent {
            AppTheme {
                LoadingView(
                    state = PreloadState.Completed
                )
            }
        }

        // 验证完成状态不显示任何加载UI
        composeTestRule.onNodeWithTag("loading_container").assertExists()
        composeTestRule.onNodeWithTag("progress_indicator").assertDoesNotExist()
    }

    @Test
    fun testLoadingPlaceholder() {
        composeTestRule.setContent {
            AppTheme {
                LoadingPlaceholder()
            }
        }

        // 验证占位符是否显示
        composeTestRule.onNodeWithTag("loading_placeholder").assertExists()
    }

    @Test
    fun testLoadingShimmer() {
        composeTestRule.setContent {
            AppTheme {
                LoadingShimmer()
            }
        }

        // 验证骨架屏是否显示
        composeTestRule.onNodeWithTag("loading_shimmer").assertExists()
    }
} 
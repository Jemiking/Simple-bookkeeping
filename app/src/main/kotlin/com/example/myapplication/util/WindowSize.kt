package com.example.myapplication.util

import android.app.Activity
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 窗口大小类型
 */
enum class WindowSizeClass {
    COMPACT,    // 紧凑型(手机竖屏)
    MEDIUM,     // 中等型(手机横屏/小平板)
    EXPANDED    // 扩展型(大平板/桌面)
}

/**
 * 窗口大小信息
 */
data class WindowSize(
    val widthSizeClass: WindowSizeClass,
    val heightSizeClass: WindowSizeClass,
    val widthDp: Dp,
    val heightDp: Dp
)

/**
 * 计算窗口大小类型
 */
@Composable
fun Activity.rememberWindowSize(): WindowSize {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    
    // 获取屏幕尺寸(dp)
    val widthDp = with(density) { configuration.screenWidthDp.dp }
    val heightDp = with(density) { configuration.screenHeightDp.dp }
    
    // 计算宽度类型
    val widthSizeClass = when {
        widthDp < 600.dp -> WindowSizeClass.COMPACT
        widthDp < 840.dp -> WindowSizeClass.MEDIUM
        else -> WindowSizeClass.EXPANDED
    }
    
    // 计算高度类型
    val heightSizeClass = when {
        heightDp < 480.dp -> WindowSizeClass.COMPACT
        heightDp < 900.dp -> WindowSizeClass.MEDIUM
        else -> WindowSizeClass.EXPANDED
    }
    
    return WindowSize(widthSizeClass, heightSizeClass, widthDp, heightDp)
}

/**
 * 根据窗口大小调整布局
 */
@Composable
fun rememberWindowSizeLayout(
    windowSize: WindowSize
): WindowSizeLayout {
    return remember(windowSize) {
        WindowSizeLayout(
            // 导航栏宽度
            navigationRailWidth = when (windowSize.widthSizeClass) {
                WindowSizeClass.COMPACT -> 0.dp
                WindowSizeClass.MEDIUM -> 72.dp
                WindowSizeClass.EXPANDED -> 96.dp
            },
            
            // 内容最大宽度
            contentMaxWidth = when (windowSize.widthSizeClass) {
                WindowSizeClass.COMPACT -> windowSize.widthDp
                WindowSizeClass.MEDIUM -> 840.dp
                WindowSizeClass.EXPANDED -> 1040.dp
            },
            
            // 是否显示底部导航栏
            showBottomBar = windowSize.widthSizeClass == WindowSizeClass.COMPACT,
            
            // 是否显示侧边导航栏
            showNavigationRail = windowSize.widthSizeClass != WindowSizeClass.COMPACT,
            
            // 是否使用双栏布局
            useTwoPane = windowSize.widthSizeClass == WindowSizeClass.EXPANDED,
            
            // 内容水平padding
            contentHorizontalPadding = when (windowSize.widthSizeClass) {
                WindowSizeClass.COMPACT -> 16.dp
                WindowSizeClass.MEDIUM -> 24.dp
                WindowSizeClass.EXPANDED -> 32.dp
            }
        )
    }
}

/**
 * 布局配置
 */
data class WindowSizeLayout(
    val navigationRailWidth: Dp,
    val contentMaxWidth: Dp,
    val showBottomBar: Boolean,
    val showNavigationRail: Boolean,
    val useTwoPane: Boolean,
    val contentHorizontalPadding: Dp
) 
package com.example.myapplication.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
) {
    companion object {
        val items = listOf(
            BottomNavItem(
                screen = Screen.Home,
                icon = Icons.Default.Home,
                label = "首页"
            ),
            BottomNavItem(
                screen = Screen.Statistics,
                icon = Icons.Default.PieChart,
                label = "统计"
            ),
            BottomNavItem(
                screen = Screen.AddTransaction,
                icon = Icons.Default.Add,
                label = "记账"
            ),
            BottomNavItem(
                screen = Screen.Budget,
                icon = Icons.Default.AccountBalance,
                label = "预算"
            ),
            BottomNavItem(
                screen = Screen.Settings,
                icon = Icons.Default.Settings,
                label = "设置"
            )
        )
    }
} 
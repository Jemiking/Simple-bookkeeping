package com.example.myapplication.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController

@Composable
fun BottomNavBar(
    navController: NavHostController,
    currentDestination: NavDestination?,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        NavigationBar(
            modifier = modifier,
            tonalElevation = 8.dp
        ) {
            BottomNavItem.items.forEach { item ->
                val selected = currentDestination?.hierarchy?.any {
                    it.route == item.screen.route
                } == true

                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    selected = selected,
                    onClick = {
                        if (!selected) {
                            navController.navigate(item.screen.route) {
                                // 弹出到起始目的地，避免堆栈积累
                                popUpTo(Screen.Home.route) {
                                    saveState = true
                                }
                                // 避免重复导航到同一目的地
                                launchSingleTop = true
                                // 恢复状态
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    }
} 
package com.example.myapplication.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController

@Composable
fun NavigationRail(
    navController: NavHostController,
    currentDestination: NavDestination?,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
        ) {
            BottomNavItem.items.forEach { item ->
                val selected = currentDestination?.hierarchy?.any {
                    it.route == item.screen.route
                } == true

                NavigationRailItem(
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
                    },
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
                    alwaysShowLabel = false
                )
            }
        }
    }
} 
package com.example.myapplication.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.presentation.main.MainScreen
import com.example.myapplication.presentation.transaction.TransactionScreen
import com.example.myapplication.presentation.statistics.StatisticsScreen
import com.example.myapplication.presentation.settings.SettingsScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String = Screen.Main.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(
            route = Screen.Main.route,
            enterTransition = {
                fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300))
            }
        ) {
            MainScreen(
                onNavigateToTransaction = { 
                    navController.navigate(Screen.Transaction.route)
                },
                onNavigateToStatistics = {
                    navController.navigate(Screen.Statistics.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.Transaction.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(300)
                )
            }
        ) {
            TransactionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Statistics.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(300)
                )
            }
        ) {
            StatisticsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Settings.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(300)
                )
            }
        ) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Transaction : Screen("transaction")
    object Statistics : Screen("statistics")
    object Settings : Screen("settings")
} 
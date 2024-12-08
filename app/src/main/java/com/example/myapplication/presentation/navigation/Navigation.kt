package com.example.myapplication.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.presentation.account.adjust.AccountAdjustScreen
import com.example.myapplication.presentation.account.transfer.AccountTransferScreen
import com.example.myapplication.presentation.budget.BudgetScreen
import com.example.myapplication.presentation.category.CategoryScreen
import com.example.myapplication.presentation.main.MainScreen
import com.example.myapplication.presentation.search.SearchTransactionScreen
import com.example.myapplication.presentation.settings.SettingsScreen
import com.example.myapplication.presentation.statistics.StatisticsScreen
import com.example.myapplication.presentation.transaction.add.AddTransactionScreen
import com.example.myapplication.presentation.transaction.detail.TransactionDetailScreen

@Composable
fun Navigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            MainScreen(navController)
        }
        
        composable(Screen.Statistics.route) {
            StatisticsScreen(navController)
        }
        
        composable(Screen.AddTransaction.route) {
            AddTransactionScreen(navController)
        }
        
        composable(Screen.Budget.route) {
            BudgetScreen(navController)
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(navController)
        }
        
        composable(Screen.Category.route) {
            CategoryScreen(navController)
        }
        
        composable(
            route = Screen.TransactionDetail.route,
            arguments = listOf(
                navArgument("transactionId") {
                    type = NavType.LongType
                }
            )
        ) {
            TransactionDetailScreen(
                navController = navController,
                transactionId = it.arguments?.getLong("transactionId") ?: 0L
            )
        }
        
        composable(Screen.SearchTransaction.route) {
            SearchTransactionScreen(navController)
        }
        
        composable(Screen.AccountTransfer.route) {
            AccountTransferScreen(navController)
        }
        
        composable(Screen.AccountAdjust.route) {
            AccountAdjustScreen(navController)
        }
    }
} 
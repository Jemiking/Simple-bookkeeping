package com.example.myapplication.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.presentation.account.detail.AccountDetailScreen
import com.example.myapplication.presentation.account.edit.AccountEditScreen
import com.example.myapplication.presentation.account.list.AccountListScreen
import com.example.myapplication.presentation.account.transfer.AccountTransferScreen
import com.example.myapplication.presentation.category.detail.CategoryDetailScreen
import com.example.myapplication.presentation.category.list.CategoryListScreen
import com.example.myapplication.presentation.settings.SettingsScreen
import com.example.myapplication.presentation.statistics.StatisticsScreen
import com.example.myapplication.presentation.transaction.batch.BatchEditScreen
import com.example.myapplication.presentation.transaction.batch.BatchOperationScreen
import com.example.myapplication.presentation.transaction.detail.TransactionDetailScreen
import com.example.myapplication.presentation.transaction.edit.TransactionEditScreen
import com.example.myapplication.presentation.transaction.list.TransactionListScreen

sealed class Screen(val route: String) {
    object TransactionList : Screen("transaction_list")
    object TransactionDetail : Screen("transaction_detail/{transactionId}") {
        fun createRoute(transactionId: Long) = "transaction_detail/$transactionId"
    }
    object TransactionEdit : Screen("transaction_edit/{transactionId}") {
        fun createRoute(transactionId: Long) = "transaction_edit/$transactionId"
    }
    object TransactionBatch : Screen("transaction_batch")
    object TransactionBatchEdit : Screen("transaction_batch_edit/{transactionIds}") {
        fun createRoute(transactionIds: List<Long>) = "transaction_batch_edit/${transactionIds.joinToString(",")}"
    }
    object AccountList : Screen("account_list")
    object AccountDetail : Screen("account_detail/{accountId}") {
        fun createRoute(accountId: Long) = "account_detail/$accountId"
    }
    object AccountEdit : Screen("account_edit/{accountId}") {
        fun createRoute(accountId: Long) = "account_edit/$accountId"
    }
    object AccountTransfer : Screen("account_transfer")
    object CategoryList : Screen("category_list")
    object CategoryDetail : Screen("category_detail/{categoryId}") {
        fun createRoute(categoryId: Long) = "category_detail/$categoryId"
    }
    object Statistics : Screen("statistics")
    object Settings : Screen("settings")
}

@Composable
fun Navigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.TransactionList.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        transactionGraph(navController)
        accountGraph(navController)
        categoryGraph(navController)
        statisticsGraph(navController)
        settingsGraph(navController)
    }
} 
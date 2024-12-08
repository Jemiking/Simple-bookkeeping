package com.example.myapplication.presentation.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Statistics : Screen("statistics")
    data object AddTransaction : Screen("add_transaction")
    data object Budget : Screen("budget")
    data object Settings : Screen("settings")
    data object Category : Screen("category")
    data object TransactionDetail : Screen("transaction_detail/{transactionId}") {
        fun createRoute(transactionId: Long) = "transaction_detail/$transactionId"
    }
    data object SearchTransaction : Screen("search_transaction")
    data object AccountTransfer : Screen("account_transfer")
    data object AccountAdjust : Screen("account_adjust")
    
    companion object {
        fun fromRoute(route: String?): Screen {
            return when (route?.substringBefore("/")) {
                Home.route -> Home
                Statistics.route -> Statistics
                AddTransaction.route -> AddTransaction
                Budget.route -> Budget
                Settings.route -> Settings
                Category.route -> Category
                "transaction_detail" -> TransactionDetail
                SearchTransaction.route -> SearchTransaction
                AccountTransfer.route -> AccountTransfer
                AccountAdjust.route -> AccountAdjust
                else -> Home
            }
        }
    }
} 
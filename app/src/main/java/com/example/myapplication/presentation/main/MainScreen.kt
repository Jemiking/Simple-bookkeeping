package com.example.myapplication.presentation.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.myapplication.presentation.main.components.*
import com.example.myapplication.presentation.navigation.Screen
import java.time.YearMonth

@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的账本") },
                actions = {
                    // 搜索按钮
                    IconButton(onClick = {
                        navController.navigate(Screen.SearchTransaction.route)
                    }) {
                        Icon(Icons.Default.Search, "搜索")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.AddTransaction.route)
                }
            ) {
                Icon(Icons.Default.Add, "添加交易")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 月份选择器
            MonthPicker(
                selectedMonth = state.selectedMonth,
                onMonthSelected = { month ->
                    viewModel.onEvent(MainEvent.SelectMonth(month))
                },
                modifier = Modifier.padding(16.dp)
            )

            // 账户列表
            AccountList(
                accounts = state.accounts,
                selectedAccountId = state.selectedAccountId,
                onAccountSelected = { account ->
                    viewModel.onEvent(MainEvent.SelectAccount(account.id))
                },
                onAccountLongClick = { account ->
                    // 显示账户操作菜单
                    showAccountMenu(
                        account = account,
                        onTransfer = {
                            navController.navigate(Screen.AccountTransfer.route)
                        },
                        onAdjustBalance = {
                            navController.navigate(Screen.AccountAdjust.route)
                        }
                    )
                }
            )

            // 预算进度
            if (state.monthlyBudget != null) {
                BudgetProgressBar(
                    currentAmount = state.monthlyExpense,
                    budgetAmount = state.monthlyBudget.amount,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // 交易列表
            TransactionList(
                transactions = state.recentTransactions,
                onTransactionClick = { transaction ->
                    navController.navigate(
                        Screen.TransactionDetail.createRoute(transaction.id)
                    )
                }
            )
        }
    }
}

@Composable
private fun showAccountMenu(
    account: Account,
    onTransfer: () -> Unit,
    onAdjustBalance: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box {
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("转账") },
                onClick = {
                    showMenu = false
                    onTransfer()
                },
                leadingIcon = {
                    Icon(Icons.Default.SwapHoriz, "转账")
                }
            )
            DropdownMenuItem(
                text = { Text("调整余额") },
                onClick = {
                    showMenu = false
                    onAdjustBalance()
                },
                leadingIcon = {
                    Icon(Icons.Default.Edit, "调整余额")
                }
            )
        }
    }
}

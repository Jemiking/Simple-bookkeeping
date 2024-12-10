package com.example.myapplication.presentation.budget

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.presentation.budget.components.BudgetDialog
import com.example.myapplication.presentation.budget.components.BudgetList
import com.example.myapplication.presentation.budget.components.DeleteBudgetDialog
import com.example.myapplication.presentation.main.components.MonthPicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.error) {
        state.error?.let { error ->
            // TODO: 显示错误���示
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("预算") },
                actions = {
                    IconButton(
                        onClick = { viewModel.onEvent(BudgetEvent.ShowAddDialog) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "添加预算"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 月份选择器
            MonthPicker(
                selectedMonth = state.selectedMonth,
                onMonthSelected = { month ->
                    viewModel.onEvent(BudgetEvent.MonthSelected(month))
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 预算列表
            BudgetList(
                budgets = listOfNotNull(state.overallBudget) + state.categoryBudgets,
                selectedBudgetId = state.selectedBudgetId,
                onBudgetClick = { budget ->
                    viewModel.onEvent(BudgetEvent.BudgetSelected(budget.id))
                },
                onEditClick = { budget ->
                    viewModel.onEvent(BudgetEvent.ShowAddDialog)
                    viewModel.onEvent(BudgetEvent.AmountChanged(budget.amount.toString()))
                    viewModel.onEvent(BudgetEvent.CategorySelected(budget.categoryId))
                    viewModel.onEvent(BudgetEvent.ThresholdChanged(budget.notifyThreshold?.toString() ?: "0.8"))
                    viewModel.onEvent(BudgetEvent.EnabledChanged(budget.isEnabled))
                },
                onDeleteClick = { budget ->
                    viewModel.onEvent(BudgetEvent.BudgetSelected(budget.id))
                    viewModel.onEvent(BudgetEvent.ShowDeleteDialog)
                }
            )

            // 加载指示器
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        // 添加/编辑预算对话框
        if (state.isAddDialogVisible) {
            BudgetDialog(
                budget = state.editingBudget,
                categories = state.categories,
                onAmountChanged = { amount ->
                    viewModel.onEvent(BudgetEvent.AmountChanged(amount))
                },
                onCategorySelected = { categoryId ->
                    viewModel.onEvent(BudgetEvent.CategorySelected(categoryId))
                },
                onThresholdChanged = { threshold ->
                    viewModel.onEvent(BudgetEvent.ThresholdChanged(threshold))
                },
                onEnabledChanged = { enabled ->
                    viewModel.onEvent(BudgetEvent.EnabledChanged(enabled))
                },
                onDismiss = {
                    viewModel.onEvent(BudgetEvent.HideAddDialog)
                },
                onConfirm = {
                    viewModel.onEvent(BudgetEvent.SaveBudget)
                }
            )
        }

        // 删除预算确认对话框
        if (state.isDeleteDialogVisible) {
            val selectedBudget = state.categoryBudgets.find { 
                it.id == state.selectedBudgetId 
            } ?: state.overallBudget
            
            if (selectedBudget != null) {
                DeleteBudgetDialog(
                    budget = selectedBudget,
                    onDismiss = {
                        viewModel.onEvent(BudgetEvent.HideDeleteDialog)
                    },
                    onConfirm = {
                        viewModel.onEvent(BudgetEvent.DeleteBudget)
                    }
                )
            }
        }
    }
} 
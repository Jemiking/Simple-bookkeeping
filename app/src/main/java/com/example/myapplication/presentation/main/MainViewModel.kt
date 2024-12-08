package com.example.myapplication.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.usecase.account.GetAccountsUseCase
import com.example.myapplication.domain.usecase.budget.GetBudgetsUseCase
import com.example.myapplication.domain.usecase.transaction.AddTransactionUseCase
import com.example.myapplication.domain.usecase.transaction.GetTransactionStatsUseCase
import com.example.myapplication.domain.usecase.transaction.GetTransactionsUseCase
import com.example.myapplication.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getAccounts: GetAccountsUseCase,
    private val getTransactions: GetTransactionsUseCase,
    private val getTransactionStats: GetTransactionStatsUseCase,
    private val getBudgets: GetBudgetsUseCase,
    private val addTransaction: AddTransactionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = combine(
        getAccounts(),
        getTransactionStats(),
        getBudgets(),
        getTransactions()
    ) { accounts, stats, budgets, transactions ->
        _state.value.copy(
            accounts = accounts,
            totalBalance = accounts.sumOf { it.balance },
            monthlyIncome = stats.totalIncome,
            monthlyExpense = stats.totalExpense,
            monthlyBalance = stats.balance,
            monthlyBudget = budgets.firstOrNull { it.categoryId == null },
            budgetProgress = calculateBudgetProgress(stats.totalExpense, budgets),
            isOverBudget = isOverBudget(stats.totalExpense, budgets),
            recentTransactions = transactions
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainState()
    )

    fun onEvent(event: MainEvent) {
        when (event) {
            is MainEvent.SelectAccount -> {
                _state.update { it.copy(selectedAccountId = event.accountId) }
            }
            is MainEvent.SelectMonth -> {
                _state.update { it.copy(selectedMonth = event.yearMonth) }
            }
            MainEvent.ShowAddTransactionDialog -> {
                _state.update { it.copy(isAddTransactionDialogVisible = true) }
            }
            MainEvent.HideAddTransactionDialog -> {
                _state.update { it.copy(isAddTransactionDialogVisible = false) }
            }
            is MainEvent.AddTransaction -> {
                addNewTransaction(event)
            }
            is MainEvent.DeleteTransaction -> {
                // TODO: 实现删除交易功能
            }
            MainEvent.ShowFilterDialog -> {
                _state.update { it.copy(isFilterDialogVisible = true) }
            }
            MainEvent.HideFilterDialog -> {
                _state.update { it.copy(isFilterDialogVisible = false) }
            }
            is MainEvent.ApplyFilter -> {
                // TODO: 实现筛选功能
            }
            MainEvent.ClearFilter -> {
                // TODO: 实现清除筛选功能
            }
            MainEvent.DismissError -> {
                _state.update { it.copy(error = null) }
            }
            MainEvent.Refresh -> {
                // 刷新数据的逻辑已经由Flow自动处理
            }
        }
    }

    private fun addNewTransaction(event: MainEvent.AddTransaction) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            val transaction = Transaction(
                amount = event.amount,
                type = event.type,
                categoryId = event.categoryId,
                categoryName = "", // TODO: 从分类仓库获取
                categoryIcon = "", // TODO: 从分类仓库获取
                categoryColor = 0, // TODO: 从分类仓库获取
                accountId = event.accountId,
                accountName = "", // TODO: 从账户仓库获取
                note = event.note,
                date = LocalDateTime.now(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

            when (val result = addTransaction(transaction)) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isAddTransactionDialogVisible = false
                        )
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = result.exception.message
                        )
                    }
                }
                Result.Loading -> Unit
            }
        }
    }

    private fun calculateBudgetProgress(
        totalExpense: Double,
        budgets: List<com.example.myapplication.domain.model.Budget>
    ): Float {
        val monthlyBudget = budgets.firstOrNull { it.categoryId == null }
            ?: return 0f
        return (totalExpense / monthlyBudget.amount).toFloat()
            .coerceIn(0f, 1f)
    }

    private fun isOverBudget(
        totalExpense: Double,
        budgets: List<com.example.myapplication.domain.model.Budget>
    ): Boolean {
        val monthlyBudget = budgets.firstOrNull { it.categoryId == null }
            ?: return false
        return totalExpense > monthlyBudget.amount
    }
} 
package com.example.myapplication.presentation.budget.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.model.Budget

@Composable
fun BudgetList(
    budgets: List<Budget>,
    selectedBudgetId: Long?,
    onBudgetClick: (Budget) -> Unit,
    onEditClick: (Budget) -> Unit,
    onDeleteClick: (Budget) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 总预算
        val overallBudget = budgets.find { it.categoryId == null }
        if (overallBudget != null) {
            item(key = "overall") {
                BudgetItem(
                    budget = overallBudget,
                    isSelected = overallBudget.id == selectedBudgetId,
                    onBudgetClick = onBudgetClick,
                    onEditClick = onEditClick,
                    onDeleteClick = onDeleteClick
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            item {
                Text(
                    text = "分类预算",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
        
        // 分类预算
        val categoryBudgets = budgets.filter { it.categoryId != null }
        if (categoryBudgets.isNotEmpty()) {
            items(
                items = categoryBudgets,
                key = { it.id }
            ) { budget ->
                BudgetItem(
                    budget = budget,
                    isSelected = budget.id == selectedBudgetId,
                    onBudgetClick = onBudgetClick,
                    onEditClick = onEditClick,
                    onDeleteClick = onDeleteClick
                )
            }
        } else {
            item {
                Text(
                    text = "暂无分类预算",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
} 
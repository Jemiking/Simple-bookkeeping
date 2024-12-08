package com.example.myapplication.presentation.budget.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.model.Category
import com.example.myapplication.presentation.budget.EditingBudget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetDialog(
    budget: EditingBudget,
    categories: List<Category>,
    onAmountChanged: (String) -> Unit,
    onCategorySelected: (Long?) -> Unit,
    onThresholdChanged: (String) -> Unit,
    onEnabledChanged: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (budget.id == 0L) "添加预算" else "编辑预算"
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 金额输入
                OutlinedTextField(
                    value = budget.amount,
                    onValueChange = onAmountChanged,
                    label = { Text("预算金额") },
                    singleLine = true,
                    isError = budget.amountError != null,
                    supportingText = budget.amountError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // 分类选择
                if (categories.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = false,
                        onExpandedChange = { },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = categories.find { it.id == budget.categoryId }?.name ?: "总预算",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("预算分类") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        
                        DropdownMenu(
                            expanded = false,
                            onDismissRequest = { }
                        ) {
                            // 总预算选项
                            DropdownMenuItem(
                                text = { Text("总预算") },
                                onClick = { onCategorySelected(null) }
                            )
                            
                            // 分类预算选项
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = { onCategorySelected(category.id) }
                                )
                            }
                        }
                    }
                }
                
                // 提醒阈值
                OutlinedTextField(
                    value = budget.notifyThreshold,
                    onValueChange = onThresholdChanged,
                    label = { Text("提醒阈值 (0-1)") },
                    singleLine = true,
                    isError = budget.thresholdError != null,
                    supportingText = budget.thresholdError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // 启用状态
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "启用预算",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = budget.isEnabled,
                        onCheckedChange = onEnabledChanged
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = budget.amountError == null && budget.thresholdError == null
            ) {
                Text(if (budget.id == 0L) "添加" else "保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        modifier = modifier
    )
} 
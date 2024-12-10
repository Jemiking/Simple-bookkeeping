package com.example.myapplication.presentation.budget.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.myapplication.domain.model.Budget

@Composable
fun DeleteBudgetDialog(
    budget: Budget,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("删除预算")
        },
        text = {
            Text(
                text = if (budget.categoryId == null) {
                    "确定要删除总预算吗？此操作无法撤销。"
                } else {
                    "确定要删除\"${budget.categoryName}\"的预算吗？此操作无法撤销。"
                }
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text("删除")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("取消")
            }
        },
        modifier = modifier
    )
} 
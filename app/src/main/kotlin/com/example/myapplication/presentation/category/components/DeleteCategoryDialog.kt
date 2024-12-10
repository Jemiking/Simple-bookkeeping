package com.example.myapplication.presentation.category.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.myapplication.domain.model.Category

@Composable
fun DeleteCategoryDialog(
    category: Category,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("删除分类")
        },
        text = {
            Text("确定要删除\"${category.name}\"分类吗？此操作无法撤销。")
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
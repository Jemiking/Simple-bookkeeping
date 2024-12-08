package com.example.myapplication.presentation.transaction.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.format.DateTimeFormatter

@Composable
fun TransactionDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: TransactionDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("交易详情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (!state.isEditMode) {
                        IconButton(
                            onClick = { viewModel.onEvent(TransactionDetailEvent.ToggleEditMode) }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "编辑")
                        }
                        IconButton(
                            onClick = { viewModel.onEvent(TransactionDetailEvent.ShowDeleteConfirmation) }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "删除")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            state.transaction?.let { transaction ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (state.isEditMode) {
                        // TODO: 实现编辑模式UI
                    } else {
                        // 显示模式
                        Text(
                            text = "金额：${transaction.amount}",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "类型：${if (transaction.type.name == "EXPENSE") "支出" else "收入"}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "分类：${transaction.categoryName}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "账户：${transaction.accountName}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "时间：${transaction.date.format(dateFormatter)}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        transaction.note?.let { note ->
                            if (note.isNotBlank()) {
                                Text(
                                    text = "备注：$note",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }

            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            state.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }

            if (state.showDeleteConfirmation) {
                AlertDialog(
                    onDismissRequest = {
                        viewModel.onEvent(TransactionDetailEvent.HideDeleteConfirmation)
                    },
                    title = { Text("确认删除") },
                    text = { Text("确定要删除这条交易记录吗？") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.onEvent(TransactionDetailEvent.DeleteTransaction)
                            }
                        ) {
                            Text("删除")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                viewModel.onEvent(TransactionDetailEvent.HideDeleteConfirmation)
                            }
                        ) {
                            Text("取消")
                        }
                    }
                )
            }
        }
    }
} 
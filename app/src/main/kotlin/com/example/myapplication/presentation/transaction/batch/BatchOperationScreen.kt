package com.example.myapplication.presentation.transaction.batch

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.presentation.components.SwipeToDismissCard
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchOperationScreen(
    onNavigateBack: () -> Unit,
    viewModel: BatchOperationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val numberFormat = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("批量操作") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 全选按钮
                    IconButton(onClick = { viewModel.toggleSelectAll() }) {
                        Icon(
                            imageVector = if (state.isAllSelected) {
                                Icons.Default.CheckBox
                            } else {
                                Icons.Default.CheckBoxOutlineBlank
                            },
                            contentDescription = "全选"
                        )
                    }
                    // 批量操作菜单
                    IconButton(onClick = { viewModel.toggleOperationMenu() }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "更多操作")
                    }
                    DropdownMenu(
                        expanded = state.showOperationMenu,
                        onDismissRequest = { viewModel.toggleOperationMenu() }
                    ) {
                        DropdownMenuItem(
                            text = { Text("批量编辑") },
                            onClick = {
                                viewModel.startBatchEdit()
                                viewModel.toggleOperationMenu()
                            },
                            enabled = state.selectedTransactions.isNotEmpty()
                        )
                        DropdownMenuItem(
                            text = { Text("批量删除") },
                            onClick = {
                                viewModel.showDeleteConfirmation()
                                viewModel.toggleOperationMenu()
                            },
                            enabled = state.selectedTransactions.isNotEmpty()
                        )
                        DropdownMenuItem(
                            text = { Text("调整分类") },
                            onClick = {
                                viewModel.showCategorySelection()
                                viewModel.toggleOperationMenu()
                            },
                            enabled = state.selectedTransactions.isNotEmpty()
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (state.selectedTransactions.isNotEmpty()) {
                BottomAppBar {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "已选择 ${state.selectedTransactions.size} 项",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.clearSelection() }
                            ) {
                                Text("取消")
                            }
                            Button(
                                onClick = { viewModel.startBatchEdit() },
                                enabled = state.selectedTransactions.isNotEmpty()
                            ) {
                                Text("编辑")
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = state.transactions,
                        key = { it.id }
                    ) { transaction ->
                        SwipeToDismissCard(
                            onDismiss = { viewModel.deleteTransaction(transaction.id) }
                        ) {
                            TransactionBatchItem(
                                transaction = transaction,
                                isSelected = state.selectedTransactions.contains(transaction.id),
                                onToggleSelection = { viewModel.toggleSelection(transaction.id) },
                                numberFormat = numberFormat
                            )
                        }
                    }
                }
            }

            // 错误提示
            if (state.error != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.dismissError() }) {
                            Text("确定")
                        }
                    }
                ) {
                    Text(state.error)
                }
            }
        }
    }

    // 删除确认对话框
    if (state.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteConfirmation() },
            title = { Text("确认删除") },
            text = { Text("确定要删除选中的 ${state.selectedTransactions.size} 条记录吗？此操作无法撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.confirmDelete()
                        viewModel.dismissDeleteConfirmation()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteConfirmation() }) {
                    Text("取消")
                }
            }
        )
    }

    // 分类选择对话框
    if (state.showCategorySelection) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCategorySelection() },
            title = { Text("选择分类") },
            text = {
                Column {
                    state.categories.forEach { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(category.name)
                            RadioButton(
                                selected = category.id == state.selectedCategoryId,
                                onClick = { viewModel.selectCategory(category.id) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.confirmCategoryChange()
                        viewModel.dismissCategorySelection()
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissCategorySelection() }) {
                    Text("取消")
                }
            }
        )
    }
} 
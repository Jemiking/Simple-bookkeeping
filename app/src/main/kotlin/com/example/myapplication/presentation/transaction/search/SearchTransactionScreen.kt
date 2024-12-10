package com.example.myapplication.presentation.transaction.search

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
import com.example.myapplication.data.local.entity.TransactionType
import com.example.myapplication.presentation.main.components.TransactionItem
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTransactionScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTransactionDetail: (Long) -> Unit,
    viewModel: SearchTransactionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = { viewModel.onEvent(SearchTransactionEvent.UpdateQuery(it)) },
                        placeholder = { Text("搜索交易记录") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.onEvent(SearchTransactionEvent.ToggleFilters) }
                    ) {
                        Icon(
                            imageVector = if (state.showFilters) Icons.Default.FilterList else Icons.Default.FilterListOff,
                            contentDescription = if (state.showFilters) "隐藏筛选" else "显示筛选"
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
            AnimatedVisibility(
                visible = state.showFilters,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // 交易类型筛选
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FilterChip(
                            selected = state.selectedType == TransactionType.EXPENSE,
                            onClick = {
                                viewModel.onEvent(
                                    SearchTransactionEvent.SelectType(
                                        if (state.selectedType == TransactionType.EXPENSE) null
                                        else TransactionType.EXPENSE
                                    )
                                )
                            },
                            label = { Text("支出") }
                        )
                        FilterChip(
                            selected = state.selectedType == TransactionType.INCOME,
                            onClick = {
                                viewModel.onEvent(
                                    SearchTransactionEvent.SelectType(
                                        if (state.selectedType == TransactionType.INCOME) null
                                        else TransactionType.INCOME
                                    )
                                )
                            },
                            label = { Text("收入") }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 金额范围筛选
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedTextField(
                            value = state.minAmount?.toString() ?: "",
                            onValueChange = { value ->
                                viewModel.onEvent(
                                    SearchTransactionEvent.SetAmountRange(
                                        minAmount = value.toDoubleOrNull(),
                                        maxAmount = state.maxAmount
                                    )
                                )
                            },
                            label = { Text("最小金额") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = state.maxAmount?.toString() ?: "",
                            onValueChange = { value ->
                                viewModel.onEvent(
                                    SearchTransactionEvent.SetAmountRange(
                                        minAmount = state.minAmount,
                                        maxAmount = value.toDoubleOrNull()
                                    )
                                )
                            },
                            label = { Text("最大金额") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 清除筛选按钮
                    TextButton(
                        onClick = { viewModel.onEvent(SearchTransactionEvent.ClearFilters) },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("清除筛选")
                    }
                }
            }

            // 搜索结果列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.transactions) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onClick = { onNavigateToTransactionDetail(transaction.id) }
                    )
                }
            }

            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            if (state.transactions.isEmpty() && !state.isLoading) {
                Text(
                    text = "没有找到匹配的交易记录",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
} 
package com.example.myapplication.presentation.transaction.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.data.local.entity.TransactionType
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    viewModel: TransactionDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

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
                    state.transaction?.let { transaction ->
                        IconButton(onClick = { onNavigateToEdit(transaction.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "编辑")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            } else {
                state.transaction?.let { transaction ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // 金额
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = when (transaction.type) {
                                        TransactionType.EXPENSE -> "支出"
                                        TransactionType.INCOME -> "收入"
                                        TransactionType.TRANSFER -> "转账"
                                    },
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = when (transaction.type) {
                                        TransactionType.EXPENSE -> "-¥%.2f".format(transaction.amount)
                                        TransactionType.INCOME -> "+¥%.2f".format(transaction.amount)
                                        TransactionType.TRANSFER -> "¥%.2f".format(transaction.amount)
                                    },
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = when (transaction.type) {
                                        TransactionType.EXPENSE -> MaterialTheme.colorScheme.error
                                        TransactionType.INCOME -> MaterialTheme.colorScheme.primary
                                        TransactionType.TRANSFER -> MaterialTheme.colorScheme.secondary
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 详细信息
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                // 日期
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("日期")
                                    Text(transaction.date.format(
                                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                                    )
                                }

                                Divider(modifier = Modifier.padding(vertical = 8.dp))

                                // 账户
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("账户")
                                    Text(transaction.accountName)
                                }

                                if (transaction.type == TransactionType.TRANSFER) {
                                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("转入账户")
                                        Text(transaction.toAccountName ?: "")
                                    }
                                }

                                Divider(modifier = Modifier.padding(vertical = 8.dp))

                                // 分类
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("分类")
                                    Text(transaction.categoryName)
                                }

                                Divider(modifier = Modifier.padding(vertical = 8.dp))

                                // 备注
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text("备注")
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = transaction.note.ifBlank { "无备注" },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} 
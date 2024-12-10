package com.example.myapplication.presentation.account

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.data.local.entity.AccountType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(
    accountId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    viewModel: AccountViewModel = hiltViewModel()
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()
    val account = state.accounts.find { it.id == accountId }

    LaunchedEffect(true) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AccountEffect.ShowMessage -> {
                    if (effect.message == "删除成功") {
                        onNavigateBack()
                    }
                }
                is AccountEffect.ShowError -> {
                    // TODO: Show error dialog
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("账户详情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(accountId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "删除")
                    }
                }
            )
        }
    ) { padding ->
        account?.let { a ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 账户基本信息卡片
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // 账户图标和名称
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = Color(android.graphics.Color.parseColor(a.color)),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = when (a.type) {
                                        AccountType.CASH -> Icons.Default.Money
                                        AccountType.BANK_CARD -> Icons.Default.CreditCard
                                        AccountType.CREDIT_CARD -> Icons.Default.CreditScore
                                        AccountType.ALIPAY -> Icons.Default.Payment
                                        AccountType.WECHAT -> Icons.Default.Message
                                        AccountType.OTHER -> Icons.Default.AccountBalance
                                    },
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .size(24.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = a.name,
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    text = when (a.type) {
                                        AccountType.CASH -> "现金"
                                        AccountType.BANK_CARD -> "银行卡"
                                        AccountType.CREDIT_CARD -> "信用卡"
                                        AccountType.ALIPAY -> "支付宝"
                                        AccountType.WECHAT -> "微信"
                                        AccountType.OTHER -> "其他"
                                    },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 余额
                        Text(
                            text = "当前余额",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "¥%.2f".format(a.balance),
                            style = MaterialTheme.typography.headlineMedium,
                            color = if (a.balance >= 0) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }

                // 账户详细信息卡片
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 备注
                        if (a.note.isNotBlank()) {
                            Column {
                                Text(
                                    text = "备注",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = a.note,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Divider()
                        }

                        // 创建时间
                        Column {
                            Text(
                                text = "创建时间",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = a.createdAt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Divider()

                        // 最后更新时间
                        Column {
                            Text(
                                text = "最后更新",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = a.updatedAt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        } ?: run {
            // 账户不存在
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("账户不存在")
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除这个账户吗？删除后将无法恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(AccountEvent.DeleteAccount(accountId))
                        showDeleteDialog = false
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
} 
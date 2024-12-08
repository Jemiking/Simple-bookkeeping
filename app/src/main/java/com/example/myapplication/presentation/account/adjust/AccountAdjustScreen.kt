package com.example.myapplication.presentation.account.adjust

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountAdjustScreen(
    onNavigateBack: () -> Unit,
    viewModel: AccountAdjustViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("余额调整") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // 账户选择
            OutlinedCard(
                onClick = { viewModel.onEvent(AccountAdjustEvent.ShowAccountSelector) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "选择账户",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = state.selectedAccount?.name ?: "请选择账户",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 当前余额显示
            if (state.selectedAccount != null) {
                Text(
                    text = "当前余额：¥${state.currentBalance}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // 新余额输入
            OutlinedTextField(
                value = state.newBalance,
                onValueChange = { viewModel.onEvent(AccountAdjustEvent.UpdateNewBalance(it)) },
                label = { Text("新余额") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("¥") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 备注输入
            OutlinedTextField(
                value = state.note,
                onValueChange = { viewModel.onEvent(AccountAdjustEvent.UpdateNote(it)) },
                label = { Text("备注（可选）") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 调整按钮
            Button(
                onClick = { viewModel.onEvent(AccountAdjustEvent.AdjustBalance) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading &&
                    state.selectedAccount != null &&
                    state.newBalance.isNotBlank()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("确认调整")
                }
            }

            // 错误提示
            state.error?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // 账户选择对话框
        if (state.showAccountSelector) {
            AlertDialog(
                onDismissRequest = {
                    viewModel.onEvent(AccountAdjustEvent.HideAccountSelector)
                },
                title = { Text("选择账户") },
                text = {
                    Column {
                        state.accounts.forEach { account ->
                            TextButton(
                                onClick = {
                                    viewModel.onEvent(AccountAdjustEvent.SelectAccount(account))
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(account.name)
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(
                        onClick = {
                            viewModel.onEvent(AccountAdjustEvent.HideAccountSelector)
                        }
                    ) {
                        Text("取消")
                    }
                }
            )
        }
    }
} 
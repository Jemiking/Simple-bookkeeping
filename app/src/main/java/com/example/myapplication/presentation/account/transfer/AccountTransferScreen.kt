package com.example.myapplication.presentation.account.transfer

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
fun AccountTransferScreen(
    onNavigateBack: () -> Unit,
    viewModel: AccountTransferViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("账户转账") },
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
            // 转出账户选择
            OutlinedCard(
                onClick = {
                    viewModel.onEvent(AccountTransferEvent.ToggleAccountSelectorType)
                    viewModel.onEvent(AccountTransferEvent.ShowAccountSelector)
                },
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
                            text = "转出账户",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = state.fromAccount?.name ?: "请选择账户",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 转入账户选择
            OutlinedCard(
                onClick = {
                    viewModel.onEvent(AccountTransferEvent.ToggleAccountSelectorType)
                    viewModel.onEvent(AccountTransferEvent.ShowAccountSelector)
                },
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
                            text = "转入账户",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = state.toAccount?.name ?: "请选择账户",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 转账金额输入
            OutlinedTextField(
                value = state.amount,
                onValueChange = { viewModel.onEvent(AccountTransferEvent.UpdateAmount(it)) },
                label = { Text("转账金额") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("¥") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 备注输入
            OutlinedTextField(
                value = state.note,
                onValueChange = { viewModel.onEvent(AccountTransferEvent.UpdateNote(it)) },
                label = { Text("备注（可选）") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 转账按钮
            Button(
                onClick = { viewModel.onEvent(AccountTransferEvent.Transfer) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading &&
                    state.fromAccount != null &&
                    state.toAccount != null &&
                    state.amount.isNotBlank()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("确认转账")
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
                    viewModel.onEvent(AccountTransferEvent.HideAccountSelector)
                },
                title = {
                    Text(
                        if (state.isSelectingFromAccount) "选择转出账户"
                        else "选择转入账户"
                    )
                },
                text = {
                    Column {
                        state.accounts.forEach { account ->
                            // 如果是选择转出账户，不显示已选���的转入账户
                            // 如果是选择转入账户，不显示已选择的转出账户
                            if ((state.isSelectingFromAccount && account.id != state.toAccount?.id) ||
                                (!state.isSelectingFromAccount && account.id != state.fromAccount?.id)
                            ) {
                                TextButton(
                                    onClick = {
                                        if (state.isSelectingFromAccount) {
                                            viewModel.onEvent(AccountTransferEvent.SelectFromAccount(account))
                                        } else {
                                            viewModel.onEvent(AccountTransferEvent.SelectToAccount(account))
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(account.name)
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(
                        onClick = {
                            viewModel.onEvent(AccountTransferEvent.HideAccountSelector)
                        }
                    ) {
                        Text("取消")
                    }
                }
            )
        }
    }
} 
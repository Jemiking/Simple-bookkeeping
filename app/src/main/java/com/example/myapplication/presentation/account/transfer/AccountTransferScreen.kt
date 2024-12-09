package com.example.myapplication.presentation.account.transfer

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountTransferScreen(
    onNavigateBack: () -> Unit,
    viewModel: AccountTransferViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    val numberFormat = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }

    LaunchedEffect(state.transferSuccess) {
        if (state.transferSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("账户转账") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.transfer() },
                        enabled = state.isValid
                    ) {
                        Text("确认转账")
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
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 转出账户
                    ExposedDropdownMenuBox(
                        expanded = state.showFromAccountDropdown,
                        onExpandedChange = { viewModel.toggleFromAccountDropdown() }
                    ) {
                        OutlinedTextField(
                            value = state.fromAccount?.name ?: "选择转出账户",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("转出账户") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.showFromAccountDropdown)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            supportingText = state.fromAccount?.let {
                                { Text("当前余额: ${numberFormat.format(it.balance)}") }
                            }
                        )

                        ExposedDropdownMenu(
                            expanded = state.showFromAccountDropdown,
                            onDismissRequest = { viewModel.toggleFromAccountDropdown() }
                        ) {
                            state.accounts.forEach { account ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(account.name)
                                            Text(
                                                text = numberFormat.format(account.balance),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.selectFromAccount(account)
                                        viewModel.toggleFromAccountDropdown()
                                    }
                                )
                            }
                        }
                    }

                    // 转入账户
                    ExposedDropdownMenuBox(
                        expanded = state.showToAccountDropdown,
                        onExpandedChange = { viewModel.toggleToAccountDropdown() }
                    ) {
                        OutlinedTextField(
                            value = state.toAccount?.name ?: "选择转入账户",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("转入账户") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.showToAccountDropdown)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            supportingText = state.toAccount?.let {
                                { Text("当前余额: ${numberFormat.format(it.balance)}") }
                            }
                        )

                        ExposedDropdownMenu(
                            expanded = state.showToAccountDropdown,
                            onDismissRequest = { viewModel.toggleToAccountDropdown() }
                        ) {
                            state.accounts.filter { it.id != state.fromAccount?.id }
                                .forEach { account ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(account.name)
                                                Text(
                                                    text = numberFormat.format(account.balance),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = {
                                            viewModel.selectToAccount(account)
                                            viewModel.toggleToAccountDropdown()
                                        }
                                    )
                                }
                        }
                    }

                    // 转账金额
                    OutlinedTextField(
                        value = state.amount,
                        onValueChange = { viewModel.updateAmount(it) },
                        label = { Text("转账金额") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = state.amountError != null,
                        supportingText = state.amountError?.let { { Text(it) } },
                        singleLine = true
                    )

                    // 备注
                    OutlinedTextField(
                        value = state.note,
                        onValueChange = { viewModel.updateNote(it) },
                        label = { Text("备注") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )

                    // 转账预览
                    if (state.isValid) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "转账预览",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "转出账户余额",
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = state.fromAccount?.let {
                                            numberFormat.format(it.balance - state.amount.toBigDecimalOrNull()!!)
                                        } ?: "",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "转入账户余额",
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = state.toAccount?.let {
                                            numberFormat.format(it.balance + state.amount.toBigDecimalOrNull()!!)
                                        } ?: "",
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
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
} 
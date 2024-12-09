package com.example.myapplication.presentation.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.data.local.entity.AccountType
import com.example.myapplication.domain.model.Account

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountScreen(
    onNavigateBack: () -> Unit,
    viewModel: AccountViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(AccountType.CASH) }
    var selectedColor by remember { mutableStateOf("#2196F3") }

    LaunchedEffect(true) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AccountEffect.ShowMessage -> {
                    if (effect.message == "添加成功") {
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
                title = { Text("添加账户") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 账户类型选择
            Text(
                text = "账户类型",
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AccountType.values().forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = {
                            Text(
                                when (type) {
                                    AccountType.CASH -> "现金"
                                    AccountType.BANK_CARD -> "银行卡"
                                    AccountType.CREDIT_CARD -> "信用卡"
                                    AccountType.ALIPAY -> "支付宝"
                                    AccountType.WECHAT -> "微信"
                                    AccountType.OTHER -> "其他"
                                }
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = when (type) {
                                    AccountType.CASH -> Icons.Default.Money
                                    AccountType.BANK_CARD -> Icons.Default.CreditCard
                                    AccountType.CREDIT_CARD -> Icons.Default.CreditScore
                                    AccountType.ALIPAY -> Icons.Default.Payment
                                    AccountType.WECHAT -> Icons.Default.Message
                                    AccountType.OTHER -> Icons.Default.AccountBalance
                                },
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }

            // 账户名称
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("账户名称") },
                modifier = Modifier.fillMaxWidth()
            )

            // 初始余额
            OutlinedTextField(
                value = balance,
                onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) balance = it },
                label = { Text("初始余额") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("¥") }
            )

            // 备注
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("备注") },
                modifier = Modifier.fillMaxWidth()
            )

            // 颜色选择
            Text(
                text = "账户颜色",
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "#2196F3", // Blue
                    "#4CAF50", // Green
                    "#F44336", // Red
                    "#FF9800", // Orange
                    "#9C27B0", // Purple
                    "#795548"  // Brown
                ).forEach { color ->
                    Surface(
                        onClick = { selectedColor = color },
                        shape = MaterialTheme.shapes.small,
                        color = Color(android.graphics.Color.parseColor(color)),
                        border = if (selectedColor == color) {
                            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        } else {
                            null
                        },
                        modifier = Modifier.size(40.dp)
                    ) {}
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 保存按钮
            Button(
                onClick = {
                    val balanceValue = balance.toDoubleOrNull()
                    if (name.isNotBlank() && balanceValue != null) {
                        viewModel.onEvent(
                            AccountEvent.AddAccount(
                                Account(
                                    name = name,
                                    type = selectedType,
                                    balance = balanceValue,
                                    icon = when (selectedType) {
                                        AccountType.CASH -> "money"
                                        AccountType.BANK_CARD -> "credit_card"
                                        AccountType.CREDIT_CARD -> "credit_score"
                                        AccountType.ALIPAY -> "payment"
                                        AccountType.WECHAT -> "message"
                                        AccountType.OTHER -> "account_balance"
                                    },
                                    color = selectedColor,
                                    note = note
                                )
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && balance.isNotBlank()
            ) {
                Text("保存")
            }
        }
    }
} 
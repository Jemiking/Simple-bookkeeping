package com.example.myapplication.presentation.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.data.local.entity.TransactionType
import com.example.myapplication.domain.model.Transaction
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onNavigateBack: () -> Unit,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDateTime.now()) }
    
    LaunchedEffect(true) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is TransactionEffect.ShowMessage -> {
                    // TODO: Show snackbar
                    if (effect.message == "添加成功") {
                        onNavigateBack()
                    }
                }
                is TransactionEffect.ShowError -> {
                    // TODO: Show error dialog
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加交易") },
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
            // 交易类型选择
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TransactionType.values().forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = {
                            Text(
                                when (type) {
                                    TransactionType.EXPENSE -> "支出"
                                    TransactionType.INCOME -> "收入"
                                    TransactionType.TRANSFER -> "转账"
                                }
                            )
                        }
                    )
                }
            }

            // 金额输入
            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) amount = it },
                label = { Text("金额") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("¥") }
            )

            // 备注输入
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("备注") },
                modifier = Modifier.fillMaxWidth()
            )

            // 日期选择
            OutlinedTextField(
                value = selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                onValueChange = {},
                label = { Text("日期") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "选择日期")
                    }
                }
            )

            // TODO: 添加账户选择
            // TODO: 添加分类选择

            Spacer(modifier = Modifier.weight(1f))

            // 保存按钮
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    if (amountValue != null && note.isNotBlank()) {
                        viewModel.onEvent(
                            TransactionEvent.AddTransaction(
                                Transaction(
                                    amount = amountValue,
                                    type = selectedType,
                                    note = note,
                                    date = selectedDate,
                                    // TODO: 替换为实际选择的账户和分类ID
                                    accountId = 1,
                                    categoryId = 1
                                )
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = amount.isNotBlank() && note.isNotBlank()
            ) {
                Text("保存")
            }
        }
    }

    if (showDatePicker) {
        // TODO: 实现日期选择器
        showDatePicker = false
    }
} 
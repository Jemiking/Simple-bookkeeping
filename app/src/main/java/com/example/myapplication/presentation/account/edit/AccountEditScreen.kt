package com.example.myapplication.presentation.account.edit

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
import com.example.myapplication.domain.model.AccountType
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: AccountEditViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    val numberFormat = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "编辑账户" else "新建账户") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveAccount() },
                        enabled = state.isValid
                    ) {
                        Text("保存")
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
                    // 账户名称
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = { viewModel.updateName(it) },
                        label = { Text("账户名称") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = state.nameError != null,
                        supportingText = state.nameError?.let { { Text(it) } },
                        singleLine = true
                    )

                    // 账户类型
                    ExposedDropdownMenuBox(
                        expanded = state.showTypeDropdown,
                        onExpandedChange = { viewModel.toggleTypeDropdown() }
                    ) {
                        OutlinedTextField(
                            value = state.type.toString(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("账户类型") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.showTypeDropdown)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = state.showTypeDropdown,
                            onDismissRequest = { viewModel.toggleTypeDropdown() }
                        ) {
                            AccountType.values().forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.toString()) },
                                    onClick = {
                                        viewModel.updateType(type)
                                        viewModel.toggleTypeDropdown()
                                    }
                                )
                            }
                        }
                    }

                    // 初始余额
                    OutlinedTextField(
                        value = state.balance,
                        onValueChange = { viewModel.updateBalance(it) },
                        label = { Text("初始余额") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = state.balanceError != null,
                        supportingText = state.balanceError?.let { { Text(it) } },
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
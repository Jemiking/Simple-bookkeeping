package com.example.myapplication.presentation.transaction.batch

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
fun BatchEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: BatchEditViewModel = hiltViewModel()
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
                title = { Text("批量编辑") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveChanges() },
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
                    // 选中的���易数量
                    Text(
                        text = "已选择 ${state.selectedCount} 条记录",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // 分类选择
                    ExposedDropdownMenuBox(
                        expanded = state.showCategoryDropdown,
                        onExpandedChange = { viewModel.toggleCategoryDropdown() }
                    ) {
                        OutlinedTextField(
                            value = state.selectedCategory?.name ?: "选择分类",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("分类") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.showCategoryDropdown)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = state.showCategoryDropdown,
                            onDismissRequest = { viewModel.toggleCategoryDropdown() }
                        ) {
                            state.categories.forEach { category ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = category.icon,
                                                contentDescription = null,
                                                tint = category.color,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Text(category.name)
                                        }
                                    },
                                    onClick = {
                                        viewModel.selectCategory(category)
                                        viewModel.toggleCategoryDropdown()
                                    }
                                )
                            }
                        }
                    }

                    // 金额调整
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "金额调整",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = state.adjustmentAmount,
                                onValueChange = { viewModel.updateAdjustmentAmount(it) },
                                label = { Text("调整金额") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                isError = state.adjustmentAmountError != null,
                                supportingText = state.adjustmentAmountError?.let { { Text(it) } },
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = state.adjustmentPercentage,
                                onValueChange = { viewModel.updateAdjustmentPercentage(it) },
                                label = { Text("调整百分比") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                isError = state.adjustmentPercentageError != null,
                                supportingText = state.adjustmentPercentageError?.let { { Text(it) } },
                                singleLine = true,
                                trailingIcon = { Text("%") }
                            )
                        }
                    }

                    // 日期调整
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "日期调整",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = state.adjustmentDays,
                                onValueChange = { viewModel.updateAdjustmentDays(it) },
                                label = { Text("调整天数") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = state.adjustmentDaysError != null,
                                supportingText = state.adjustmentDaysError?.let { { Text(it) } },
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = state.adjustmentMonths,
                                onValueChange = { viewModel.updateAdjustmentMonths(it) },
                                label = { Text("调整月数") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = state.adjustmentMonthsError != null,
                                supportingText = state.adjustmentMonthsError?.let { { Text(it) } },
                                singleLine = true
                            )
                        }
                    }

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
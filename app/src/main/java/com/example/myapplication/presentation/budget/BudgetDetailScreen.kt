package com.example.myapplication.presentation.budget

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.R
import com.example.myapplication.domain.model.Category
import com.example.myapplication.presentation.components.MonthYearPicker
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun BudgetDetailScreen(
    budgetId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: BudgetDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showMonthPicker by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(budgetId) {
        budgetId?.let { viewModel.loadBudget(it) }
    }

    LaunchedEffect(true) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is BudgetDetailEffect.NavigateBack -> onNavigateBack()
                is BudgetDetailEffect.ShowError -> {
                    // TODO: Show error dialog
                }
            }
        }
    }

    if (showMonthPicker) {
        MonthYearPicker(
            initialYearMonth = state.yearMonth,
            onYearMonthSelected = { yearMonth ->
                viewModel.onEvent(BudgetDetailEvent.UpdateYearMonth(yearMonth))
                showMonthPicker = false
            },
            onDismiss = { showMonthPicker = false }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除这个预算吗？这个操作不能撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(BudgetDetailEvent.DeleteBudget)
                        showDeleteConfirmDialog = false
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (budgetId == null) {
                            stringResource(R.string.add_budget)
                        } else {
                            stringResource(R.string.edit_budget)
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (budgetId != null) {
                        IconButton(onClick = { showDeleteConfirmDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "删除")
                        }
                    }
                    IconButton(
                        onClick = { viewModel.onEvent(BudgetDetailEvent.SaveBudget) }
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "保存")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 月份选择
                OutlinedCard(
                    onClick = { showMonthPicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "预算月份",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.yearMonth.format(
                                DateTimeFormatter.ofPattern("yyyy年MM月")
                            ),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                // 分类选择
                if (state.categories.isNotEmpty()) {
                    Text(
                        text = "选择分类",
                        style = MaterialTheme.typography.titleMedium
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.categories) { category ->
                            CategoryChip(
                                category = category,
                                isSelected = category.id == state.categoryId,
                                onClick = {
                                    viewModel.onEvent(
                                        BudgetDetailEvent.UpdateCategory(category.id)
                                    )
                                }
                            )
                        }
                    }
                }

                // 预算金额
                OutlinedTextField(
                    value = state.amount,
                    onValueChange = { value ->
                        viewModel.onEvent(BudgetDetailEvent.UpdateAmount(value))
                    },
                    label = { Text("预算金额") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.amountError != null,
                    supportingText = state.amountError?.let { { Text(it) } }
                )

                // 备注
                OutlinedTextField(
                    value = state.note,
                    onValueChange = { value ->
                        viewModel.onEvent(BudgetDetailEvent.UpdateNote(value))
                    },
                    label = { Text("备注") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun CategoryChip(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(category.name) },
        modifier = modifier.fillMaxWidth()
    )
} 
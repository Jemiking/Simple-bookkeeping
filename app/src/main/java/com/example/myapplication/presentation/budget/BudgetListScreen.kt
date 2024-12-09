package com.example.myapplication.presentation.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.R
import com.example.myapplication.domain.model.BudgetProgress
import com.example.myapplication.presentation.components.MonthYearPicker
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun BudgetListScreen(
    onNavigateToAddBudget: () -> Unit,
    onNavigateToBudgetDetail: (Long) -> Unit,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showMonthPicker by remember { mutableStateOf(false) }

    LaunchedEffect(true) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is BudgetEffect.ShowError -> {
                    // TODO: Show error dialog
                }
            }
        }
    }

    if (showMonthPicker) {
        MonthYearPicker(
            initialYearMonth = state.selectedMonth,
            onYearMonthSelected = { yearMonth ->
                viewModel.onEvent(BudgetEvent.SelectMonth(yearMonth))
                showMonthPicker = false
            },
            onDismiss = { showMonthPicker = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.budget)) },
                actions = {
                    IconButton(onClick = onNavigateToAddBudget) {
                        Icon(Icons.Default.Add, contentDescription = "添加预算")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 月份选择器
            TextButton(
                onClick = { showMonthPicker = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = state.selectedMonth.format(
                        DateTimeFormatter.ofPattern("yyyy年MM月")
                    ),
                    style = MaterialTheme.typography.titleLarge
                )
            }

            // 总体预算进度
            state.totalBudget?.let { total ->
                TotalBudgetCard(
                    total = total,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 分类预算列表
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.budgets) { budget ->
                    BudgetItem(
                        budget = budget,
                        onClick = { onNavigateToBudgetDetail(budget.id) }
                    )
                }

                item {
                    if (state.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TotalBudgetCard(
    total: BudgetProgress,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "总预算",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "预算金额",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "¥%.2f".format(total.amount),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Column {
                    Text(
                        text = "已支出",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "¥%.2f".format(total.spentAmount),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Column {
                    Text(
                        text = "剩余",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "¥%.2f".format(total.remainingAmount),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (total.remainingAmount < 0) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = (total.progress / 100).toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = when {
                    total.progress >= 100 -> MaterialTheme.colorScheme.error
                    total.progress >= 80 -> MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                    else -> MaterialTheme.colorScheme.primary
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${total.progress.roundToInt()}%",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetItem(
    budget: BudgetProgress,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = budget.categoryName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (budget.note.isNotBlank()) {
                        Text(
                            text = budget.note,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Text(
                    text = "¥%.2f".format(budget.remainingAmount),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (budget.remainingAmount < 0) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = (budget.progress / 100).toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = when {
                    budget.progress >= 100 -> MaterialTheme.colorScheme.error
                    budget.progress >= 80 -> MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                    else -> MaterialTheme.colorScheme.primary
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "¥%.2f / ¥%.2f".format(budget.spentAmount, budget.amount),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "${budget.progress.roundToInt()}%",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
} 
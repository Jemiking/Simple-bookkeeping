package com.example.myapplication.presentation.transaction

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.presentation.transaction.components.CategoryGrid
import com.example.myapplication.presentation.transaction.components.NumberKeyboard
import com.example.myapplication.presentation.transaction.components.TransactionTypeSelector
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val numberFormat = NumberFormat.getCurrencyInstance(Locale.CHINA)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("记一笔") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    // 保存按钮
                    IconButton(
                        onClick = { viewModel.onEvent(AddTransactionEvent.SaveTransaction) },
                        enabled = state.selectedCategory != null && 
                                 state.selectedAccount != null &&
                                 state.amount.toDoubleOrNull() != null &&
                                 state.amount.toDoubleOrNull() != 0.0
                    ) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "保存"
                        )
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
            // 金��输入区域
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 交易类型选择器
                TransactionTypeSelector(
                    selectedType = state.type,
                    onTypeSelected = { type ->
                        viewModel.onEvent(AddTransactionEvent.TypeChanged(type))
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 金额显示
                Text(
                    text = if (state.amount == "0") "0.00" else state.amount,
                    style = MaterialTheme.typography.headlineLarge,
                    color = when (state.type) {
                        com.example.myapplication.data.local.entity.TransactionType.EXPENSE -> 
                            MaterialTheme.colorScheme.error
                        com.example.myapplication.data.local.entity.TransactionType.INCOME -> 
                            MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // 错误提示
                state.amountError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // 分类选择网格
            AnimatedVisibility(
                visible = state.categories.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                CategoryGrid(
                    categories = state.categories,
                    selectedCategory = state.selectedCategory,
                    onCategorySelected = { category ->
                        viewModel.onEvent(AddTransactionEvent.CategorySelected(category))
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // 数字键盘
            NumberKeyboard(
                onNumberClick = { number ->
                    viewModel.onEvent(AddTransactionEvent.NumberPressed(number))
                },
                onDecimalClick = {
                    viewModel.onEvent(AddTransactionEvent.DecimalPressed)
                },
                onDeleteClick = {
                    viewModel.onEvent(AddTransactionEvent.BackspacePressed)
                },
                onClearClick = {
                    viewModel.onEvent(AddTransactionEvent.ClearPressed)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            )
        }

        // 错误提示
        state.error?.let { error ->
            Snackbar(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(error)
            }
        }
    }
} 
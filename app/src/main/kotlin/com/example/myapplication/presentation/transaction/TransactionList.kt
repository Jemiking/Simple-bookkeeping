package com.example.myapplication.presentation.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.presentation.components.EmptyState
import com.example.myapplication.presentation.components.ErrorState
import com.example.myapplication.presentation.components.LoadingState

@Composable
fun TransactionList(
    transactions: List<Transaction>,
    isLoading: Boolean,
    error: String?,
    onRefresh: () -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onTransactionSwipe: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> LoadingState(
                message = "加载交易记录中...",
                modifier = Modifier.fillMaxWidth()
            )
            error != null -> ErrorState(
                message = error,
                onRetry = onRefresh,
                modifier = Modifier.fillMaxWidth()
            )
            transactions.isEmpty() -> EmptyState(
                message = "暂无交易记录",
                modifier = Modifier.fillMaxWidth()
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = transactions,
                    key = { it.id }
                ) { transaction ->
                    SwipeableTransactionItem(
                        transaction = transaction,
                        onClick = { onTransactionClick(transaction) },
                        onSwipe = { onTransactionSwipe(transaction) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SwipeableTransactionItem(
    transaction: Transaction,
    onClick: () -> Unit,
    onSwipe: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSwipeEnabled by remember { mutableStateOf(true) }

    Card(
        modifier = modifier,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.note,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = transaction.date.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${if (transaction.amount >= 0) "+" else ""}${transaction.amount}",
                style = MaterialTheme.typography.titleMedium,
                color = if (transaction.amount >= 0) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }
    }
} 
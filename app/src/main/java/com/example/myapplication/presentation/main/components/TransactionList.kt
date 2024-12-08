package com.example.myapplication.presentation.main.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.presentation.transaction.components.SwipeableTransactionItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TransactionList(
    transactions: List<Transaction>,
    onTransactionClick: (Transaction) -> Unit,
    onTransactionDelete: (Transaction) -> Unit,
    onRefresh: () -> Unit,
    isRefreshing: Boolean = false,
    modifier: Modifier = Modifier
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = onRefresh
    )

    val groupedTransactions = transactions.groupBy { 
        it.date.toLocalDate()
    }.toSortedMap(compareByDescending { it })

    Box(
        modifier = modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            groupedTransactions.forEach { (date, transactionsForDate) ->
                item(key = "date_$date") {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        DateHeader(date = date)
                    }
                }

                items(
                    items = transactionsForDate,
                    key = { it.id }
                ) { transaction ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + expandVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        SwipeableTransactionItem(
                            transaction = transaction,
                            onEdit = onTransactionClick,
                            onDelete = onTransactionDelete,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .animateItemPlacement(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                        )
                    }
                }
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            scale = true
        )
    }
}

@Composable
private fun DateHeader(
    date: LocalDate,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)

    val displayText = when(date) {
        today -> "今天"
        yesterday -> "昨天"
        else -> formatDate(date)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = displayText,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatDate(date: LocalDate): String {
    return date.format(
        DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINESE)
    )
} 
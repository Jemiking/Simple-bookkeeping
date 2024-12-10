package com.example.myapplication.presentation.main.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.presentation.transaction.components.SwipeableTransactionItem
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val PAGE_SIZE = 20

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TransactionList(
    transactions: List<Transaction>,
    onTransactionClick: (Transaction) -> Unit,
    onTransactionDelete: (Transaction) -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    isRefreshing: Boolean = false,
    isLoadingMore: Boolean = false,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = onRefresh
    )

    val groupedTransactions by remember(transactions) {
        derivedStateOf {
            transactions.groupBy { 
                it.date.toLocalDate()
            }.toSortedMap(compareByDescending { it })
        }
    }

    val isScrollInProgress by remember {
        derivedStateOf { listState.isScrollInProgress }
    }

    LaunchedEffect(listState) {
        snapshotFlow { 
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            
            lastVisibleItem >= totalItems - 5
        }
        .distinctUntilChanged()
        .collect { shouldLoadMore ->
            if (shouldLoadMore && !isLoadingMore) {
                onLoadMore()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            groupedTransactions.forEach { (date, transactionsForDate) ->
                stickyHeader(key = "date_$date") {
                    DateHeader(
                        date = date,
                        modifier = Modifier.animateItemPlacement(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    )
                }

                items(
                    items = transactionsForDate,
                    key = { it.id }
                ) { transaction ->
                    TransactionListItem(
                        transaction = transaction,
                        onEdit = onTransactionClick,
                        onDelete = onTransactionDelete,
                        isScrolling = isScrollInProgress,
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

            if (isLoadingMore) {
                item {
                    LoadingMoreIndicator()
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
private fun TransactionListItem(
    transaction: Transaction,
    onEdit: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit,
    isScrolling: Boolean,
    modifier: Modifier = Modifier
) {
    val animationSpec = remember {
        spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    }

    key(transaction.id) {
        SwipeableTransactionItem(
            transaction = transaction,
            onEdit = onEdit,
            onDelete = onDelete,
            enableAnimation = !isScrolling,
            modifier = modifier
        )
    }
}

@Composable
private fun DateHeader(
    date: LocalDate,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    val yesterday = remember { today.minusDays(1) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("M月d日") }

    val displayText = remember(date) {
        when(date) {
            today -> "今天"
            yesterday -> "昨天"
            else -> date.format(dateFormatter)
        }
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

@Composable
private fun LoadingMoreIndicator(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 2.dp
        )
    }
} 
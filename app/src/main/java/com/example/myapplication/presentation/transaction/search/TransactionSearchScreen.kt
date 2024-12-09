package com.example.myapplication.presentation.transaction.search

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.presentation.transaction.components.TransactionListItem

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun TransactionSearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    viewModel: TransactionSearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = { viewModel.onQueryChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        placeholder = { Text("搜索交易记录") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                keyboardController?.hide()
                                viewModel.search()
                            }
                        ),
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "搜索")
                        },
                        trailingIcon = {
                            Row {
                                if (state.query.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onQueryChange("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "清除")
                                    }
                                }
                                IconButton(onClick = { viewModel.toggleFilter() }) {
                                    Icon(
                                        imageVector = if (state.filter == TransactionFilter()) {
                                            Icons.Default.FilterList
                                        } else {
                                            Icons.Default.FilterListOff
                                        },
                                        contentDescription = "筛选"
                                    )
                                }
                            }
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
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
            } else if (state.error != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = state.error,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(
                        onClick = { viewModel.search() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("重试")
                    }
                }
            } else if (state.transactions.isEmpty()) {
                if (state.query.isEmpty() && state.filter == TransactionFilter()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (state.searchHistory.isNotEmpty()) {
                            item {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "搜索历史",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        TextButton(onClick = { viewModel.clearSearchHistory() }) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteSweep,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("清除")
                                        }
                                    }
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        state.searchHistory.forEach { query ->
                                            SuggestionChip(
                                                onClick = { viewModel.onQueryChange(query) },
                                                label = { Text(query) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (state.suggestedQueries.isNotEmpty()) {
                            item {
                                Column {
                                    Text(
                                        text = "搜索建议",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        state.suggestedQueries.forEach { query ->
                                            SuggestionChip(
                                                onClick = { viewModel.onQueryChange(query) },
                                                label = { Text(query) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "输入关键词搜索交易记录",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "可以搜索备注、标签、位置等信息",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "未找到相关交易记录",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "换个关键词试试吧",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "找到 ${state.transactions.size} 条相关记录",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (state.filter != TransactionFilter()) {
                                TextButton(
                                    onClick = { viewModel.onFilterChange(TransactionFilter()) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FilterListOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("清除筛选")
                                }
                            }
                        }
                    }

                    items(
                        items = state.transactions,
                        key = { it.id }
                    ) { transaction ->
                        TransactionSearchResultItem(
                            transaction = transaction,
                            searchQuery = state.query,
                            onClick = { onNavigateToDetail(transaction.id) },
                            animationDelay = state.transactions.indexOf(transaction) * 50
                        )
                    }
                }
            }
        }
    }

    if (state.showFilter) {
        TransactionFilterSheet(
            filter = state.filter,
            onFilterChange = { viewModel.onFilterChange(it) },
            onDismiss = { viewModel.toggleFilter() }
        )
    }
}

@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val rows = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentRowWidth = 0

        measurables.forEach { measurable ->
            val placeable = measurable.measure(constraints)
            if (currentRowWidth + placeable.width > constraints.maxWidth) {
                rows.add(currentRow)
                currentRow = mutableListOf(placeable)
                currentRowWidth = placeable.width
            } else {
                currentRow.add(placeable)
                currentRowWidth += placeable.width
            }
        }
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }

        val height = rows.sumOf { row ->
            row.maxOf { it.height }
        } + (rows.size - 1) * verticalArrangement.spacing.roundToPx()

        layout(constraints.maxWidth, height) {
            var y = 0
            rows.forEach { row ->
                var x = 0
                row.forEach { placeable ->
                    placeable.place(x, y)
                    x += placeable.width + horizontalArrangement.spacing.roundToPx()
                }
                y += row.maxOf { it.height } + verticalArrangement.spacing.roundToPx()
            }
        }
    }
} 
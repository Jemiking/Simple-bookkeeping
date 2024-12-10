package com.example.myapplication.presentation.category

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.R
import com.example.myapplication.data.local.entity.CategoryType
import com.example.myapplication.domain.model.Category
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CategoryListScreen(
    onNavigateToAddCategory: () -> Unit,
    onNavigateToCategoryDetail: (Long) -> Unit,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedType by remember { mutableStateOf<CategoryType?>(null) }

    LaunchedEffect(true) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is CategoryEffect.ShowMessage -> {
                    // TODO: Show snackbar
                }
                is CategoryEffect.ShowError -> {
                    // TODO: Show error dialog
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.categories)) },
                actions = {
                    IconButton(onClick = onNavigateToAddCategory) {
                        Icon(Icons.Default.Add, contentDescription = "添加分类")
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
            // 分类统计信息
            state.stats?.let { stats ->
                CategoryStatsCard(stats = stats)
            }

            // 分类类型筛选
            TabRow(
                selectedTabIndex = if (selectedType == null) 0 else 1,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedType == null,
                    onClick = {
                        selectedType = null
                        viewModel.onEvent(CategoryEvent.LoadCategories())
                    }
                ) {
                    Text(
                        text = "全部",
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Tab(
                    selected = selectedType == CategoryType.EXPENSE,
                    onClick = {
                        selectedType = CategoryType.EXPENSE
                        viewModel.onEvent(CategoryEvent.LoadCategories(type = CategoryType.EXPENSE))
                    }
                ) {
                    Text(
                        text = "支出",
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Tab(
                    selected = selectedType == CategoryType.INCOME,
                    onClick = {
                        selectedType = CategoryType.INCOME
                        viewModel.onEvent(CategoryEvent.LoadCategories(type = CategoryType.INCOME))
                    }
                ) {
                    Text(
                        text = "收入",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // 分类列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.categories) { category ->
                    CategoryItem(
                        category = category,
                        onClick = { onNavigateToCategoryDetail(category.id) }
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
fun CategoryStatsCard(
    stats: CategoryStats,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "分类统计",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "总数量",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = stats.totalCount.toString(),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Column {
                    Text(
                        text = "支出分类",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = stats.countByType[CategoryType.EXPENSE]?.toString() ?: "0",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Column {
                    Text(
                        text = "收入分类",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = stats.countByType[CategoryType.INCOME]?.toString() ?: "0",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryItem(
    category: Category,
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 分类图标
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = Color(android.graphics.Color.parseColor(category.color)),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (category.note.isNotBlank()) {
                            Text(
                                text = category.note,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // 子分类数量
                if (category.subCategories.isNotEmpty()) {
                    Badge {
                        Text(text = category.subCategories.size.toString())
                    }
                }
            }

            // 预算金额
            category.budgetAmount?.let { budget ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "预算",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "¥%.2f".format(budget),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
} 
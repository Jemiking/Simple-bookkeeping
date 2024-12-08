package com.example.myapplication.presentation.category

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.data.local.entity.TransactionType
import com.example.myapplication.presentation.category.components.CategoryDialog
import com.example.myapplication.presentation.category.components.DeleteCategoryDialog
import com.example.myapplication.presentation.category.components.DraggableCategoryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.error) {
        state.error?.let { error ->
            // TODO: 显示错误提示
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("分类管理") },
                actions = {
                    IconButton(
                        onClick = { viewModel.onEvent(CategoryEvent.ShowAddDialog) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "添加分类"
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
            // 类型选择器
            TabRow(
                selectedTabIndex = if (state.selectedType == TransactionType.EXPENSE) 0 else 1
            ) {
                Tab(
                    selected = state.selectedType == TransactionType.EXPENSE,
                    onClick = {
                        viewModel.onEvent(CategoryEvent.TypeChanged(TransactionType.EXPENSE))
                    }
                ) {
                    Text(
                        text = "支出",
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
                Tab(
                    selected = state.selectedType == TransactionType.INCOME,
                    onClick = {
                        viewModel.onEvent(CategoryEvent.TypeChanged(TransactionType.INCOME))
                    }
                ) {
                    Text(
                        text = "收入",
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }

            // 分类列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val categories = if (state.selectedType == TransactionType.EXPENSE) {
                    state.expenseCategories
                } else {
                    state.incomeCategories
                }

                itemsIndexed(
                    items = categories,
                    key = { _, category -> category.id }
                ) { index, category ->
                    DraggableCategoryItem(
                        category = category,
                        isDragging = state.isDragging && state.selectedCategory?.id == category.id,
                        onDragStart = {
                            viewModel.onEvent(CategoryEvent.StartDragging(category))
                        },
                        onDragEnd = {
                            viewModel.onEvent(CategoryEvent.StopDragging)
                        },
                        onEdit = {
                            viewModel.onEvent(CategoryEvent.CategorySelected(category))
                        },
                        onDelete = {
                            viewModel.onEvent(CategoryEvent.CategorySelected(category))
                            viewModel.onEvent(CategoryEvent.ShowDeleteDialog)
                        }
                    )
                }
            }
        }

        // 添加/编辑分类对话框
        if (state.isAddDialogVisible) {
            CategoryDialog(
                editingCategory = state.editingCategory,
                isEditing = state.isEditing,
                onNameChanged = { name ->
                    viewModel.onEvent(CategoryEvent.NameChanged(name))
                },
                onIconChanged = { icon ->
                    viewModel.onEvent(CategoryEvent.IconChanged(icon))
                },
                onColorChanged = { color ->
                    viewModel.onEvent(CategoryEvent.ColorChanged(color))
                },
                onDismiss = {
                    viewModel.onEvent(CategoryEvent.HideAddDialog)
                },
                onSave = {
                    viewModel.onEvent(CategoryEvent.SaveCategory)
                }
            )
        }

        // 删除确认对话框
        if (state.isDeleteDialogVisible) {
            state.selectedCategory?.let { category ->
                DeleteCategoryDialog(
                    category = category,
                    onDismiss = {
                        viewModel.onEvent(CategoryEvent.HideDeleteDialog)
                    },
                    onConfirm = {
                        viewModel.onEvent(CategoryEvent.DeleteCategory)
                    }
                )
            }
        }

        // 加载指示器
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
} 
package com.example.myapplication.presentation.category

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.myapplication.domain.model.Category
import com.example.myapplication.presentation.components.ColorPickerDialog
import com.example.myapplication.presentation.components.IconPickerDialog
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CategoryDetailScreen(
    categoryId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: CategoryDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showColorPicker by remember { mutableStateOf(false) }
    var showIconPicker by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(categoryId) {
        categoryId?.let { viewModel.loadCategory(it) }
    }

    LaunchedEffect(true) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is CategoryDetailEffect.NavigateBack -> onNavigateBack()
                is CategoryDetailEffect.ShowError -> {
                    // TODO: Show error dialog
                }
            }
        }
    }

    if (showColorPicker) {
        ColorPickerDialog(
            initialColor = state.category?.color ?: "#000000",
            onColorSelected = { color ->
                viewModel.onEvent(CategoryDetailEvent.UpdateColor(color))
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }

    if (showIconPicker) {
        IconPickerDialog(
            initialIcon = state.category?.icon ?: "category",
            onIconSelected = { icon ->
                viewModel.onEvent(CategoryDetailEvent.UpdateIcon(icon))
                showIconPicker = false
            },
            onDismiss = { showIconPicker = false }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除这个分类吗？这个操作不能撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(CategoryDetailEvent.DeleteCategory)
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
                        text = if (categoryId == null) {
                            stringResource(R.string.add_category)
                        } else {
                            stringResource(R.string.edit_category)
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (categoryId != null) {
                        IconButton(onClick = { showDeleteConfirmDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "删除")
                        }
                    }
                    IconButton(
                        onClick = { viewModel.onEvent(CategoryDetailEvent.SaveCategory) }
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
                // 基本信息
                OutlinedTextField(
                    value = state.name,
                    onValueChange = { viewModel.onEvent(CategoryDetailEvent.UpdateName(it)) },
                    label = { Text("名称") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = state.note,
                    onValueChange = { viewModel.onEvent(CategoryDetailEvent.UpdateNote(it)) },
                    label = { Text("备注") },
                    modifier = Modifier.fillMaxWidth()
                )

                // 类型选择
                Text(
                    text = "类型",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.type == CategoryType.EXPENSE,
                        onClick = { viewModel.onEvent(CategoryDetailEvent.UpdateType(CategoryType.EXPENSE)) },
                        label = { Text("支出") }
                    )
                    FilterChip(
                        selected = state.type == CategoryType.INCOME,
                        onClick = { viewModel.onEvent(CategoryDetailEvent.UpdateType(CategoryType.INCOME)) },
                        label = { Text("收入") }
                    )
                }

                // 图标和颜色
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedCard(
                        onClick = { showIconPicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("选择图标")
                        }
                    }

                    OutlinedCard(
                        onClick = { showColorPicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = Color(android.graphics.Color.parseColor(state.color)),
                                modifier = Modifier.size(24.dp)
                            ) {}
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("选择颜色")
                        }
                    }
                }

                // 预算设置
                if (state.type == CategoryType.EXPENSE) {
                    OutlinedTextField(
                        value = state.budgetAmount?.toString() ?: "",
                        onValueChange = { value ->
                            value.toDoubleOrNull()?.let {
                                viewModel.onEvent(CategoryDetailEvent.UpdateBudget(it))
                            }
                        },
                        label = { Text("预算金额") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
} 
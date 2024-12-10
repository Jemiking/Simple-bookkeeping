package com.example.myapplication.presentation.category.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.presentation.category.EditingCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDialog(
    editingCategory: EditingCategory,
    isEditing: Boolean,
    onNameChanged: (String) -> Unit,
    onIconChanged: (String) -> Unit,
    onColorChanged: (Int) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 标题栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) "编辑分类" else "添加分类",
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 分类名称输入
                OutlinedTextField(
                    value = editingCategory.name,
                    onValueChange = onNameChanged,
                    label = { Text("分类名称") },
                    isError = editingCategory.nameError != null,
                    supportingText = editingCategory.nameError?.let { 
                        { Text(it) }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 图标选择
                Text(
                    text = "选择图标",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (editingCategory.iconError != null) {
                    Text(
                        text = editingCategory.iconError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    items(defaultIcons) { icon ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = if (icon == editingCategory.icon) {
                                        Color(editingCategory.color)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    shape = MaterialTheme.shapes.small
                                )
                                .clickable { onIconChanged(icon) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = icon,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (icon == editingCategory.icon) {
                                    MaterialTheme.colorScheme.surface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 颜色选择
                Text(
                    text = "选择颜色",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(100.dp)
                ) {
                    items(defaultColors) { color ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = Color(color),
                                    shape = MaterialTheme.shapes.small
                                )
                                .clickable { onColorChanged(color) }
                        ) {
                            if (color == editingCategory.color) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onSave) {
                        Text(if (isEditing) "保存" else "添加")
                    }
                }
            }
        }
    }
}

private val defaultIcons = listOf(
    "🛒", "🍔", "🍜", "🚌", "🏠", "💊",
    "👕", "📱", "💄", "🎮", "📚", "🎬",
    "🏃", "🎨", "🎵", "✈️", "🎁", "💰",
    "💳", "🏦", "💼", "📊", "🎓", "⚡️"
)

private val defaultColors = listOf(
    0xFFF44336, // Red
    0xFFE91E63, // Pink
    0xFF9C27B0, // Purple
    0xFF673AB7, // Deep Purple
    0xFF3F51B5, // Indigo
    0xFF2196F3, // Blue
    0xFF03A9F4, // Light Blue
    0xFF00BCD4, // Cyan
    0xFF009688, // Teal
    0xFF4CAF50, // Green
    0xFF8BC34A, // Light Green
    0xFFCDDC39, // Lime
    0xFFFFEB3B, // Yellow
    0xFFFFC107, // Amber
    0xFFFF9800, // Orange
    0xFFFF5722, // Deep Orange
    0xFF795548, // Brown
    0xFF607D8B  // Blue Grey
) 
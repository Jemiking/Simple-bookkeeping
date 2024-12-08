package com.example.myapplication.presentation.category.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.model.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraggableCategoryItem(
    category: Category,
    isDragging: Boolean,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        label = "CategoryItemOffset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { onDragStart() },
                    onDragEnd = {
                        onDragEnd()
                        offsetX = 0f
                    },
                    onDragCancel = {
                        onDragEnd()
                        offsetX = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        offsetX = (offsetX + dragAmount).coerceIn(-200f, 0f)
                    }
                )
            }
    ) {
        // 滑动操作按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onEdit,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "编辑分类"
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onDelete,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除分类"
                )
            }
        }

        // 主要内容
        Row(
            modifier = Modifier
                .offset { androidx.compose.ui.unit.IntOffset(animatedOffset.toInt(), 0) }
                .fillMaxWidth()
                .height(64.dp)
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 分类图标
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = Color(category.color).copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.small
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category.icon,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(category.color)
                    )
                }

                // 分类名称
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // 拖动手柄
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "拖动排序",
                tint = if (isDragging) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
} 
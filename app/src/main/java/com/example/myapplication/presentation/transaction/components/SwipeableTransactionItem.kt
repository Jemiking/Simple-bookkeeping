package com.example.myapplication.presentation.transaction.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.model.Transaction
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableTransactionItem(
    transaction: Transaction,
    onEdit: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    val swipeableState = rememberSwipeableState(initialValue = 0)
    val density = LocalDensity.current
    val actionSize = 72.dp
    val actionSizePx = with(density) { actionSize.toPx() }
    
    val anchors = mapOf(
        -actionSizePx * 2 to -2, // 删除
        -actionSizePx to -1,     // 编辑
        0f to 0,                 // 初始位置
    )

    // 动画值
    val scale by animateFloatAsState(
        targetValue = if (swipeableState.offset.value != 0f) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    // 计算动画值
    val editButtonAlpha = (swipeableState.offset.value / -actionSizePx).coerceIn(0f, 1f)
    val deleteButtonAlpha = ((swipeableState.offset.value + actionSizePx) / -actionSizePx).coerceIn(0f, 1f)
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
    ) {
        // 操作按钮背景
        Row(
            modifier = Modifier
                .matchParentSize()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 编辑按钮
            Box(
                modifier = Modifier
                    .size(actionSize)
                    .graphicsLayer(alpha = editButtonAlpha)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "编辑",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.scale(
                        0.8f + (editButtonAlpha * 0.2f)
                    )
                )
            }
            
            // 删除按钮
            Box(
                modifier = Modifier
                    .size(actionSize)
                    .graphicsLayer(alpha = deleteButtonAlpha)
                    .background(MaterialTheme.colorScheme.error)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.scale(
                        0.8f + (deleteButtonAlpha * 0.2f)
                    )
                )
            }
        }

        // 交易项内容
        Surface(
            modifier = Modifier
                .offset {
                    IntOffset(swipeableState.offset.value.roundToInt(), 0)
                }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .swipeable(
                    state = swipeableState,
                    anchors = anchors,
                    thresholds = { _, _ -> FractionalThreshold(0.3f) },
                    orientation = Orientation.Horizontal
                )
        ) {
            TransactionItem(
                transaction = transaction,
                onClick = { onEdit(transaction) }
            )
        }
    }

    // 处理滑动操作
    LaunchedEffect(swipeableState.currentValue) {
        when (swipeableState.currentValue) {
            -2 -> { // 删除
                onDelete(transaction)
                swipeableState.snapTo(0)
            }
            -1 -> { // 编辑
                onEdit(transaction)
                swipeableState.snapTo(0)
            }
        }
    }
} 
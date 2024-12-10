package com.example.myapplication.presentation.transaction.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.model.TransactionType
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.*

private val dateFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.CHINA)
private val cornerShape = RoundedCornerShape(12.dp)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeableTransactionItem(
    transaction: Transaction,
    onEdit: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit,
    enableAnimation: Boolean = true,
    modifier: Modifier = Modifier
) {
    val onDismissStateChange = remember(transaction) {
        { dismissValue: DismissValue ->
            when (dismissValue) {
                DismissValue.DismissedToStart -> {
                    onDelete(transaction)
                    true
                }
                DismissValue.DismissedToEnd -> {
                    onEdit(transaction)
                    true
                }
                DismissValue.Default -> false
            }
        }
    }

    val dismissState = rememberDismissState(
        confirmStateChange = onDismissStateChange
    )

    val dismissThreshold = remember { FractionalThreshold(0.3f) }

    SwipeToDismiss(
        state = dismissState,
        modifier = modifier,
        directions = setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart),
        dismissThresholds = { dismissThreshold },
        background = {
            SwipeBackground(
                dismissState = dismissState,
                enableAnimation = enableAnimation
            )
        }
    ) {
        TransactionCard(
            transaction = transaction,
            onClick = { onEdit(transaction) },
            enableAnimation = enableAnimation
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SwipeBackground(
    dismissState: DismissState,
    enableAnimation: Boolean,
    modifier: Modifier = Modifier
) {
    val direction = dismissState.dismissDirection
    val (color, icon, alignment, iconTint) = remember(direction) {
        when (direction) {
            DismissDirection.StartToEnd -> quadruple(
                MaterialTheme.colorScheme.primary,
                Icons.Default.Edit,
                Alignment.CenterStart,
                MaterialTheme.colorScheme.onPrimary
            )
            DismissDirection.EndToStart -> quadruple(
                MaterialTheme.colorScheme.error,
                Icons.Default.Delete,
                Alignment.CenterEnd,
                MaterialTheme.colorScheme.onError
            )
            null -> quadruple(
                Color.Transparent,
                null,
                Alignment.Center,
                Color.Transparent
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color)
            .padding(horizontal = 20.dp),
        contentAlignment = alignment
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun TransactionCard(
    transaction: Transaction,
    onClick: () -> Unit,
    enableAnimation: Boolean,
    modifier: Modifier = Modifier
) {
    val amount = remember(transaction.amount, transaction.type) {
        formatAmount(transaction.amount, transaction.type)
    }
    
    val date = remember(transaction.date) {
        transaction.date.format(dateFormatter)
    }

    val amountColor = remember(transaction.type) {
        when (transaction.type) {
            TransactionType.INCOME -> MaterialTheme.colorScheme.primary
            TransactionType.EXPENSE -> MaterialTheme.colorScheme.error
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = cornerShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧: 分类图标和名称
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                CategoryIcon(
                    icon = transaction.categoryIcon,
                    color = transaction.categoryColor,
                    enableAnimation = enableAnimation
                )
                
                Column {
                    Text(
                        text = transaction.categoryName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (transaction.note?.isNotBlank() == true) {
                        Text(
                            text = transaction.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // 右侧: 金额和时间
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = amount,
                    style = MaterialTheme.typography.titleMedium,
                    color = amountColor
                )
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CategoryIcon(
    icon: String,
    color: Int,
    enableAnimation: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = remember(color) {
        Color(color).copy(alpha = 0.1f)
    }
    
    val iconColor = remember(color) {
        Color(color)
    }

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.titleMedium,
            color = iconColor
        )
    }
}

private fun formatAmount(amount: Double, type: TransactionType): String {
    val symbol = when (type) {
        TransactionType.INCOME -> "+"
        TransactionType.EXPENSE -> "-"
    }
    return "$symbol${currencyFormatter.format(amount)}"
}

private data class SwipeBackgroundState(
    val color: Color,
    val icon: ImageVector?,
    val alignment: Alignment,
    val iconTint: Color
)

private fun <A, B, C, D> quadruple(a: A, b: B, c: C, d: D) = object {
    val first = a
    val second = b
    val third = c
    val fourth = d
} 
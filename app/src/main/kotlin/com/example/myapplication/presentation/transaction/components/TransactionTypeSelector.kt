package com.example.myapplication.presentation.transaction.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.local.entity.TransactionType

@Composable
fun TransactionTypeSelector(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TransactionType.values().forEach { type ->
            TypeItem(
                type = type,
                isSelected = type == selectedType,
                onClick = { onTypeSelected(type) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TypeItem(
    type: TransactionType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.surface
        } else {
            Color.Transparent
        },
        label = "TypeItemBackground"
    )

    val textColor by animateColorAsState(
        targetValue = when {
            !isSelected -> MaterialTheme.colorScheme.onSurfaceVariant
            type == TransactionType.EXPENSE -> MaterialTheme.colorScheme.error
            type == TransactionType.INCOME -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.tertiary
        },
        label = "TypeItemText"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(MaterialTheme.shapes.small)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (type) {
                TransactionType.EXPENSE -> "支出"
                TransactionType.INCOME -> "收入"
                TransactionType.TRANSFER -> "转账"
            },
            style = MaterialTheme.typography.titleSmall,
            color = textColor
        )
    }
} 
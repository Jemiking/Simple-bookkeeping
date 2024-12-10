package com.example.myapplication.presentation.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.local.entity.TransactionType
import com.example.myapplication.domain.model.Transaction
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TransactionItem(
    transaction: Transaction,
    onClick: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        onClick = { onClick(transaction) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 分类图标
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Color(transaction.categoryColor).copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = transaction.categoryIcon,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // 交易信息
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                // 分类名称和备注
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = transaction.categoryName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // 金额
                    Text(
                        text = formatAmount(transaction.amount, transaction.type),
                        style = MaterialTheme.typography.bodyLarge,
                        color = when (transaction.type) {
                            TransactionType.EXPENSE -> MaterialTheme.colorScheme.error
                            TransactionType.INCOME -> MaterialTheme.colorScheme.primary
                            TransactionType.TRANSFER -> MaterialTheme.colorScheme.tertiary
                        }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 账户和时间
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 账户名称
                        Text(
                            text = transaction.accountName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        // 备注
                        transaction.note?.let { note ->
                            Text(
                                text = "· $note",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // 时间
                    Text(
                        text = formatDate(transaction.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 右箭头
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "查看详情",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatAmount(amount: Double, type: TransactionType): String {
    val numberFormat = NumberFormat.getCurrencyInstance(Locale.CHINA)
    return when (type) {
        TransactionType.EXPENSE -> "-${numberFormat.format(amount)}"
        TransactionType.INCOME -> "+${numberFormat.format(amount)}"
        TransactionType.TRANSFER -> numberFormat.format(amount)
    }
}

private fun formatDate(dateTime: java.time.LocalDateTime): String {
    return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
} 
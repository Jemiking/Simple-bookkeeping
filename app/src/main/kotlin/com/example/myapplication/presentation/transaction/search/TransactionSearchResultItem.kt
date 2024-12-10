package com.example.myapplication.presentation.transaction.search

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.model.TransactionType
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionSearchResultItem(
    transaction: Transaction,
    searchQuery: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    animationDelay: Int = 0
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(true) {
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { 100 },
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = animationDelay,
                easing = FastOutSlowInEasing
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = animationDelay,
                easing = FastOutSlowInEasing
            )
        ),
        modifier = modifier
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧：类别图标和信息
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 类别图标
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(
                                when (transaction.type) {
                                    TransactionType.EXPENSE -> MaterialTheme.colorScheme.errorContainer
                                    TransactionType.INCOME -> MaterialTheme.colorScheme.primaryContainer
                                    TransactionType.TRANSFER -> MaterialTheme.colorScheme.secondaryContainer
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (transaction.type) {
                                TransactionType.EXPENSE -> Icons.Default.ArrowUpward
                                TransactionType.INCOME -> Icons.Default.ArrowDownward
                                TransactionType.TRANSFER -> Icons.Default.SwapHoriz
                            },
                            contentDescription = null,
                            tint = when (transaction.type) {
                                TransactionType.EXPENSE -> MaterialTheme.colorScheme.error
                                TransactionType.INCOME -> MaterialTheme.colorScheme.primary
                                TransactionType.TRANSFER -> MaterialTheme.colorScheme.secondary
                            }
                        )
                    }

                    // 交易信息
                    Column {
                        // 备注（高亮搜索关键词）
                        Text(
                            text = highlightSearchQuery(transaction.note, searchQuery),
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 日期
                            Text(
                                text = transaction.date.format(DateTimeFormatter.ofPattern("MM-dd HH:mm")),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            // 标签（高亮搜索关键词）
                            if (transaction.tags.isNotEmpty()) {
                                SuggestionChip(
                                    onClick = { },
                                    label = {
                                        Text(
                                            text = highlightSearchQuery(transaction.tags.first(), searchQuery),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    },
                                    modifier = Modifier.height(24.dp)
                                )
                            }
                            // 位置（高亮搜索关键词）
                            if (transaction.location != null) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = highlightSearchQuery(transaction.location, searchQuery),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            // 图片附件
                            if (transaction.images.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // 右侧：金额
                Text(
                    text = "${if (transaction.amount >= 0) "+" else ""}${String.format("%.2f", transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = when (transaction.type) {
                        TransactionType.EXPENSE -> MaterialTheme.colorScheme.error
                        TransactionType.INCOME -> MaterialTheme.colorScheme.primary
                        TransactionType.TRANSFER -> MaterialTheme.colorScheme.secondary
                    }
                )
            }
        }
    }
}

@Composable
private fun highlightSearchQuery(text: String, query: String): AnnotatedString {
    if (query.isBlank()) return AnnotatedString(text)

    return buildAnnotatedString {
        var lastIndex = 0
        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()
        var startIndex = lowerText.indexOf(lowerQuery)

        while (startIndex != -1) {
            // 添加未匹配的文本
            append(text.substring(lastIndex, startIndex))
            // 添加高亮的匹配文本
            pushStyle(
                SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    background = MaterialTheme.colorScheme.primaryContainer
                )
            )
            append(text.substring(startIndex, startIndex + query.length))
            pop()

            lastIndex = startIndex + query.length
            startIndex = lowerText.indexOf(lowerQuery, lastIndex)
        }

        // 添加剩余的文本
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
} 
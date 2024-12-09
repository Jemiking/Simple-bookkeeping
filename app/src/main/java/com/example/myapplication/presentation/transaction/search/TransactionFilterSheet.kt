package com.example.myapplication.presentation.transaction.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.model.TransactionType
import java.time.LocalDateTime

data class TransactionFilter(
    val types: Set<TransactionType> = setOf(),
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val categoryIds: Set<Long> = setOf(),
    val accountIds: Set<Long> = setOf(),
    val hasTags: Boolean? = null,
    val hasLocation: Boolean? = null,
    val hasImages: Boolean? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFilterSheet(
    filter: TransactionFilter,
    onFilterChange: (TransactionFilter) -> Unit,
    onDismiss: () -> Unit
) {
    var showDateRangePicker by remember { mutableStateOf(false) }
    var showAmountRangePicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showAccountPicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 标题
            Text(
                text = "高级筛选",
                style = MaterialTheme.typography.titleLarge
            )

            // 交易类型
            Column {
                Text(
                    text = "交易类型",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = TransactionType.EXPENSE in filter.types,
                        onClick = {
                            onFilterChange(
                                filter.copy(
                                    types = if (TransactionType.EXPENSE in filter.types) {
                                        filter.types - TransactionType.EXPENSE
                                    } else {
                                        filter.types + TransactionType.EXPENSE
                                    }
                                )
                            )
                        },
                        label = { Text("支出") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                    FilterChip(
                        selected = TransactionType.INCOME in filter.types,
                        onClick = {
                            onFilterChange(
                                filter.copy(
                                    types = if (TransactionType.INCOME in filter.types) {
                                        filter.types - TransactionType.INCOME
                                    } else {
                                        filter.types + TransactionType.INCOME
                                    }
                                )
                            )
                        },
                        label = { Text("收入") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                    FilterChip(
                        selected = TransactionType.TRANSFER in filter.types,
                        onClick = {
                            onFilterChange(
                                filter.copy(
                                    types = if (TransactionType.TRANSFER in filter.types) {
                                        filter.types - TransactionType.TRANSFER
                                    } else {
                                        filter.types + TransactionType.TRANSFER
                                    }
                                )
                            )
                        },
                        label = { Text("转账") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }

            // 日期范围
            OutlinedCard(
                onClick = { showDateRangePicker = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text("日期范围")
                    }
                    if (filter.startDate != null && filter.endDate != null) {
                        Text(
                            text = "${filter.startDate.toLocalDate()} ~ ${filter.endDate.toLocalDate()}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            text = "不限",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 金额范围
            OutlinedCard(
                onClick = { showAmountRangePicker = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text("金额范围")
                    }
                    if (filter.minAmount != null && filter.maxAmount != null) {
                        Text(
                            text = "${filter.minAmount} ~ ${filter.maxAmount}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            text = "不限",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 分类
            OutlinedCard(
                onClick = { showCategoryPicker = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text("分类")
                    }
                    if (filter.categoryIds.isNotEmpty()) {
                        Text(
                            text = "已选择 ${filter.categoryIds.size} 个",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            text = "不限",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 账户
            OutlinedCard(
                onClick = { showAccountPicker = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text("账户")
                    }
                    if (filter.accountIds.isNotEmpty()) {
                        Text(
                            text = "已选择 ${filter.accountIds.size} 个",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            text = "不限",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 其他条件
            Column {
                Text(
                    text = "其他条件",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filter.hasTags == true,
                        onClick = {
                            onFilterChange(
                                filter.copy(
                                    hasTags = if (filter.hasTags == true) null else true
                                )
                            )
                        },
                        label = { Text("有标签") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Label,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                    FilterChip(
                        selected = filter.hasLocation == true,
                        onClick = {
                            onFilterChange(
                                filter.copy(
                                    hasLocation = if (filter.hasLocation == true) null else true
                                )
                            )
                        },
                        label = { Text("有位置") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                    FilterChip(
                        selected = filter.hasImages == true,
                        onClick = {
                            onFilterChange(
                                filter.copy(
                                    hasImages = if (filter.hasImages == true) null else true
                                )
                            )
                        },
                        label = { Text("有图片") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }

            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onFilterChange(TransactionFilter())
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("重置")
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("确定")
                }
            }
        }
    }

    // TODO: 实现日期范围选择器
    if (showDateRangePicker) {
        // ...
    }

    // TODO: 实现金额范围选择器
    if (showAmountRangePicker) {
        // ...
    }

    // TODO: 实现分类选择器
    if (showCategoryPicker) {
        // ...
    }

    // TODO: 实现账户选择器
    if (showAccountPicker) {
        // ...
    }
} 
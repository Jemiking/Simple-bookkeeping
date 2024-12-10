package com.example.myapplication.presentation.budget.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.model.Budget
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetItem(
    budget: Budget,
    isSelected: Boolean,
    onBudgetClick: (Budget) -> Unit,
    onEditClick: (Budget) -> Unit,
    onDeleteClick: (Budget) -> Unit,
    modifier: Modifier = Modifier
) {
    val numberFormat = NumberFormat.getCurrencyInstance(Locale.CHINA)
    
    Card(
        modifier = modifier.clickable { onBudgetClick(budget) },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 标题和操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 标题
                Text(
                    text = budget.categoryName ?: "总预算",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                // 操作按钮
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { onEditClick(budget) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "编辑预算",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    IconButton(
                        onClick = { onDeleteClick(budget) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除预算",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 预算进度
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 进度条
                LinearProgressIndicator(
                    progress = (budget.spendingPercentage / 100).toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = when {
                        budget.isExceeded -> MaterialTheme.colorScheme.error
                        budget.spendingPercentage >= 80 -> MaterialTheme.colorScheme.error
                        budget.spendingPercentage >= 50 -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.primary
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                
                // 金额信息
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 已用金额
                    Text(
                        text = "已用：${numberFormat.format(budget.currentSpending)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // 预算金额
                    Text(
                        text = "预算：${numberFormat.format(budget.amount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // 剩余金额和百分比
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 剩余金额
                    Text(
                        text = "剩余：${numberFormat.format(budget.remainingAmount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (budget.isExceeded) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    
                    // 使用百分比
                    Text(
                        text = "使用率：%.1f%%".format(budget.spendingPercentage),
                        style = MaterialTheme.typography.bodyMedium,
                        color = when {
                            budget.isExceeded -> MaterialTheme.colorScheme.error
                            budget.spendingPercentage >= 80 -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            
            // 预算状态
            if (!budget.isEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "已禁用",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
} 
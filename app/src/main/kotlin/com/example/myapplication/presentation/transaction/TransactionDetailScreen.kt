package com.example.myapplication.presentation.transaction

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.model.TransactionType
import com.example.myapplication.presentation.transaction.components.TransactionListItem
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    viewModel: TransactionDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    var showShareDialog by remember { mutableStateOf(false) }

    // 手势处理
    var offsetX by remember { mutableStateOf(0f) }
    val swipeThreshold = with(LocalDensity.current) { 96.dp.toPx() }
    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    LaunchedEffect(state.deleteSuccess) {
        if (state.deleteSuccess) {
            snackbarMessage = "删除成功"
            showSnackbar = true
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("交易详情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showShareDialog = true }) {
                        Icon(Icons.Default.Share, contentDescription = "分享")
                    }
                    IconButton(onClick = { onNavigateToEdit(state.transaction?.id ?: 0) }) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "删除")
                    }
                }
            )
        },
        snackbarHost = {
            if (showSnackbar) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { showSnackbar = false }) {
                            Text("关闭")
                        }
                    }
                ) {
                    Text(snackbarMessage)
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX < -swipeThreshold) {
                                // 向左滑动超过阈值，执行编辑操作
                                offsetX = 0f
                                state.transaction?.let { onNavigateToEdit(it.id) }
                            } else if (offsetX > swipeThreshold) {
                                // 向右滑动超过阈值，返回上一页
                                offsetX = 0f
                                onNavigateBack()
                            } else {
                                // 未超过阈值，恢复原位
                                offsetX = 0f
                            }
                        },
                        onDragCancel = {
                            offsetX = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            offsetX = (offsetX + dragAmount).coerceIn(-swipeThreshold * 2, swipeThreshold * 2)
                        }
                    )
                }
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (state.error != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = state.error,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(
                        onClick = { viewModel.loadTransaction() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("重试")
                    }
                }
            } else {
                state.transaction?.let { transaction ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset { IntOffset(animatedOffset.toInt(), 0) }
                            .verticalScroll(scrollState)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 金额和类型
                        AmountSection(transaction)

                        // 基本信息
                        InfoSection(transaction)

                        // 附加信息
                        if (transaction.tags.isNotEmpty() || transaction.location != null || transaction.images.isNotEmpty()) {
                            ExtraSection(transaction)
                        }

                        // 相关交易
                        if (state.relatedTransactions.isNotEmpty()) {
                            RelatedTransactionsSection(
                                transactions = state.relatedTransactions,
                                onTransactionClick = onNavigateToDetail
                            )
                        }

                        // 操作按钮
                        ActionButtons(
                            onEdit = { onNavigateToEdit(transaction.id) },
                            onDelete = { showDeleteDialog = true }
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除这笔交易吗？此操作无法撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteTransaction()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (showShareDialog) {
        state.transaction?.let { transaction ->
            AlertDialog(
                onDismissRequest = { showShareDialog = false },
                title = { Text("分享交易") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("选择分享方式：")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ShareButton(
                                icon = Icons.Default.Message,
                                label = "短信",
                                onClick = {
                                    showShareDialog = false
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, generateShareText(transaction))
                                    }
                                    context.startActivity(Intent.createChooser(intent, "分享交易"))
                                }
                            )
                            ShareButton(
                                icon = Icons.Default.ContentCopy,
                                label = "复制",
                                onClick = {
                                    showShareDialog = false
                                    // TODO: 实现复制到剪贴板功能
                                    snackbarMessage = "已复制到剪贴板"
                                    showSnackbar = true
                                }
                            )
                            ShareButton(
                                icon = Icons.Default.Image,
                                label = "图片",
                                onClick = {
                                    showShareDialog = false
                                    // TODO: 实现生成分享图片功能
                                    snackbarMessage = "图片分享功能开发中"
                                    showSnackbar = true
                                }
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showShareDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}

@Composable
private fun AmountSection(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (transaction.type) {
                TransactionType.EXPENSE -> MaterialTheme.colorScheme.errorContainer
                TransactionType.INCOME -> MaterialTheme.colorScheme.primaryContainer
                TransactionType.TRANSFER -> MaterialTheme.colorScheme.secondaryContainer
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${if (transaction.amount >= 0) "+" else ""}${String.format("%.2f", transaction.amount)}",
                style = MaterialTheme.typography.headlineLarge,
                color = when (transaction.type) {
                    TransactionType.EXPENSE -> MaterialTheme.colorScheme.error
                    TransactionType.INCOME -> MaterialTheme.colorScheme.primary
                    TransactionType.TRANSFER -> MaterialTheme.colorScheme.secondary
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            when (transaction.type) {
                                TransactionType.EXPENSE -> MaterialTheme.colorScheme.error
                                TransactionType.INCOME -> MaterialTheme.colorScheme.primary
                                TransactionType.TRANSFER -> MaterialTheme.colorScheme.secondary
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
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (transaction.type) {
                        TransactionType.EXPENSE -> "支出"
                        TransactionType.INCOME -> "收入"
                        TransactionType.TRANSFER -> "转账"
                    },
                    style = MaterialTheme.typography.titleMedium,
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
private fun InfoSection(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InfoItem(
                icon = Icons.Default.Description,
                label = "备注",
                value = transaction.note
            )
            InfoItem(
                icon = Icons.Default.AccountBalance,
                label = "账户",
                value = "账户名称" // TODO: 从账户仓库获取账户名称
            )
            InfoItem(
                icon = Icons.Default.Category,
                label = "分类",
                value = "分类名称" // TODO: 从分类仓库获取分类名称
            )
            InfoItem(
                icon = Icons.Default.DateRange,
                label = "日期",
                value = transaction.date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm"))
            )
        }
    }
}

@Composable
private fun ExtraSection(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (transaction.tags.isNotEmpty()) {
                InfoItem(
                    icon = Icons.Default.Label,
                    label = "标签",
                    value = transaction.tags.joinToString(", ")
                )
            }
            if (transaction.location != null) {
                InfoItem(
                    icon = Icons.Default.LocationOn,
                    label = "位置",
                    value = transaction.location
                )
            }
            if (transaction.images.isNotEmpty()) {
                InfoItem(
                    icon = Icons.Default.Image,
                    label = "图片",
                    value = "${transaction.images.size}张图片"
                )
                // TODO: 显示图片网格
            }
        }
    }
}

@Composable
private fun InfoItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun ActionButtons(
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onEdit,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("编辑")
        }
        Button(
            onClick = onDelete,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("删除")
        }
    }
}

@Composable
private fun RelatedTransactionsSection(
    transactions: List<Transaction>,
    onTransactionClick: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "相关交易",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "共${transactions.size}笔",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            transactions.forEach { transaction ->
                TransactionListItem(
                    transaction = transaction,
                    onClick = { onTransactionClick(transaction.id) }
                )
            }
        }
    }
}

@Composable
private fun ShareButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun generateShareText(transaction: Transaction): String {
    val type = when (transaction.type) {
        TransactionType.EXPENSE -> "支出"
        TransactionType.INCOME -> "收入"
        TransactionType.TRANSFER -> "转账"
    }
    val amount = "${if (transaction.amount >= 0) "+" else ""}${String.format("%.2f", transaction.amount)}"
    val date = transaction.date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm"))
    val note = transaction.note.ifBlank { "无备注" }

    return buildString {
        appendLine("【记账】")
        appendLine("类型：$type")
        appendLine("金额：$amount")
        appendLine("日期：$date")
        appendLine("备注：$note")
        if (transaction.tags.isNotEmpty()) {
            appendLine("标签：${transaction.tags.joinToString(", ")}")
        }
        if (transaction.location != null) {
            appendLine("位置：${transaction.location}")
        }
        appendLine("\n分享自简单记账")
    }
} 
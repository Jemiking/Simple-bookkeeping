package com.example.myapplication.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.domain.model.*
import com.example.myapplication.presentation.components.TimePickerDialog

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showThemePicker by remember { mutableStateOf(false) }
    var showLanguagePicker by remember { mutableStateOf(false) }
    var showCurrencyPicker by remember { mutableStateOf(false) }
    var showStartDayPicker by remember { mutableStateOf(false) }
    var showBackupFrequencyPicker by remember { mutableStateOf(false) }
    var showBackupPathPicker by remember { mutableStateOf(false) }
    var showNotificationTimePicker by remember { mutableStateOf(false) }
    var showBudgetAlertThresholdPicker by remember { mutableStateOf(false) }
    var showClearDataConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(true) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SettingsEffect.ShowError -> {
                    // TODO: Show error dialog
                }
                is SettingsEffect.ShowMessage -> {
                    // TODO: Show snackbar
                }
            }
        }
    }

    if (showThemePicker) {
        AlertDialog(
            onDismissRequest = { showThemePicker = false },
            title = { Text("选择主题") },
            text = {
                Column {
                    Theme.values().forEach { theme ->
                        RadioButton(
                            selected = theme == state.theme,
                            onClick = {
                                viewModel.onEvent(SettingsEvent.UpdateTheme(theme))
                                showThemePicker = false
                            }
                        )
                        Text(
                            text = when (theme) {
                                Theme.LIGHT -> "浅色"
                                Theme.DARK -> "深色"
                                Theme.SYSTEM -> "跟随系统"
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemePicker = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (showLanguagePicker) {
        AlertDialog(
            onDismissRequest = { showLanguagePicker = false },
            title = { Text("选择语言") },
            text = {
                Column {
                    Language.values().forEach { language ->
                        RadioButton(
                            selected = language == state.language,
                            onClick = {
                                viewModel.onEvent(SettingsEvent.UpdateLanguage(language))
                                showLanguagePicker = false
                            }
                        )
                        Text(
                            text = when (language) {
                                Language.SYSTEM -> "跟随系统"
                                Language.CHINESE_SIMPLIFIED -> "简体中文"
                                Language.CHINESE_TRADITIONAL -> "繁体中文"
                                Language.ENGLISH -> "English"
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguagePicker = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (showCurrencyPicker) {
        AlertDialog(
            onDismissRequest = { showCurrencyPicker = false },
            title = { Text("选择货币") },
            text = {
                Column {
                    Currency.values().forEach { currency ->
                        RadioButton(
                            selected = currency == state.currency,
                            onClick = {
                                viewModel.onEvent(SettingsEvent.UpdateCurrency(currency))
                                showCurrencyPicker = false
                            }
                        )
                        Text(
                            text = when (currency) {
                                Currency.CNY -> "人民币 (¥)"
                                Currency.USD -> "美元 ($)"
                                Currency.EUR -> "欧元 (€)"
                                Currency.GBP -> "英镑 (£)"
                                Currency.JPY -> "日元 (¥)"
                                Currency.HKD -> "港币 (HK$)"
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCurrencyPicker = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (showStartDayPicker) {
        AlertDialog(
            onDismissRequest = { showStartDayPicker = false },
            title = { Text("选择记账起始日") },
            text = {
                Column {
                    (1..31).forEach { day ->
                        RadioButton(
                            selected = day == state.startDayOfMonth,
                            onClick = {
                                viewModel.onEvent(SettingsEvent.UpdateStartDayOfMonth(day))
                                showStartDayPicker = false
                            }
                        )
                        Text(text = "$day 日")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStartDayPicker = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (showBackupFrequencyPicker) {
        AlertDialog(
            onDismissRequest = { showBackupFrequencyPicker = false },
            title = { Text("选择备份频率") },
            text = {
                Column {
                    BackupFrequency.values().forEach { frequency ->
                        RadioButton(
                            selected = frequency == state.backupFrequency,
                            onClick = {
                                viewModel.onEvent(SettingsEvent.UpdateBackupFrequency(frequency))
                                showBackupFrequencyPicker = false
                            }
                        )
                        Text(
                            text = when (frequency) {
                                BackupFrequency.DAILY -> "每天"
                                BackupFrequency.WEEKLY -> "每周"
                                BackupFrequency.MONTHLY -> "每月"
                                BackupFrequency.NEVER -> "从不"
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBackupFrequencyPicker = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (showNotificationTimePicker) {
        TimePickerDialog(
            initialTime = state.notificationTime,
            onTimeSelected = { time ->
                viewModel.onEvent(SettingsEvent.UpdateNotificationTime(time))
                showNotificationTimePicker = false
            },
            onDismiss = { showNotificationTimePicker = false }
        )
    }

    if (showBudgetAlertThresholdPicker) {
        AlertDialog(
            onDismissRequest = { showBudgetAlertThresholdPicker = false },
            title = { Text("选择预算提醒阈值") },
            text = {
                Column {
                    (50..100 step 5).forEach { threshold ->
                        RadioButton(
                            selected = threshold == state.budgetAlertThreshold,
                            onClick = {
                                viewModel.onEvent(SettingsEvent.UpdateBudgetAlertThreshold(threshold))
                                showBudgetAlertThresholdPicker = false
                            }
                        )
                        Text(text = "$threshold%")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBudgetAlertThresholdPicker = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (showClearDataConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataConfirmDialog = false },
            title = { Text("确认清除数据") },
            text = { Text("确定要清除所有数据吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(SettingsEvent.ClearData)
                        showClearDataConfirmDialog = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataConfirmDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // 通用设置
        item {
            SettingsSection(title = "通用") {
                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = "主题",
                    subtitle = when (state.theme) {
                        Theme.LIGHT -> "浅色"
                        Theme.DARK -> "深色"
                        Theme.SYSTEM -> "跟随系统"
                    },
                    onClick = { showThemePicker = true }
                )

                SettingsItem(
                    icon = Icons.Default.Language,
                    title = "语言",
                    subtitle = when (state.language) {
                        Language.SYSTEM -> "跟随系统"
                        Language.CHINESE_SIMPLIFIED -> "简体中文"
                        Language.CHINESE_TRADITIONAL -> "繁体中文"
                        Language.ENGLISH -> "English"
                    },
                    onClick = { showLanguagePicker = true }
                )

                SettingsItem(
                    icon = Icons.Default.AttachMoney,
                    title = "货币",
                    subtitle = when (state.currency) {
                        Currency.CNY -> "人民币 (¥)"
                        Currency.USD -> "美元 ($)"
                        Currency.EUR -> "欧元 (€)"
                        Currency.GBP -> "英镑 (£)"
                        Currency.JPY -> "日元 (¥)"
                        Currency.HKD -> "港币 (HK$)"
                    },
                    onClick = { showCurrencyPicker = true }
                )

                SettingsItem(
                    icon = Icons.Default.DateRange,
                    title = "记账起始日",
                    subtitle = "${state.startDayOfMonth} 日",
                    onClick = { showStartDayPicker = true }
                )

                SettingsItem(
                    icon = Icons.Default.Numbers,
                    title = "显示小数",
                    subtitle = if (state.showDecimal) "开启" else "关闭",
                    onClick = {
                        viewModel.onEvent(SettingsEvent.UpdateShowDecimal(!state.showDecimal))
                    }
                )
            }
        }

        // 安全设置
        item {
            SettingsSection(title = "安全") {
                SettingsItem(
                    icon = Icons.Default.Fingerprint,
                    title = "生物识别",
                    subtitle = if (state.enableBiometric) "开启" else "关闭",
                    onClick = {
                        viewModel.onEvent(SettingsEvent.UpdateBiometric(!state.enableBiometric))
                    }
                )
            }
        }

        // 备份设置
        item {
            SettingsSection(title = "备份") {
                SettingsItem(
                    icon = Icons.Default.Backup,
                    title = "自动备份",
                    subtitle = if (state.enableBackup) "开启" else "关闭",
                    onClick = {
                        viewModel.onEvent(SettingsEvent.UpdateBackup(!state.enableBackup))
                    }
                )

                if (state.enableBackup) {
                    SettingsItem(
                        icon = Icons.Default.Schedule,
                        title = "备份频率",
                        subtitle = when (state.backupFrequency) {
                            BackupFrequency.DAILY -> "每天"
                            BackupFrequency.WEEKLY -> "每周"
                            BackupFrequency.MONTHLY -> "每月"
                            BackupFrequency.NEVER -> "从不"
                        },
                        onClick = { showBackupFrequencyPicker = true }
                    )

                    SettingsItem(
                        icon = Icons.Default.Folder,
                        title = "备份路径",
                        subtitle = state.backupPath.ifBlank { "未设置" },
                        onClick = { showBackupPathPicker = true }
                    )
                }
            }
        }

        // 通知设置
        item {
            SettingsSection(title = "通知") {
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "每日提醒",
                    subtitle = if (state.enableNotification) "开启" else "关闭",
                    onClick = {
                        viewModel.onEvent(SettingsEvent.UpdateNotification(!state.enableNotification))
                    }
                )

                if (state.enableNotification) {
                    SettingsItem(
                        icon = Icons.Default.Schedule,
                        title = "提醒时间",
                        subtitle = state.notificationTime,
                        onClick = { showNotificationTimePicker = true }
                    )
                }

                SettingsItem(
                    icon = Icons.Default.Warning,
                    title = "预算提醒",
                    subtitle = if (state.enableBudgetAlert) "开启" else "关闭",
                    onClick = {
                        viewModel.onEvent(SettingsEvent.UpdateBudgetAlert(!state.enableBudgetAlert))
                    }
                )

                if (state.enableBudgetAlert) {
                    SettingsItem(
                        icon = Icons.Default.Percent,
                        title = "提醒阈值",
                        subtitle = "${state.budgetAlertThreshold}%",
                        onClick = { showBudgetAlertThresholdPicker = true }
                    )
                }
            }
        }

        // 数据管理
        item {
            SettingsSection(title = "数据管理") {
                SettingsItem(
                    icon = Icons.Default.ImportExport,
                    title = "导出数据",
                    onClick = { viewModel.onEvent(SettingsEvent.ExportData) }
                )

                SettingsItem(
                    icon = Icons.Default.ImportExport,
                    title = "导入数据",
                    onClick = { viewModel.onEvent(SettingsEvent.ImportData) }
                )

                SettingsItem(
                    icon = Icons.Default.Delete,
                    title = "清除数据",
                    onClick = { showClearDataConfirmDialog = true }
                )
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
        content()
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
} 
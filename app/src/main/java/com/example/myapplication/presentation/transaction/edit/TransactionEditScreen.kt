package com.example.myapplication.presentation.transaction.edit

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Help
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.data.local.entity.TransactionType
import com.example.myapplication.presentation.components.*
import com.example.myapplication.presentation.utils.rememberKeyboardState
import com.example.myapplication.presentation.utils.rememberKeyboardHeight
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun TransactionEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: TransactionEditViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val validationState by viewModel.validationState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val amountFocusRequester = remember { FocusRequester() }
    val noteFocusRequester = remember { FocusRequester() }
    val keyboardVisible by rememberKeyboardState()
    val keyboardHeight by rememberKeyboardHeight()

    LaunchedEffect(true) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is TransactionEditEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                TransactionEditEffect.NavigateBack -> {
                    onNavigateBack()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (state.transactionId == null) "新建交易" else "编辑交易",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(TransactionEditEvent.ShowDeleteConfirmation) }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    IconButton(
                        onClick = { viewModel.onEvent(TransactionEditEvent.Submit) },
                        enabled = validationState.isValid
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "保存")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 金额输入卡片
                    GestureCard(
                        onDoubleTap = { 
                            viewModel.onEvent(TransactionEditEvent.ClearForm)
                        },
                        onSwipeLeft = { 
                            viewModel.onEvent(TransactionEditEvent.CancelEdit)
                        },
                        onSwipeRight = { 
                            viewModel.onEvent(TransactionEditEvent.QuickSave)
                        },
                        onLongPress = { 
                            viewModel.onEvent(TransactionEditEvent.ShowMoreOptions(true))
                        }
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
                                HelpTooltip(
                                    text = "输入交易金额，支持两位小数",
                                    isError = validationState.amountError != null
                                ) { showTooltip ->
                                    OutlinedTextField(
                                        value = state.amount,
                                        onValueChange = { viewModel.onEvent(TransactionEditEvent.AmountChanged(it)) },
                                        label = { Text("金额") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        prefix = { Text("¥") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(amountFocusRequester)
                                            .onFocusChanged { focusState ->
                                                if (focusState.isFocused) {
                                                    keyboardController?.show()
                                                }
                                            },
                                        singleLine = true,
                                        isError = validationState.amountError != null,
                                        supportingText = validationState.amountError?.let { { Text(it) } },
                                        trailingIcon = {
                                            IconButton(onClick = showTooltip) {
                                                Icon(Icons.Outlined.Help, "帮助")
                                            }
                                        }
                                    )
                                }

                                // 交易类型选择
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TransactionType.values().forEach { type ->
                                        FilterChip(
                                            selected = state.type == type,
                                            onClick = { 
                                                viewModel.onEvent(TransactionEditEvent.TypeChanged(type))
                                                keyboardController?.hide()
                                            },
                                            label = {
                                                Text(
                                                    when (type) {
                                                        TransactionType.EXPENSE -> "支出"
                                                        TransactionType.INCOME -> "收入"
                                                        TransactionType.TRANSFER -> "转账"
                                                    }
                                                )
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 账户选择卡片
                    GestureCard(
                        onDoubleTap = { /* 清空账户选择 */ },
                        onSwipeLeft = { /* 取消编辑 */ },
                        onSwipeRight = { /* 保存 */ },
                        onLongPress = { /* 显示更多选项 */ }
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
                                Text(
                                    text = "账户信息",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                HelpTooltip(
                                    text = "选择交易账户",
                                    isError = validationState.accountError != null
                                ) { showTooltip ->
                                    ExposedDropdownMenuBox(
                                        expanded = state.showAccountMenu,
                                        onExpandedChange = { expanded ->
                                            viewModel.onEvent(TransactionEditEvent.ShowAccountMenu(expanded))
                                            keyboardController?.hide()
                                        }
                                    ) {
                                        OutlinedTextField(
                                            value = state.accounts.find { it.id == state.accountId }?.name ?: "",
                                            onValueChange = { },
                                            readOnly = true,
                                            label = { Text("账户") },
                                            trailingIcon = {
                                                Row {
                                                    IconButton(onClick = showTooltip) {
                                                        Icon(Icons.Outlined.Help, "帮助")
                                                    }
                                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                                        expanded = state.showAccountMenu
                                                    )
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                                            isError = validationState.accountError != null,
                                            supportingText = validationState.accountError?.let { { Text(it) } }
                                        )

                                        ExposedDropdownMenu(
                                            expanded = state.showAccountMenu,
                                            onDismissRequest = {
                                                viewModel.onEvent(TransactionEditEvent.ShowAccountMenu(false))
                                            }
                                        ) {
                                            state.accounts.forEach { account ->
                                                DropdownMenuItem(
                                                    text = { Text(account.name) },
                                                    onClick = {
                                                        viewModel.onEvent(TransactionEditEvent.AccountSelected(account.id))
                                                        viewModel.onEvent(TransactionEditEvent.ShowAccountMenu(false))
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                // 转账目标账户
                                AnimatedFormSection(
                                    visible = state.type == TransactionType.TRANSFER,
                                    label = {
                                        Text(
                                            text = "转入账户",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                ) {
                                    HelpTooltip(
                                        text = "选择转入账户",
                                        isError = validationState.accountError != null
                                    ) { showTooltip ->
                                        ExposedDropdownMenuBox(
                                            expanded = state.showToAccountMenu,
                                            onExpandedChange = { expanded ->
                                                viewModel.onEvent(TransactionEditEvent.ShowToAccountMenu(expanded))
                                                keyboardController?.hide()
                                            }
                                        ) {
                                            OutlinedTextField(
                                                value = state.accounts.find { it.id == state.toAccountId }?.name ?: "",
                                                onValueChange = { },
                                                readOnly = true,
                                                label = { Text("转入账户") },
                                                trailingIcon = {
                                                    Row {
                                                        IconButton(onClick = showTooltip) {
                                                            Icon(Icons.Outlined.Help, "帮助")
                                                        }
                                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                                            expanded = state.showToAccountMenu
                                                        )
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                                isError = validationState.accountError != null,
                                                supportingText = validationState.accountError?.let { { Text(it) } }
                                            )

                                            ExposedDropdownMenu(
                                                expanded = state.showToAccountMenu,
                                                onDismissRequest = {
                                                    viewModel.onEvent(TransactionEditEvent.ShowToAccountMenu(false))
                                                }
                                            ) {
                                                state.accounts.forEach { account ->
                                                    if (account.id != state.accountId) {
                                                        DropdownMenuItem(
                                                            text = { Text(account.name) },
                                                            onClick = {
                                                                viewModel.onEvent(TransactionEditEvent.ToAccountSelected(account.id))
                                                                viewModel.onEvent(TransactionEditEvent.ShowToAccountMenu(false))
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 分类选择卡片
                    AnimatedFormSection(
                        visible = state.type != TransactionType.TRANSFER,
                        label = {
                            Text(
                                text = "分类信息",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    ) {
                        GestureCard(
                            onDoubleTap = { /* 清空分类选择 */ },
                            onSwipeLeft = { /* 取消编辑 */ },
                            onSwipeRight = { /* 保存 */ },
                            onLongPress = { /* 显��更多选项 */ }
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
                                    HelpTooltip(
                                        text = "选择交易分类",
                                        isError = validationState.categoryError != null
                                    ) { showTooltip ->
                                        ExposedDropdownMenuBox(
                                            expanded = state.showCategoryMenu,
                                            onExpandedChange = { expanded ->
                                                viewModel.onEvent(TransactionEditEvent.ShowCategoryMenu(expanded))
                                                keyboardController?.hide()
                                            }
                                        ) {
                                            OutlinedTextField(
                                                value = state.categories.find { it.id == state.categoryId }?.name ?: "",
                                                onValueChange = { },
                                                readOnly = true,
                                                label = { Text("分类") },
                                                trailingIcon = {
                                                    Row {
                                                        IconButton(onClick = showTooltip) {
                                                            Icon(Icons.Outlined.Help, "帮助")
                                                        }
                                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                                            expanded = state.showCategoryMenu
                                                        )
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                                isError = validationState.categoryError != null,
                                                supportingText = validationState.categoryError?.let { { Text(it) } }
                                            )

                                            ExposedDropdownMenu(
                                                expanded = state.showCategoryMenu,
                                                onDismissRequest = {
                                                    viewModel.onEvent(TransactionEditEvent.ShowCategoryMenu(false))
                                                }
                                            ) {
                                                state.categories.forEach { category ->
                                                    DropdownMenuItem(
                                                        text = { Text(category.name) },
                                                        onClick = {
                                                            viewModel.onEvent(TransactionEditEvent.CategorySelected(category.id))
                                                            viewModel.onEvent(TransactionEditEvent.ShowCategoryMenu(false))
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 日期和备注卡片
                    GestureCard(
                        onDoubleTap = { /* 清空备注 */ },
                        onSwipeLeft = { /* 取消编辑 */ },
                        onSwipeRight = { /* 保存 */ },
                        onLongPress = { /* 显示更多选项 */ }
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
                                Text(
                                    text = "其他信息",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                // 日期选择
                                OutlinedTextField(
                                    value = state.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                                    onValueChange = { },
                                    readOnly = true,
                                    label = { Text("日期") },
                                    trailingIcon = {
                                        Row {
                                            IconButton(
                                                onClick = { 
                                                    viewModel.onEvent(TransactionEditEvent.ShowDatePicker(true))
                                                    keyboardController?.hide()
                                                }
                                            ) {
                                                Icon(Icons.Default.CalendarMonth, contentDescription = "选择日期")
                                            }
                                            IconButton(
                                                onClick = { 
                                                    viewModel.onEvent(TransactionEditEvent.ShowTimePicker(true))
                                                    keyboardController?.hide()
                                                }
                                            ) {
                                                Icon(Icons.Default.Schedule, contentDescription = "选择时间")
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // 备注输入
                                HelpTooltip(
                                    text = "输入交易备注（可选）",
                                    isError = validationState.noteError != null
                                ) { showTooltip ->
                                    OutlinedTextField(
                                        value = state.note,
                                        onValueChange = { viewModel.onEvent(TransactionEditEvent.NoteChanged(it)) },
                                        label = { Text("备注") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(noteFocusRequester)
                                            .onFocusChanged { focusState ->
                                                if (focusState.isFocused) {
                                                    keyboardController?.show()
                                                }
                                            },
                                        minLines = 3,
                                        isError = validationState.noteError != null,
                                        supportingText = validationState.noteError?.let { { Text(it) } },
                                        trailingIcon = {
                                            IconButton(onClick = showTooltip) {
                                                Icon(Icons.Outlined.Help, "帮助")
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 保存按钮
                    Button(
                        onClick = { 
                            keyboardController?.hide()
                            viewModel.onEvent(TransactionEditEvent.Submit)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = validationState.isValid
                    ) {
                        Text("保存")
                    }
                }
            }
        }
    }

    // 日期选择器
    if (state.showDatePicker) {
        MonthYearPicker(
            initialYearMonth = state.date.toLocalDate().toYearMonth(),
            onYearMonthSelected = { yearMonth ->
                val newDate = state.date
                    .withYear(yearMonth.year)
                    .withMonth(yearMonth.monthValue)
                viewModel.onEvent(TransactionEditEvent.DateChanged(newDate))
                viewModel.onEvent(TransactionEditEvent.ShowDatePicker(false))
            },
            onDismiss = { viewModel.onEvent(TransactionEditEvent.ShowDatePicker(false)) }
        )
    }

    // 时��选择器
    if (state.showTimePicker) {
        TimePickerDialog(
            initialTime = state.date.format(DateTimeFormatter.ofPattern("HH:mm")),
            onTimeSelected = { time ->
                val (hour, minute) = time.split(":").map { it.toInt() }
                val newDate = state.date
                    .withHour(hour)
                    .withMinute(minute)
                viewModel.onEvent(TransactionEditEvent.DateChanged(newDate))
                viewModel.onEvent(TransactionEditEvent.ShowTimePicker(false))
            },
            onDismiss = { viewModel.onEvent(TransactionEditEvent.ShowTimePicker(false)) }
        )
    }

    // 删除确认对话框
    if (state.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = {
                viewModel.onEvent(TransactionEditEvent.HideDeleteConfirmation)
            },
            title = { Text("确认删除") },
            text = { Text("确定要删除这条交易记录吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(TransactionEditEvent.Delete)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(TransactionEditEvent.HideDeleteConfirmation)
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }

    // 添加更多选项菜单
    if (state.showMoreOptions) {
        AlertDialog(
            onDismissRequest = {
                viewModel.onEvent(TransactionEditEvent.ShowMoreOptions(false))
            },
            title = { Text("更多选项") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = { 
                            viewModel.onEvent(TransactionEditEvent.ClearForm)
                            viewModel.onEvent(TransactionEditEvent.ShowMoreOptions(false))
                        }
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "清空")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("清空表单")
                    }
                    TextButton(
                        onClick = { 
                            viewModel.onEvent(TransactionEditEvent.QuickSave)
                            viewModel.onEvent(TransactionEditEvent.ShowMoreOptions(false))
                        },
                        enabled = validationState.isValid
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "保存")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("快速保存")
                    }
                    TextButton(
                        onClick = { 
                            viewModel.onEvent(TransactionEditEvent.CancelEdit)
                            viewModel.onEvent(TransactionEditEvent.ShowMoreOptions(false))
                        }
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "取消")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("取消编辑")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(TransactionEditEvent.ShowMoreOptions(false))
                    }
                ) {
                    Text("关闭")
                }
            }
        )
    }

    // 添加动画效果收集器
    LaunchedEffect(true) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is TransactionEditEffect.ShowSaveAnimation -> {
                    var showAnimation by remember { mutableStateOf(true) }
                    if (showAnimation) {
                        SaveAnimation {
                            showAnimation = false
                        }
                    }
                }
                is TransactionEditEffect.ShowCancelAnimation -> {
                    var showAnimation by remember { mutableStateOf(true) }
                    if (showAnimation) {
                        CancelAnimation {
                            showAnimation = false
                        }
                    }
                }
                is TransactionEditEffect.ShowClearAnimation -> {
                    var showAnimation by remember { mutableStateOf(true) }
                    if (showAnimation) {
                        ClearAnimation {
                            showAnimation = false
                        }
                    }
                }
                is TransactionEditEffect.ShowFieldAnimation -> {
                    var showAnimation by remember { mutableStateOf(true) }
                    if (showAnimation) {
                        FieldAnimation(
                            field = effect.field
                        ) {
                            showAnimation = false
                        }
                    }
                }
                else -> { /* 处理其他效果 */ }
            }
        }
    }

    // 添加键盘处理
    LaunchedEffect(keyboardVisible) {
        if (keyboardVisible) {
            // 滚动到当前输入框位置
            when (state.activeField) {
                "amount" -> amountFocusRequester.requestFocus()
                "note" -> noteFocusRequester.requestFocus()
            }
        }
    }

    // 添加底部间距以避免键盘遮挡
    Spacer(
        modifier = Modifier.height(
            with(LocalDensity.current) { keyboardHeight.toDp() }
        )
    )
} 
package com.example.myapplication.presentation.account

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.data.local.entity.AccountType
import com.example.myapplication.domain.model.Account
import java.text.NumberFormat
import java.util.*
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountListScreen(
    onNavigateToAddAccount: () -> Unit,
    onNavigateToAccountDetail: (Long) -> Unit,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val numberFormat = remember { NumberFormat.getCurrencyInstance(Locale.CHINA) }
    
    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf<AccountType?>(null) }

    Scaffold(
        topBar = {
            if (isSearchVisible) {
                SearchTopBar(
                    query = searchQuery,
                    onQueryChange = { query ->
                        searchQuery = query
                        viewModel.onEvent(AccountEvent.Search(query))
                    },
                    onCloseClick = {
                        isSearchVisible = false
                        searchQuery = ""
                        viewModel.onEvent(AccountEvent.Search(""))
                    },
                    focusRequester = focusRequester
                )
            } else {
                TopAppBar(
                    title = { Text("账户") },
                    actions = {
                        // 搜索按钮
                        IconButton(
                            onClick = {
                                isSearchVisible = true
                                kotlinx.coroutines.MainScope().launch {
                                    kotlinx.coroutines.delay(300)
                                    focusRequester.requestFocus()
                                }
                            }
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "搜索")
                        }
                        // 筛选按钮
                        IconButton(onClick = { showFilterDialog = true }) {
                            Badge(
                                modifier = Modifier.padding(4.dp),
                                content = { },
                                containerColor = MaterialTheme.colorScheme.primary,
                            ) {
                                Icon(
                                    Icons.Default.FilterList,
                                    contentDescription = "筛选",
                                    tint = if (selectedType != null) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddAccount) {
                Icon(Icons.Default.Add, contentDescription = "添加账户")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading && state.accounts.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.error != null && state.accounts.isEmpty() -> {
                    Text(
                        text = state.error ?: "未知错误",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.accounts.isEmpty() -> {
                    EmptyAccountList(
                        modifier = Modifier.align(Alignment.Center),
                        onAddClick = onNavigateToAddAccount
                    )
                }
                else -> {
                    SwipeRefresh(
                        state = rememberSwipeRefreshState(state.isLoading),
                        onRefresh = { viewModel.onEvent(AccountEvent.Refresh) }
                    ) {
                        AccountList(
                            accounts = state.accounts,
                            onAccountClick = onNavigateToAccountDetail,
                            onDeleteAccount = { account ->
                                viewModel.onEvent(AccountEvent.DeleteAccount(account.id))
                            },
                            numberFormat = numberFormat
                        )
                    }
                }
            }
        }

        if (showFilterDialog) {
            FilterDialog(
                selectedType = selectedType,
                onTypeSelected = { type ->
                    selectedType = type
                    viewModel.onEvent(AccountEvent.Filter(type))
                },
                onDismiss = { showFilterDialog = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onCloseClick: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("搜索账户") },
                singleLine = true,
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
        },
        navigationIcon = {
            IconButton(onClick = onCloseClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "返回")
            }
        },
        actions = {
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = {
                        onQueryChange("")
                    }
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "清除")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun AccountList(
    accounts: List<Account>,
    onAccountClick: (Long) -> Unit,
    onDeleteAccount: (Account) -> Unit,
    numberFormat: NumberFormat,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // 总资产卡片
        TotalAssetsCard(
            totalAssets = accounts.sumOf { it.balance },
            numberFormat = numberFormat
        )

        // 账户列表
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = accounts,
                key = { it.id } // 为了正确的动画效果
            ) { account ->
                val dismissState = rememberDismissState(
                    confirmStateChange = { dismissValue ->
                        if (dismissValue == DismissValue.DismissedToStart) {
                            onDeleteAccount(account)
                            true
                        } else {
                            false
                        }
                    }
                )

                SwipeToDismiss(
                    state = dismissState,
                    directions = setOf(DismissDirection.EndToStart),
                    background = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "删除",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                ) {
                    AccountCard(
                        account = account,
                        numberFormat = numberFormat,
                        onClick = { onAccountClick(account.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountCard(
    account: Account,
    numberFormat: NumberFormat,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 账户信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = account.type.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 余额
            Text(
                text = numberFormat.format(account.balance),
                style = MaterialTheme.typography.titleMedium,
                color = if (account.balance >= 0) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }
    }
}

@Composable
private fun TotalAssetsCard(
    totalAssets: Double,
    numberFormat: NumberFormat,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "总资产",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = numberFormat.format(totalAssets),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun EmptyAccountList(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.AccountBalance,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "没有账户",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "点击下方按钮添加账户",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onAddClick) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("添加账户")
        }
    }
}

@Composable
private fun FilterDialog(
    selectedType: AccountType?,
    onTypeSelected: (AccountType?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择账户类型") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 全部类型选项
                FilterChip(
                    selected = selectedType == null,
                    onClick = { onTypeSelected(null) },
                    label = { Text("全部") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // 各种账户类型
                AccountType.values().forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { onTypeSelected(type) },
                        label = { 
                            Text(
                                when (type) {
                                    AccountType.CASH -> "现金"
                                    AccountType.BANK_CARD -> "银行卡"
                                    AccountType.CREDIT_CARD -> "信用卡"
                                    AccountType.ALIPAY -> "支付宝"
                                    AccountType.WECHAT -> "微信"
                                    AccountType.OTHER -> "其他"
                                }
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = when (type) {
                                    AccountType.CASH -> Icons.Default.Money
                                    AccountType.BANK_CARD -> Icons.Default.CreditCard
                                    AccountType.CREDIT_CARD -> Icons.Default.CreditScore
                                    AccountType.ALIPAY -> Icons.Default.Payment
                                    AccountType.WECHAT -> Icons.Default.Message
                                    AccountType.OTHER -> Icons.Default.AccountBalance
                                },
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
} 
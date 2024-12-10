package com.example.myapplication.presentation.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.R
import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.model.AccountType
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountListScreen(
    onNavigateToAddAccount: () -> Unit,
    onNavigateToAccountDetail: (Long) -> Unit,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var accountToDelete by remember { mutableStateOf<Account?>(null) }
    var showTypeFilter by remember { mutableStateOf(false) }

    LaunchedEffect(true) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AccountEffect.AccountDeleted -> {
                    showDeleteDialog = false
                    accountToDelete = null
                }
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.accounts)) },
                actions = {
                    IconButton(onClick = { showTypeFilter = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = stringResource(R.string.filter))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddAccount,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_account))
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Total Balance
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
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
                                text = stringResource(R.string.total_balance),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = state.totalBalance.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Account List
                    if (state.accounts.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_accounts),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = state.accounts,
                                key = { it.id }
                            ) { account ->
                                AccountItem(
                                    account = account,
                                    onClick = { onNavigateToAccountDetail(account.id) },
                                    onDelete = {
                                        accountToDelete = account
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    accountToDelete = null
                },
                title = { Text(stringResource(R.string.delete_account)) },
                text = { Text(stringResource(R.string.delete_account_confirmation)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            accountToDelete?.let { account ->
                                viewModel.onEvent(AccountEvent.DeleteAccount(account))
                            }
                        }
                    ) {
                        Text(stringResource(R.string.delete))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            accountToDelete = null
                        }
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }

        if (showTypeFilter) {
            AlertDialog(
                onDismissRequest = { showTypeFilter = false },
                title = { Text(stringResource(R.string.filter_by_type)) },
                text = {
                    Column {
                        AccountType.values().forEach { type ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = state.selectedType == type,
                                    onClick = {
                                        viewModel.onEvent(AccountEvent.FilterByType(type))
                                        showTypeFilter = false
                                    }
                                )
                                Text(
                                    text = type.name,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                        // Add "All" option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = state.selectedType == null,
                                onClick = {
                                    viewModel.onEvent(AccountEvent.FilterByType(null))
                                    showTypeFilter = false
                                }
                            )
                            Text(
                                text = stringResource(R.string.all_accounts),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                },
                confirmButton = {}
            )
        }
    }
} 
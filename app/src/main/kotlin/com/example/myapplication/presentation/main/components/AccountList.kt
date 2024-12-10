package com.example.myapplication.presentation.main.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.model.Account

@Composable
fun AccountList(
    accounts: List<Account>,
    selectedAccountId: Long?,
    onAccountSelected: (Account) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // 标题
        Text(
            text = "我的账户",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // 账户列表
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = accounts,
                key = { it.id }
            ) { account ->
                AccountItem(
                    account = account,
                    isSelected = account.id == selectedAccountId,
                    onAccountClick = onAccountSelected,
                    modifier = Modifier.width(280.dp)
                )
            }
        }
    }
} 
package com.example.myapplication.presentation.transaction.edit.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.model.TransactionType
import com.example.myapplication.presentation.components.FormSection
import java.time.LocalDateTime

@Composable
fun TransactionForm(
    type: TransactionType,
    amount: String,
    note: String,
    date: LocalDateTime,
    fromAccount: Account?,
    toAccount: Account?,
    category: Category?,
    accounts: List<Account>,
    categories: List<Category>,
    onTypeChange: (TransactionType) -> Unit,
    onAmountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onDateChange: (LocalDateTime) -> Unit,
    onFromAccountChange: (Account) -> Unit,
    onToAccountChange: (Account) -> Unit,
    onCategoryChange: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 类型选择
        TransactionTypeSelector(
            selected = type,
            onTypeSelected = onTypeChange
        )

        // 金额输入
        AmountInput(
            amount = amount,
            onAmountChange = onAmountChange
        )

        // 账户选择
        AccountSection(
            type = type,
            fromAccount = fromAccount,
            toAccount = toAccount,
            accounts = accounts,
            onFromAccountChange = onFromAccountChange,
            onToAccountChange = onToAccountChange
        )

        // 分类选择
        AnimatedVisibility(
            visible = type != TransactionType.TRANSFER,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            CategorySection(
                category = category,
                categories = categories,
                onCategoryChange = onCategoryChange
            )
        }

        // 日期选择
        DateTimeSection(
            date = date,
            onDateChange = onDateChange
        )

        // 备注输入
        NoteInput(
            note = note,
            onNoteChange = onNoteChange
        )
    }
}

@Composable
private fun TransactionTypeSelector(
    selected: TransactionType,
    onTypeSelected: (TransactionType) -> Unit,
    modifier: Modifier = Modifier
) {
    FormSection(
        title = "交易类型",
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TransactionType.values().forEach { type ->
                FilterChip(
                    selected = type == selected,
                    onClick = { onTypeSelected(type) },
                    label = {
                        Text(
                            text = when (type) {
                                TransactionType.INCOME -> "收入"
                                TransactionType.EXPENSE -> "支出"
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

@Composable
private fun AmountInput(
    amount: String,
    onAmountChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FormSection(
        title = "金额",
        modifier = modifier
    ) {
        OutlinedTextField(
            value = amount,
            onValueChange = { value ->
                // 只允许输入数字和小数点
                if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
                    onAmountChange(value)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            singleLine = true
        )
    }
}

@Composable
private fun AccountSection(
    type: TransactionType,
    fromAccount: Account?,
    toAccount: Account?,
    accounts: List<Account>,
    onFromAccountChange: (Account) -> Unit,
    onToAccountChange: (Account) -> Unit,
    modifier: Modifier = Modifier
) {
    FormSection(
        title = "账户",
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 来源账户
            AccountSelector(
                label = if (type == TransactionType.TRANSFER) "转出账户" else "账户",
                selected = fromAccount,
                accounts = accounts,
                onAccountSelected = onFromAccountChange
            )

            // 目标账户(仅转账时显示)
            AnimatedVisibility(
                visible = type == TransactionType.TRANSFER,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                AccountSelector(
                    label = "转入账户",
                    selected = toAccount,
                    accounts = accounts.filter { it.id != fromAccount?.id },
                    onAccountSelected = onToAccountChange
                )
            }
        }
    }
}

@Composable
private fun AccountSelector(
    label: String,
    selected: Account?,
    accounts: List<Account>,
    onAccountSelected: (Account) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            accounts.forEach { account ->
                DropdownMenuItem(
                    text = { Text(account.name) },
                    onClick = {
                        onAccountSelected(account)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun CategorySection(
    category: Category?,
    categories: List<Category>,
    onCategoryChange: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    FormSection(
        title = "分类",
        modifier = modifier
    ) {
        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = category?.name ?: "",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name) },
                        onClick = {
                            onCategoryChange(category)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DateTimeSection(
    date: LocalDateTime,
    onDateChange: (LocalDateTime) -> Unit,
    modifier: Modifier = Modifier
) {
    FormSection(
        title = "日期和时间",
        modifier = modifier
    ) {
        // TODO: 实现日期时间选择器
        Text(date.toString())
    }
}

@Composable
private fun NoteInput(
    note: String,
    onNoteChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FormSection(
        title = "备注",
        modifier = modifier
    ) {
        OutlinedTextField(
            value = note,
            onValueChange = onNoteChange,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            maxLines = 3
        )
    }
} 
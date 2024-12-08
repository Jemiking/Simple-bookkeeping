package com.example.myapplication.presentation.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.local.entity.AccountType
import com.example.myapplication.domain.model.Account
import java.text.NumberFormat
import java.util.*

@Composable
fun AccountItem(
    account: Account,
    isSelected: Boolean,
    onAccountClick: (Account) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onAccountClick(account) },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 账户图标和名称
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 账户图标
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = Color(account.color).copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.small
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getAccountIcon(account.type),
                        contentDescription = account.name,
                        tint = Color(account.color)
                    )
                }

                // 账户名称和类型
                Column {
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = getAccountTypeText(account.type),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            // 账户余额
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatAmount(account.balance),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                
                if (account.monthlyBalance != 0.0) {
                    Text(
                        text = "本月${if (account.monthlyBalance > 0) "+" else ""}${formatAmount(account.monthlyBalance)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (account.monthlyBalance >= 0)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

private fun getAccountIcon(type: AccountType): ImageVector {
    return when (type) {
        AccountType.CASH -> Icons.Default.Money
        AccountType.BANK_CARD -> Icons.Default.CreditCard
        AccountType.CREDIT_CARD -> Icons.Default.Payment
        AccountType.ALIPAY -> Icons.Default.AccountBalanceWallet
        AccountType.WECHAT -> Icons.Default.Message
        AccountType.OTHER -> Icons.Default.AccountBalance
    }
}

private fun getAccountTypeText(type: AccountType): String {
    return when (type) {
        AccountType.CASH -> "现金"
        AccountType.BANK_CARD -> "储蓄卡"
        AccountType.CREDIT_CARD -> "信用卡"
        AccountType.ALIPAY -> "支付宝"
        AccountType.WECHAT -> "微信"
        AccountType.OTHER -> "其他"
    }
}

private fun formatAmount(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.CHINA)
    return format.format(amount)
} 
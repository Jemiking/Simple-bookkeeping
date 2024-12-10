package com.example.myapplication.presentation.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun OverviewCard(
    totalBalance: Double,
    monthlyIncome: Double,
    monthlyExpense: Double,
    monthlyBalance: Double,
    selectedMonth: YearMonth,
    onMonthSelected: (YearMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 总资产
            Text(
                text = "总资产",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = formatAmount(totalBalance),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 月度收支
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MonthPicker(
                    selectedMonth = selectedMonth,
                    onMonthSelected = onMonthSelected
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 收支详情
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 收入
                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "收入",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = formatAmount(monthlyIncome),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // 支出
                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "支出",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = formatAmount(monthlyExpense),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // 结余
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "结余",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = formatAmount(monthlyBalance),
                        style = MaterialTheme.typography.titleLarge,
                        color = if (monthlyBalance >= 0) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

private fun formatAmount(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.CHINA)
    return format.format(amount)
} 
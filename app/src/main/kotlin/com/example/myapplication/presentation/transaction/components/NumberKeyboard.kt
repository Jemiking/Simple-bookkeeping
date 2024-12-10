package com.example.myapplication.presentation.transaction.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun NumberKeyboard(
    onNumberClick: (Int) -> Unit,
    onDecimalClick: () -> Unit,
    onBackspaceClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        // 第一行：1、2、3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NumberKey(number = 1, onClick = onNumberClick)
            NumberKey(number = 2, onClick = onNumberClick)
            NumberKey(number = 3, onClick = onNumberClick)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 第二行：4、5、6
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NumberKey(number = 4, onClick = onNumberClick)
            NumberKey(number = 5, onClick = onNumberClick)
            NumberKey(number = 6, onClick = onNumberClick)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 第三行：7、8、9
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NumberKey(number = 7, onClick = onNumberClick)
            NumberKey(number = 8, onClick = onNumberClick)
            NumberKey(number = 9, onClick = onNumberClick)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 第四行：清除、0、小数点、退格
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 清除
            KeyboardKey(
                modifier = Modifier.weight(1f),
                onClick = onClearClick
            ) {
                Text(
                    text = "C",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // 0
            NumberKey(
                number = 0,
                onClick = onNumberClick,
                modifier = Modifier.weight(1f)
            )

            // 小数点
            KeyboardKey(
                modifier = Modifier.weight(1f),
                onClick = onDecimalClick
            ) {
                Text(
                    text = ".",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // 退格
            KeyboardKey(
                modifier = Modifier.weight(1f),
                onClick = onBackspaceClick
            ) {
                Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = "退格",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun NumberKey(
    number: Int,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    KeyboardKey(
        modifier = modifier.weight(1f),
        onClick = { onClick(number) }
    ) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun KeyboardKey(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .height(56.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
} 
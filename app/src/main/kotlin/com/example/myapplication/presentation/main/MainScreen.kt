package com.example.myapplication.presentation.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.presentation.main.components.TransactionList

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("main_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 主界面内容
            TransactionList(
                modifier = Modifier
                    .weight(1f)
                    .testTag("transaction_list")
            )
        }
    }
}

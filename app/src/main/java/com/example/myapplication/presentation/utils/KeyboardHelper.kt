package com.example.myapplication.presentation.utils

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun rememberKeyboardState(): State<Boolean> {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val view = LocalView.current

    return remember {
        mutableStateOf(false)
    }.apply {
        LaunchedEffect(windowInfo, density) {
            snapshotFlow { 
                WindowInsets.ime.getBottom(density) > 0 
            }
            .distinctUntilChanged()
            .collect { isVisible ->
                this.value = isVisible
            }
        }
    }
}

@Composable
fun rememberKeyboardHeight(): State<Int> {
    val density = LocalDensity.current
    return remember {
        mutableStateOf(0)
    }.apply {
        LaunchedEffect(density) {
            snapshotFlow { 
                WindowInsets.ime.getBottom(density)
            }
            .distinctUntilChanged()
            .collect { height ->
                this.value = height
            }
        }
    }
} 
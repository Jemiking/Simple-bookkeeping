package com.example.myapplication.presentation.utils

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private const val DEBOUNCE_TIMEOUT = 100L

@Composable
fun rememberKeyboardState(): State<Boolean> {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }

    return produceState(initialValue = false) {
        job?.cancel()
        job = scope.launch {
            snapshotFlow { 
                WindowInsets.ime.getBottom(density) > 0 
            }
            .debounce(DEBOUNCE_TIMEOUT)
            .distinctUntilChanged()
            .collect { isVisible ->
                value = isVisible
            }
        }

        awaitDispose {
            job?.cancel()
            job = null
        }
    }
}

@Composable
fun rememberKeyboardHeight(): State<Int> {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }

    return produceState(initialValue = 0) {
        job?.cancel()
        job = scope.launch {
            snapshotFlow { 
                WindowInsets.ime.getBottom(density)
            }
            .debounce(DEBOUNCE_TIMEOUT)
            .distinctUntilChanged()
            .collect { height ->
                value = height
            }
        }

        awaitDispose {
            job?.cancel()
            job = null
        }
    }
}

@Composable
fun KeyboardAwareWrapper(
    content: @Composable (isKeyboardVisible: Boolean, keyboardHeight: Int) -> Unit
) {
    val keyboardVisible by rememberKeyboardState()
    val keyboardHeight by rememberKeyboardHeight()
    val scope = rememberCoroutineScope()
    var cleanupJob by remember { mutableStateOf<Job?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            cleanupJob?.cancel()
            cleanupJob = scope.launch {
                // 确保键盘隐藏
                val view = LocalView.current
                view.clearFocus()
                android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS
            }
        }
    }

    content(keyboardVisible, keyboardHeight)
}

@Composable
fun KeyboardAwareContent(
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    onKeyboardVisibilityChanged: ((Boolean) -> Unit)? = null,
    onKeyboardHeightChanged: ((Int) -> Unit)? = null,
    content: @Composable (isKeyboardVisible: Boolean, keyboardHeight: Int) -> Unit
) {
    val keyboardVisible by rememberKeyboardState()
    val keyboardHeight by rememberKeyboardHeight()

    LaunchedEffect(keyboardVisible) {
        onKeyboardVisibilityChanged?.invoke(keyboardVisible)
    }

    LaunchedEffect(keyboardHeight) {
        onKeyboardHeightChanged?.invoke(keyboardHeight)
    }

    content(keyboardVisible, keyboardHeight)
} 
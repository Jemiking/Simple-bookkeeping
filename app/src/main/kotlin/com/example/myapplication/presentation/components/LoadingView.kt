package com.example.myapplication.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.core.data.PreloadState

@Composable
fun LoadingView(
    state: PreloadState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("loading_container"),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            is PreloadState.NotStarted -> {
                // 显示空状态
            }
            is PreloadState.Loading -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { state.progress / 100f },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("progress_indicator"),
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "正在加载 ${state.progress}%",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
            is PreloadState.Completed -> {
                // 加载完成,可以隐藏
            }
            is PreloadState.Error -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Filled.Error,
                        contentDescription = "错误",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("error_icon")
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { /* 重试逻辑 */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.testTag("retry_button")
                    ) {
                        Text("重试")
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingPlaceholder(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Surface(
        modifier = modifier.testTag("loading_placeholder"),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha),
        shape = MaterialTheme.shapes.small
    ) {
        Spacer(modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun LoadingShimmer(
    modifier: Modifier = Modifier
) {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
    )

    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val brush = androidx.compose.ui.graphics.Brush.linearGradient(
        colors = shimmerColors,
        start = androidx.compose.ui.geometry.Offset(translateAnim, 0f),
        end = androidx.compose.ui.geometry.Offset(translateAnim + 100f, 100f)
    )

    Surface(
        modifier = modifier.testTag("loading_shimmer"),
        shape = MaterialTheme.shapes.small
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = brush)
        )
    }
} 
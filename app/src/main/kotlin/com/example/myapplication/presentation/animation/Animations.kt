package com.example.myapplication.presentation.animation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset

// 淡入淡出动画
@ExperimentalAnimationApi
fun fadeInAnimation(duration: Int = 300) = fadeIn(
    animationSpec = tween(
        durationMillis = duration,
        easing = FastOutSlowInEasing
    )
)

@ExperimentalAnimationApi
fun fadeOutAnimation(duration: Int = 300) = fadeOut(
    animationSpec = tween(
        durationMillis = duration,
        easing = FastOutSlowInEasing
    )
)

// 滑动动画
@ExperimentalAnimationApi
fun slideInHorizontallyAnimation(
    initialOffsetX: Int = 300,
    duration: Int = 300
) = slideInHorizontally(
    initialOffsetX = { initialOffsetX },
    animationSpec = tween(
        durationMillis = duration,
        easing = FastOutSlowInEasing
    )
)

@ExperimentalAnimationApi
fun slideOutHorizontallyAnimation(
    targetOffsetX: Int = -300,
    duration: Int = 300
) = slideOutHorizontally(
    targetOffsetX = { targetOffsetX },
    animationSpec = tween(
        durationMillis = duration,
        easing = FastOutSlowInEasing
    )
)

// 缩放动画
@ExperimentalAnimationApi
fun scaleInAnimation(duration: Int = 300) = scaleIn(
    initialScale = 0.8f,
    animationSpec = tween(
        durationMillis = duration,
        easing = FastOutSlowInEasing
    )
)

@ExperimentalAnimationApi
fun scaleOutAnimation(duration: Int = 300) = scaleOut(
    targetScale = 0.8f,
    animationSpec = tween(
        durationMillis = duration,
        easing = FastOutSlowInEasing
    )
)

// 组合动画
@ExperimentalAnimationApi
fun enterTransition(duration: Int = 300) = fadeInAnimation(duration) + 
    slideInHorizontallyAnimation(duration = duration) +
    scaleInAnimation(duration)

@ExperimentalAnimationApi
fun exitTransition(duration: Int = 300) = fadeOutAnimation(duration) + 
    slideOutHorizontallyAnimation(duration = duration) +
    scaleOutAnimation(duration)

// 加载动画
@Composable
fun loadingAnimation(
    durationMillis: Int = 1000
): InfiniteTransition {
    return rememberInfiniteTransition().apply {
        animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = durationMillis,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            )
        )
    }
}

// 脉冲动画
@Composable
fun pulseAnimation(
    durationMillis: Int = 1000,
    minScale: Float = 0.8f,
    maxScale: Float = 1.2f
): InfiniteTransition {
    return rememberInfiniteTransition().apply {
        animateFloat(
            initialValue = minScale,
            targetValue = maxScale,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = durationMillis,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            )
        )
    }
}

// 波纹动画
@Composable
fun rippleAnimation(
    durationMillis: Int = 1000,
    minAlpha: Float = 0f,
    maxAlpha: Float = 1f
): InfiniteTransition {
    return rememberInfiniteTransition().apply {
        animateFloat(
            initialValue = minAlpha,
            targetValue = maxAlpha,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = durationMillis,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            )
        )
    }
} 
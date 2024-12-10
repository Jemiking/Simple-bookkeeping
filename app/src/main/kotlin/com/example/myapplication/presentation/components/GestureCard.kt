package com.example.myapplication.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
fun GestureCard(
    onDoubleTap: () -> Unit = {},
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    onLongPress: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val offsetX = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    // 双击检测
    var lastTapTime by remember { mutableStateOf(0L) }
    
    Box(
        modifier = Modifier
            .graphicsLayer {
                translationX = offsetX.value
                scaleX = scale.value
                scaleY = scale.value
                rotationZ = rotation.value
            }
            .pointerInput(Unit) {
                coroutineScope {
                    detectTapGestures(
                        onTap = { 
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastTapTime < 300) {
                                launch {
                                    // 双击动画
                                    scale.animateTo(
                                        0.8f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )
                                    scale.animateTo(1f)
                                    onDoubleTap()
                                }
                            }
                            lastTapTime = currentTime
                        },
                        onLongPress = {
                            launch {
                                // 长按��画
                                rotation.animateTo(
                                    5f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                                rotation.animateTo(0f)
                                onLongPress()
                            }
                        }
                    )
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        scope.launch {
                            val swipeThreshold = 100.dp.toPx()
                            when {
                                offsetX.value > swipeThreshold -> {
                                    offsetX.animateTo(size.width.toFloat())
                                    onSwipeRight()
                                }
                                offsetX.value < -swipeThreshold -> {
                                    offsetX.animateTo(-size.width.toFloat())
                                    onSwipeLeft()
                                }
                                else -> {
                                    offsetX.animateTo(0f)
                                }
                            }
                        }
                    },
                    onDragCancel = {
                        scope.launch {
                            offsetX.animateTo(0f)
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        scope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount)
                        }
                    }
                )
            }
    ) {
        content()
    }
} 
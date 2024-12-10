package com.example.myapplication.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Composable
fun SwipeToDismissCard(
    onDismiss: () -> Unit,
    dismissThreshold: Float = 0.5f,
    animationSpec: AnimationSpec<Float> = SpringSpec(),
    content: @Composable () -> Unit
) {
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val velocityTracker = remember { VelocityTracker() }
    
    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.value.roundToInt(), 0) }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        val velocity = velocityTracker.calculateVelocity().x
                        val dismissVelocityThreshold = with(density) { 1000.dp.toPx() }
                        
                        when {
                            velocity.absoluteValue > dismissVelocityThreshold -> {
                                // 快速滑动时直接关闭
                                scope.launch {
                                    offsetX.animateTo(
                                        if (velocity > 0) size.width.toFloat() else -size.width.toFloat(),
                                        animationSpec
                                    )
                                    onDismiss()
                                }
                            }
                            offsetX.value.absoluteValue > size.width * dismissThreshold -> {
                                // 超过阈值时关闭
                                scope.launch {
                                    offsetX.animateTo(
                                        if (offsetX.value > 0) size.width.toFloat() else -size.width.toFloat(),
                                        animationSpec
                                    )
                                    onDismiss()
                                }
                            }
                            else -> {
                                // 未超过阈值时恢复
                                scope.launch {
                                    offsetX.animateTo(0f, animationSpec)
                                }
                            }
                        }
                        velocityTracker.resetTracking()
                    },
                    onDragCancel = {
                        scope.launch {
                            offsetX.animateTo(0f, animationSpec)
                        }
                        velocityTracker.resetTracking()
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        scope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount)
                        }
                        velocityTracker.addPosition(
                            change.uptimeMillis,
                            change.position
                        )
                    }
                )
            }
    ) {
        content()
    }
} 
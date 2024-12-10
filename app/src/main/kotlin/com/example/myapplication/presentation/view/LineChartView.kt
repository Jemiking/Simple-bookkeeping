package com.example.myapplication.presentation.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import java.text.NumberFormat
import kotlin.math.max
import kotlin.math.min

class LineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 画笔设置
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 32f
        textAlign = Paint.Align.CENTER
    }

    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    // 路径
    private val linePath = Path()
    private val fillPath = Path()

    // 数据点
    private var points: List<PointF> = emptyList()
    private var animatedPoints: List<PointF> = emptyList()
    
    // 数值范围
    private var minY = 0f
    private var maxY = 0f
    
    // 动画
    private var animator: ValueAnimator? = null
    private var animationProgress = 0f
    
    // 手势检测
    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())
    private val gestureDetector = GestureDetector(context, GestureListener())
    
    // 缩放和平移
    private var scaleFactor = 1f
    private var translateX = 0f
    private var translateY = 0f

    init {
        // 初始化颜色
        linePaint.color = ContextCompat.getColor(context, R.color.primary)
        fillPaint.color = ContextCompat.getColor(context, R.color.primary_light)
        textPaint.color = ContextCompat.getColor(context, R.color.text_primary)
        axisPaint.color = ContextCompat.getColor(context, R.color.text_secondary)
    }

    fun setData(data: List<Pair<Float, Float>>) {
        // 计算数值范围
        if (data.isNotEmpty()) {
            minY = data.minOf { it.second }
            maxY = data.maxOf { it.second }
        }

        // 转换为视图坐标点
        points = data.mapIndexed { index, (x, y) ->
            PointF(
                index * (width / (data.size - 1).toFloat()),
                height - (y - minY) / (maxY - minY) * height
            )
        }

        // 启动动画
        startAnimation()
    }

    private fun startAnimation() {
        animator?.cancel()
        
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1000
            addUpdateListener { 
                animationProgress = it.animatedValue as Float
                updateAnimatedPoints()
                invalidate()
            }
            start()
        }
    }

    private fun updateAnimatedPoints() {
        animatedPoints = points.map { point ->
            PointF(
                point.x,
                height - (height - point.y) * animationProgress
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (animatedPoints.isEmpty()) return

        canvas.save()
        canvas.translate(translateX, translateY)
        canvas.scale(scaleFactor, scaleFactor)

        // 绘制坐标轴
        drawAxes(canvas)

        // 绘制填充区域
        drawFill(canvas)

        // 绘制折线
        drawLine(canvas)

        // 绘制数据点
        drawPoints(canvas)

        canvas.restore()
    }

    private fun drawAxes(canvas: Canvas) {
        // X轴
        canvas.drawLine(0f, height.toFloat(), width.toFloat(), height.toFloat(), axisPaint)
        
        // Y轴
        canvas.drawLine(0f, 0f, 0f, height.toFloat(), axisPaint)

        // Y轴刻度
        val steps = 5
        for (i in 0..steps) {
            val y = height - (height * i / steps)
            val value = minY + (maxY - minY) * i / steps
            canvas.drawLine(0f, y, -10f, y, axisPaint)
            canvas.drawText(
                NumberFormat.getInstance().format(value),
                -50f,
                y + textPaint.textSize / 3,
                textPaint
            )
        }
    }

    private fun drawFill(canvas: Canvas) {
        fillPath.reset()
        fillPath.moveTo(animatedPoints.first().x, height.toFloat())
        
        animatedPoints.forEach { point ->
            fillPath.lineTo(point.x, point.y)
        }
        
        fillPath.lineTo(animatedPoints.last().x, height.toFloat())
        fillPath.close()
        
        canvas.drawPath(fillPath, fillPaint)
    }

    private fun drawLine(canvas: Canvas) {
        linePath.reset()
        linePath.moveTo(animatedPoints.first().x, animatedPoints.first().y)
        
        animatedPoints.forEach { point ->
            linePath.lineTo(point.x, point.y)
        }
        
        canvas.drawPath(linePath, linePaint)
    }

    private fun drawPoints(canvas: Canvas) {
        animatedPoints.forEach { point ->
            canvas.drawCircle(point.x, point.y, 8f, linePaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        return true
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = max(0.1f, min(scaleFactor, 5.0f))
            invalidate()
            return true
        }
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            translateX -= distanceX
            translateY -= distanceY
            invalidate()
            return true
        }

        override fun onDown(e: MotionEvent): Boolean = true
    }
} 
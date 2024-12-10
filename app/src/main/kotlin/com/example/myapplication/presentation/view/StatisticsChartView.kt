package com.example.myapplication.presentation.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.example.myapplication.presentation.util.formatCurrency
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class StatisticsChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = resources.getDimension(R.dimen.text_size_medium)
        textAlign = Paint.Align.CENTER
    }

    private val bounds = RectF()
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f

    private var data: List<PieChartData> = emptyList()
    private var animatedData: List<PieChartData> = emptyList()
    private var totalAmount = 0f
    private var animationProgress = 0f

    private var animator: ValueAnimator? = null
    private val chartColors = resources.getIntArray(R.array.chart_colors)

    init {
        textPaint.color = ContextCompat.getColor(context, R.color.text_primary)
    }

    fun setData(newData: List<PieChartData>) {
        data = newData
        totalAmount = data.sumOf { it.amount.toDouble() }.toFloat()
        startAnimation()
    }

    private fun startAnimation() {
        animator?.cancel()

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1000
            addUpdateListener {
                animationProgress = it.animatedValue as Float
                updateAnimatedData()
                invalidate()
            }
            start()
        }
    }

    private fun updateAnimatedData() {
        animatedData = data.map { item ->
            item.copy(amount = item.amount * animationProgress)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        radius = min(w, h) / 3f
        bounds.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (animatedData.isEmpty()) return

        var startAngle = -90f
        animatedData.forEachIndexed { index, item ->
            val sweepAngle = 360f * (item.amount / totalAmount)
            
            // 绘制扇形
            paint.color = chartColors[index % chartColors.size]
            canvas.drawArc(bounds, startAngle, sweepAngle, true, paint)

            // 绘制标签
            if (sweepAngle > 15) { // 只在扇形足够大时绘制标签
                val labelAngle = Math.toRadians((startAngle + sweepAngle / 2).toDouble())
                val labelRadius = radius * 0.7
                val x = (centerX + cos(labelAngle) * labelRadius).toFloat()
                val y = (centerY + sin(labelAngle) * labelRadius).toFloat()

                // 绘制分类名称
                canvas.drawText(
                    item.category,
                    x,
                    y,
                    textPaint
                )

                // 绘制金额和百分比
                val percentage = "%.1f%%".format(100f * item.amount / totalAmount)
                val amountText = "${item.amount.formatCurrency()} ($percentage)"
                canvas.drawText(
                    amountText,
                    x,
                    y + textPaint.textSize * 1.5f,
                    textPaint
                )
            }

            startAngle += sweepAngle
        }
    }
}

data class PieChartData(
    val category: String,
    val amount: Float
) 
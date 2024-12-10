package com.example.myapplication.presentation.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.myapplication.R

class BudgetProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val warningPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val rect = RectF()
    private var progress = 0f
    private var warningThreshold = 0.8f

    init {
        backgroundPaint.color = ContextCompat.getColor(context, R.color.background_light)
        progressPaint.color = ContextCompat.getColor(context, R.color.primary)
        warningPaint.color = ContextCompat.getColor(context, R.color.expense_color)
    }

    fun setProgress(value: Float) {
        progress = value.coerceIn(0f, 1f)
        invalidate()
    }

    fun setWarningThreshold(value: Float) {
        warningThreshold = value.coerceIn(0f, 1f)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 绘制背景
        rect.set(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(rect, height / 2f, height / 2f, backgroundPaint)

        // 绘制进度
        if (progress > 0) {
            rect.right = width * progress
            val paint = if (progress > warningThreshold) warningPaint else progressPaint
            canvas.drawRoundRect(rect, height / 2f, height / 2f, paint)
        }
    }
} 
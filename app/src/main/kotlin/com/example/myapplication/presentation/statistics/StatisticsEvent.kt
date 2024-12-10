package com.example.myapplication.presentation.statistics

import com.example.myapplication.data.local.entity.TransactionType
import java.time.YearMonth

sealed class StatisticsEvent {
    // 时间范围选择
    data class MonthSelected(val month: YearMonth) : StatisticsEvent()
    
    // 类型切换
    data class TypeChanged(val type: TransactionType) : StatisticsEvent()
    
    // 图表相关
    data object ToggleChartType : StatisticsEvent()
    data class CategorySelected(val categoryId: Long?) : StatisticsEvent()
    
    // 错误处理
    data object DismissError : StatisticsEvent()
    
    // 刷新数据
    data object Refresh : StatisticsEvent()
} 
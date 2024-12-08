package com.example.myapplication.util

import org.junit.Test
import org.junit.Assert.*

class WindowSizeTest {
    
    @Test
    fun testWindowSizeCalculation() {
        // 测试窗口大小计算
        val width = 1080
        val height = 1920
        
        // 测试宽度分类
        assertTrue(WindowSize.isWindowCompact(width = 600))
        assertTrue(WindowSize.isWindowMedium(width = 840))
        assertTrue(WindowSize.isWindowExpanded(width = 1200))
        
        // 测试高度分类
        assertTrue(WindowSize.isWindowCompact(height = 480))
        assertTrue(WindowSize.isWindowMedium(height = 900))
        assertTrue(WindowSize.isWindowExpanded(height = 1000))
    }
    
    @Test
    fun testWindowSizeValidation() {
        // 测试边界值
        assertFalse(WindowSize.isWindowCompact(width = -1))
        assertFalse(WindowSize.isWindowMedium(width = 0))
        assertTrue(WindowSize.isWindowCompact(width = 1))
    }
} 
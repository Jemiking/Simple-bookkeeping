package com.example.myapplication.util

import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mockito.*
import android.content.Context
import android.content.pm.PackageManager

class PermissionManagerTest {
    
    @Test
    fun testPermissionCheck() {
        // 创建Mock对象
        val mockContext = mock(Context::class.java)
        val permissionManager = PermissionManager(mockContext)
        
        // 模拟权限检查结果
        `when`(mockContext.checkSelfPermission(any())).thenReturn(PackageManager.PERMISSION_GRANTED)
        
        // 测试权限检查
        assertTrue(permissionManager.hasPermission("android.permission.CAMERA"))
        assertTrue(permissionManager.hasPermission("android.permission.READ_EXTERNAL_STORAGE"))
        
        // 验证方法调用
        verify(mockContext, times(2)).checkSelfPermission(any())
    }
    
    @Test
    fun testPermissionDenied() {
        // 创建Mock对象
        val mockContext = mock(Context::class.java)
        val permissionManager = PermissionManager(mockContext)
        
        // 模拟权限被拒绝的情况
        `when`(mockContext.checkSelfPermission(any())).thenReturn(PackageManager.PERMISSION_DENIED)
        
        // 测试权限检查
        assertFalse(permissionManager.hasPermission("android.permission.CAMERA"))
        
        // 验证方法调用
        verify(mockContext).checkSelfPermission(any())
    }
} 
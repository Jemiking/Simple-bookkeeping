package com.example.myapplication.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import javax.inject.Inject

class PermissionManager @Inject constructor(
    private val context: Context
) {
    fun hasRequiredPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true // Android 10及以上不需要存储权限
        } else {
            hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
            hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            emptyArray() // Android 10及以上不需要存储权限
        } else {
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }
} 
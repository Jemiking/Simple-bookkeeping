package com.example.myapplication.presentation.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.view.View
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ShareUtils {

    fun shareStatistics(context: Context, view: View, title: String) {
        try {
            // 生成统计图片
            val bitmap = createBitmapFromView(view)
            
            // 保存图片到缓存目录
            val cachePath = File(context.cacheDir, "statistics")
            cachePath.mkdirs()
            
            val file = File(cachePath, "statistics.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
            
            // 获取文件URI
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            
            // 创建分享Intent
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            // 启动分享
            context.startActivity(Intent.createChooser(intent, title))
            
        } catch (e: Exception) {
            e.printStackTrace()
            // TODO: 处理错误
        }
    }

    private fun createBitmapFromView(view: View): Bitmap {
        // 确保视图已经测量和布局
        if (view.width == 0 || view.height == 0) {
            view.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        }

        // 创建位图
        val bitmap = Bitmap.createBitmap(
            view.width,
            view.height,
            Bitmap.Config.ARGB_8888
        )

        // 将视图绘制到位图
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        return bitmap
    }
} 
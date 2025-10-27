package com.movtery.zalithlauncher.utils.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.createBitmap
import com.movtery.zalithlauncher.utils.logging.Logger.lWarning
import java.io.File

/**
 * 将 [Drawable] 转换为 [Bitmap]
 * 如果该 Drawable 已经是 [BitmapDrawable] 且其内部 Bitmap 不为 null，则直接返回该 Bitmap
 * 否则渲染到一个新的 Bitmap 上
 */
fun Drawable.toBitmap(): Bitmap {
    if (this is BitmapDrawable && this.bitmap != null) {
        return this.bitmap
    }

    val width = if (intrinsicWidth > 0) intrinsicWidth else 1
    val height = if (intrinsicHeight > 0) intrinsicHeight else 1

    val bitmap = createBitmap(width, height)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)

    return bitmap
}

/**
 * 尝试判断文件是否为一个图片
 */
fun File.isImageFile(): Boolean {
    if (!this.exists()) return false

    return try {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(this.absolutePath, options)
        options.outWidth > 0 && options.outHeight > 0
    } catch (e: Exception) {
        lWarning("An exception occurred while trying to determine if ${this.absolutePath} is an image.", e)
        false
    }
}
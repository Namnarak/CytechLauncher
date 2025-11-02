/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

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
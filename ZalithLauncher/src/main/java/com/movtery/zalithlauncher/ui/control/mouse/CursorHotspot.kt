package com.movtery.zalithlauncher.ui.control.mouse

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 鼠标指针热点存储类型，保存热点的X、Y的百分比坐标
 */
@Parcelize
data class CursorHotspot(
    val xPercent: Int,
    val yPercent: Int
): Parcelable

/**
 * 默认：居中的指针热点
 */
val CENTER_HOTSPOT = CursorHotspot(xPercent = 50, yPercent = 50)

/**
 * 默认：左上角的指针热点
 */
val LEFT_TOP_HOTSPOT = CursorHotspot(xPercent = 0, yPercent = 0)
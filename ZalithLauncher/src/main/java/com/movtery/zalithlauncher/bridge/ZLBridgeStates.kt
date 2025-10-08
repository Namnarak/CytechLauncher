package com.movtery.zalithlauncher.bridge

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.PointerIcon
import android.view.PointerIcon as NativePointerIcon

object ZLBridgeStates {
    /**
     * 状态：指针模式（启用、禁用）
     */
    @JvmStatic
    var cursorMode by mutableIntStateOf(CURSOR_ENABLED)

    /**
     * 状态：指针形状
     */
    @JvmStatic
    var cursorShape by mutableStateOf(CursorShape.Arrow)

    /**
     * 状态：当前画面帧率
     */
    @JvmStatic
    var currentFPS by mutableIntStateOf(0)

    /**
     * 莊濤：窗口变更刷新key
     */
    @JvmStatic
    var windowChangeKey by mutableStateOf(false)

    fun onWindowChange() {
        this.windowChangeKey = !this.windowChangeKey
    }
}

/** 指针:启用 */
const val CURSOR_ENABLED = 1
/** 指针:禁用 */
const val CURSOR_DISABLED = 0

/**
 * 指针形状（目前仅支持箭头、输入、手型）
 */
enum class CursorShape(
    val composeIcon: PointerIcon
) {
    /**
     * 箭头
     */
    Arrow(PointerIcon.Default),

    /**
     * 输入
     */
    IBeam(PointerIcon.Text),

    /**
     * 手形
     */
    Hand(PointerIcon.Hand),

    /**
     * 十字
     */
    CrossHair(PointerIcon.Crosshair),

    /**
     * 调整大小（上下）
     */
    ResizeNS(PointerIcon(NativePointerIcon.TYPE_VERTICAL_DOUBLE_ARROW)),

    /**
     * 调整大小（左右）
     */
    ResizeEW(PointerIcon(NativePointerIcon.TYPE_HORIZONTAL_DOUBLE_ARROW)),

    /**
     * 调整大小（全部方向）
     */
    ResizeAll(PointerIcon(NativePointerIcon.TYPE_ALL_SCROLL)),

    /**
     * 禁止/无效操作
     */
    NotAllowed(PointerIcon(NativePointerIcon.TYPE_NO_DROP))
}
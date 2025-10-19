package com.movtery.zalithlauncher.ui.screens.main.control_editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerId
import com.movtery.layer_controller.ControlBoxLayout
import com.movtery.layer_controller.observable.ObservableControlLayout
import com.movtery.zalithlauncher.bridge.CURSOR_DISABLED
import com.movtery.zalithlauncher.bridge.ZLBridgeStates
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.control.mouse.SwitchableMouseLayout

/**
 * 预览控制布局层
 * @param observableLayout 被预览的控制布局
 */
@Composable
fun PreviewControlBox(
    observableLayout: ObservableControlLayout,
    modifier: Modifier = Modifier
) {
    val occupiedPointers = remember(observableLayout) { mutableStateSetOf<PointerId>() }
    val moveOnlyPointers = remember(observableLayout) { mutableStateSetOf<PointerId>() }

    ControlBoxLayout(
        modifier = modifier.fillMaxSize(),
        observedLayout = observableLayout,
        checkOccupiedPointers = { occupiedPointers.contains(it) },
        opacity = (AllSettings.controlsOpacity.state.toFloat() / 100f).coerceIn(0f, 1f),
        markPointerAsMoveOnly = { moveOnlyPointers.add(it) },
        isCursorGrabbing = ZLBridgeStates.cursorMode == CURSOR_DISABLED
    ) {
        PreviewMouseLayout(
            modifier = Modifier.fillMaxSize(),
            isMoveOnlyPointer = { moveOnlyPointers.contains(it) },
            onOccupiedPointer = { occupiedPointers.add(it) },
            onReleasePointer = {
                occupiedPointers.remove(it)
                moveOnlyPointers.remove(it)
            }
        )
    }
}

/**
 * 预览鼠标控制层
 * @param isMoveOnlyPointer 检查指针是否被标记为仅处理滑动事件
 * @param onOccupiedPointer 标记指针已被占用
 * @param onReleasePointer 标记指针已被释放
 */
@Composable
private fun PreviewMouseLayout(
    modifier: Modifier = Modifier,
    isMoveOnlyPointer: (PointerId) -> Boolean,
    onOccupiedPointer: (PointerId) -> Unit,
    onReleasePointer: (PointerId) -> Unit
) {
    Box(
        modifier = modifier
    ) {
        SwitchableMouseLayout(
            modifier = Modifier.fillMaxSize(),
            cursorMode = ZLBridgeStates.cursorMode,
            isMoveOnlyPointer = isMoveOnlyPointer,
            onOccupiedPointer = onOccupiedPointer,
            onReleasePointer = onReleasePointer
        )
    }
}
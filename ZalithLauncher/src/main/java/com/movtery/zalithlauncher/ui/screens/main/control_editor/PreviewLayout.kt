package com.movtery.zalithlauncher.ui.screens.main.control_editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerId
import com.movtery.layer_controller.ControlBoxLayout
import com.movtery.layer_controller.HideLayerWhen
import com.movtery.layer_controller.observable.ObservableControlLayout
import com.movtery.zalithlauncher.ui.control.mouse.SwitchableMouseLayout

/**
 * 预览控制布局层
 * @param observableLayout 被预览的控制布局
 * @param previewScenario 控制布局预览的场景
 * @param previewHideLayerWhen  控制布局预览时，模拟当前使用的设备
 *                              控件层会根据该值决定是否隐藏
 */
@Composable
fun PreviewControlBox(
    observableLayout: ObservableControlLayout,
    previewScenario: PreviewScenario,
    previewHideLayerWhen: HideLayerWhen,
    modifier: Modifier = Modifier,
) {
    val occupiedPointers = remember(observableLayout) { mutableStateSetOf<PointerId>() }
    val moveOnlyPointers = remember(observableLayout) { mutableStateSetOf<PointerId>() }

    ControlBoxLayout(
        modifier = modifier.fillMaxSize(),
        observedLayout = observableLayout,
        checkOccupiedPointers = { occupiedPointers.contains(it) },
        markPointerAsMoveOnly = { moveOnlyPointers.add(it) },
        isCursorGrabbing = previewScenario.isCursorGrabbing,
        hideLayerWhen = previewHideLayerWhen
    ) {
        PreviewMouseLayout(
            modifier = Modifier.fillMaxSize(),
            isMoveOnlyPointer = { moveOnlyPointers.contains(it) },
            onOccupiedPointer = { occupiedPointers.add(it) },
            onReleasePointer = {
                occupiedPointers.remove(it)
                moveOnlyPointers.remove(it)
            },
            previewScenario = previewScenario
        )
    }
}

/**
 * 预览鼠标控制层
 * @param isMoveOnlyPointer 检查指针是否被标记为仅处理滑动事件
 * @param onOccupiedPointer 标记指针已被占用
 * @param onReleasePointer 标记指针已被释放
 * @param previewScenario 控制布局预览的场景
 */
@Composable
private fun PreviewMouseLayout(
    modifier: Modifier = Modifier,
    isMoveOnlyPointer: (PointerId) -> Boolean,
    onOccupiedPointer: (PointerId) -> Unit,
    onReleasePointer: (PointerId) -> Unit,
    previewScenario: PreviewScenario
) {
    Box(
        modifier = modifier
    ) {
        SwitchableMouseLayout(
            modifier = Modifier.fillMaxSize(),
            cursorMode = previewScenario.cursorMode,
            isMoveOnlyPointer = isMoveOnlyPointer,
            onOccupiedPointer = onOccupiedPointer,
            onReleasePointer = onReleasePointer
        )
    }
}
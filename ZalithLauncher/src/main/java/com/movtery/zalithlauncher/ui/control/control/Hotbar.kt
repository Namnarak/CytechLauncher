package com.movtery.zalithlauncher.ui.control.control

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.bridge.ZLBridgeStates
import com.movtery.zalithlauncher.game.keycodes.LwjglGlfwKeycode
import com.movtery.zalithlauncher.game.launch.MCOptions
import org.lwjgl.glfw.CallbackBridge.windowHeight
import org.lwjgl.glfw.CallbackBridge.windowWidth

private val keyList = listOf(
    LwjglGlfwKeycode.GLFW_KEY_1,
    LwjglGlfwKeycode.GLFW_KEY_2,
    LwjglGlfwKeycode.GLFW_KEY_3,
    LwjglGlfwKeycode.GLFW_KEY_4,
    LwjglGlfwKeycode.GLFW_KEY_5,
    LwjglGlfwKeycode.GLFW_KEY_6,
    LwjglGlfwKeycode.GLFW_KEY_7,
    LwjglGlfwKeycode.GLFW_KEY_8,
    LwjglGlfwKeycode.GLFW_KEY_9,
)

@Composable
fun BoxScope.MinecraftHotbar(
    onClickSlot: (key: Int) -> Unit,
    isGrabbing: Boolean = false,
    resolutionRatio: Int
) {
    val density = LocalDensity.current

    var hotbarSize by remember { mutableStateOf(DpSize(0.dp, 0.dp)) }

    LaunchedEffect(
        isGrabbing, MCOptions.refreshKey, density,
        resolutionRatio, ZLBridgeStates.windowChangeKey
    ) {
        val guiScale = getMCGuiScale()
        val scaleFactor = resolutionRatio / 100f

        with(density) {
            val width = mcScale(guiScale, 180, scaleFactor)
            val height = mcScale(guiScale, 20, scaleFactor)

            val widthDp = width.toDp()
            val heightDp = height.toDp()

            hotbarSize = DpSize(widthDp, heightDp)
        }
    }

    if (isGrabbing) {
        Box(
            modifier = Modifier
                .size(hotbarSize)
                .align(Alignment.BottomCenter)
                .mainTouchLogic(
                    slotCount = keyList.size,
                    hotbarSize = hotbarSize,
                    density = density,
                    onClickSlot = { index ->
                        onClickSlot(keyList[index].toInt())
                    }
                )
        )
    }
}

private fun Modifier.mainTouchLogic(
    slotCount: Int,
    hotbarSize: DpSize,
    density: Density,
    onClickSlot: (index: Int) -> Unit
) = this.pointerInput(slotCount, hotbarSize, density) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)

            event.changes.forEach { change ->
                if (change.type == PointerType.Touch) {
                    when {
                        change.pressed && !change.previousPressed -> {
                            val x = change.position.x
                            val currentSlotIndex = calculateSlotIndex(x, hotbarSize, slotCount, density)

                            if (currentSlotIndex != -1) {
                                onClickSlot(currentSlotIndex)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getMCGuiScale(): Int {
    val guiScale = MCOptions.get("guiScale")?.toIntOrNull() ?: 0
    val dynamicScale = calculateDynamicScale()
    return if (guiScale == 0 || dynamicScale < guiScale) dynamicScale else guiScale
}

private fun calculateDynamicScale() = minOf(
    windowWidth / 320,
    windowHeight / 240
).coerceAtLeast(1)

private fun mcScale(guiScale: Int, input: Int, scaleFactor: Float): Int {
    return ((guiScale * input) / scaleFactor).toInt()
}

private fun calculateSlotIndex(
    x: Float,
    hotbarSize: DpSize,
    slotCount: Int,
    density: Density
): Int {
    val totalWidth = with(density) { hotbarSize.width.toPx() }
    val slotWidth = totalWidth / slotCount
    return (x / slotWidth).toInt().coerceIn(0, slotCount - 1)
}
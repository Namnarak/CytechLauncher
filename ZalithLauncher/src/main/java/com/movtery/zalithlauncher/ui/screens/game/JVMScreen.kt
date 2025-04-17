package com.movtery.zalithlauncher.ui.screens.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.movtery.zalithlauncher.bridge.AWTInputEvent
import com.movtery.zalithlauncher.bridge.Logger
import com.movtery.zalithlauncher.bridge.ZLBridge
import com.movtery.zalithlauncher.ui.control.mouse.VirtualPointerLayout
import com.movtery.zalithlauncher.ui.screens.game.elements.LogBox

@Composable
fun JVMScreen() {
    var enableLog by remember { mutableStateOf(false) }
    var logText by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        SimpleMouseControlLayout(
            sendMousePress = { ZLBridge.sendMousePress(AWTInputEvent.BUTTON1_DOWN_MASK) },
            sendMouseDragging = { dragging ->
                if (dragging) {
                    ZLBridge.sendMousePress(AWTInputEvent.BUTTON1_DOWN_MASK)
                }
            },
            placeMouse = { mouseX, mouseY ->
                ZLBridge.sendMousePos((mouseX * 0.8).toInt(), (mouseY * 0.8).toInt())
            }
        )

        if (enableLog) {
            Logger.setLogListener { logString ->
                logText += "$logString\n"
            }
            LogBox(logText = logText)
        } else {
            logText = ""
            Logger.setLogListener(null)
        }
    }
}

@Composable
private fun SimpleMouseControlLayout(
    sendMousePress: () -> Unit = {},
    sendMouseDragging: (Boolean) -> Unit = {},
    placeMouse: (mouseX: Float, mouseY: Float) -> Unit = { _, _ -> }
) {
    VirtualPointerLayout(
        onTap = { sendMousePress() },
        onPointerMove = { placeMouse(it.x, it.y) },
        onLongPress = { sendMouseDragging(true) },
        onLongPressEnd = { sendMouseDragging(false) }
    )
}
package com.movtery.zalithlauncher.ui.screens.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.bridge.AWTInputEvent
import com.movtery.zalithlauncher.bridge.Logger
import com.movtery.zalithlauncher.bridge.ZLBridge
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.TouchableButton
import com.movtery.zalithlauncher.ui.control.input.AWTCharSender
import com.movtery.zalithlauncher.ui.control.input.TouchCharInput
import com.movtery.zalithlauncher.ui.control.input.view.TouchCharInput
import com.movtery.zalithlauncher.ui.control.mouse.VirtualPointerLayout
import com.movtery.zalithlauncher.ui.screens.game.elements.LogBox
import com.movtery.zalithlauncher.utils.killProgress

@Composable
fun JVMScreen() {
    var forceCloseDialog by remember { mutableStateOf(false) }
    var enableLog by remember { mutableStateOf(false) }
    var logText by remember { mutableStateOf("") }

    val charInputRef = remember { mutableStateOf<TouchCharInput?>(null) }

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

        TouchCharInput(
            characterSender = AWTCharSender,
            onViewReady = { charInputRef.value = it }
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

        ButtonsLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            changeKeyboard = {
                charInputRef.value?.switchKeyboardState()
            },
            forceCloseClick = {
                forceCloseDialog = true
            },
            changeLogOutput = {
                enableLog = !enableLog
            }
        )

        if (forceCloseDialog) {
            SimpleAlertDialog(
                title = stringResource(R.string.game_button_force_close),
                text = stringResource(R.string.game_dialog_force_close_message),
                onConfirm = {
                    killProgress()
                },
                onDismiss = {
                    forceCloseDialog = false
                }
            )
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

@Composable
private fun ButtonsLayout(
    modifier: Modifier = Modifier,
    changeKeyboard: () -> Unit = {},
    forceCloseClick: () -> Unit = {},
    changeLogOutput: () -> Unit = {}
) {

    ConstraintLayout(modifier = modifier) {
        val (
            input, copy, paste,
            forceClose, logOutput,
            leftClick, rightClick,
            up, right, down, left
        ) = createRefs()

        TextButton(
            modifier = Modifier.constrainAs(input) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
            },
            onClick = changeKeyboard,
            text = stringResource(R.string.game_button_input)
        )
        TextButton(
            modifier = Modifier.constrainAs(copy) {
                top.linkTo(anchor = input.bottom, margin = 8.dp)
                start.linkTo(parent.start)
            },
            onClick = {
                ZLBridge.sendKey(' ', AWTInputEvent.VK_CONTROL, 1)
                ZLBridge.sendKey(' ', AWTInputEvent.VK_C)
                ZLBridge.sendKey(' ', AWTInputEvent.VK_CONTROL, 0)
            },
            text = stringResource(R.string.generic_copy)
        )
        TextButton(
            modifier = Modifier.constrainAs(paste) {
                top.linkTo(anchor = copy.bottom, margin = 8.dp)
                start.linkTo(parent.start)
            },
            onClick = {
                ZLBridge.sendKey(' ', AWTInputEvent.VK_CONTROL, 1)
                ZLBridge.sendKey(' ', AWTInputEvent.VK_V)
                ZLBridge.sendKey(' ', AWTInputEvent.VK_CONTROL, 0)
            },
            text = stringResource(R.string.generic_paste)
        )

        TextButton(
            modifier = Modifier.constrainAs(forceClose) {
                top.linkTo(parent.top)
                end.linkTo(parent.end)
            },
            onClick = forceCloseClick,
            text = stringResource(R.string.game_button_force_close)
        )
        TextButton(
            modifier = Modifier.constrainAs(logOutput) {
                top.linkTo(anchor = forceClose.bottom, margin = 8.dp)
                end.linkTo(parent.end)
            },
            onClick = changeLogOutput,
            text = stringResource(R.string.game_button_log_output)
        )

        TouchableButton(
            modifier = Modifier.constrainAs(leftClick) {
                bottom.linkTo(anchor = rightClick.top, margin = 16.dp)
                start.linkTo(parent.start)
            },
            onTouch = { isPressed ->
                ZLBridge.sendMousePress(AWTInputEvent.BUTTON1_DOWN_MASK, isPressed)
            },
            text = stringResource(R.string.game_button_mouse_left)
        )
        TouchableButton(
            modifier = Modifier.constrainAs(rightClick) {
                bottom.linkTo(anchor = parent.bottom, margin = 8.dp)
                start.linkTo(parent.start)
            },
            onTouch = { isPressed ->
                ZLBridge.sendMousePress(AWTInputEvent.BUTTON3_DOWN_MASK, isPressed)
            },
            text = stringResource(R.string.game_button_mouse_right)
        )

        TextButton(
            modifier = Modifier.constrainAs(up) {
                bottom.linkTo(anchor = down.top, margin = 8.dp)
                end.linkTo(anchor = right.start, margin = 8.dp)
            },
            onClick = {
                ZLBridge.moveWindow(0, -10)
            },
            text = "▲"
        )
        TextButton(
            modifier = Modifier.constrainAs(right) {
                bottom.linkTo(anchor = left.top, margin = 8.dp)
                end.linkTo(parent.end)
            },
            onClick = {
                ZLBridge.moveWindow(10, 0)
            },
            text = "▶"
        )
        TextButton(
            modifier = Modifier.constrainAs(down) {
                bottom.linkTo(parent.bottom)
                end.linkTo(anchor = right.start, margin = 8.dp)
            },
            onClick = {
                ZLBridge.moveWindow(0, 10)
            },
            text = "▼"
        )
        TextButton(
            modifier = Modifier.constrainAs(left) {
                bottom.linkTo(parent.bottom)
                end.linkTo(parent.end)
            },
            onClick = {
                ZLBridge.moveWindow(-10, 0)
            },
            text = "◀"
        )
    }
}

@Composable
private fun TextButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text: String
) {
    Button(
        modifier = modifier,
        onClick = onClick
    ) {
        Text(text = text)
    }
}
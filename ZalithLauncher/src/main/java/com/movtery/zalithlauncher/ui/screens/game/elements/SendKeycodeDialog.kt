package com.movtery.zalithlauncher.ui.screens.game.elements

import androidx.compose.runtime.Composable
import com.movtery.zalithlauncher.ui.control.control.Keyboard
import com.movtery.zalithlauncher.ui.control.control.lwjglEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed interface SendKeycodeState {
    data object None : SendKeycodeState
    data object ShowDialog : SendKeycodeState
}

@Composable
fun SendKeycodeOperation(
    operation: SendKeycodeState,
    onChange: (SendKeycodeState) -> Unit,
    lifecycleScope: CoroutineScope
) {
    when (operation) {
        is SendKeycodeState.None -> {}
        is SendKeycodeState.ShowDialog -> {
            Keyboard(
                onDismissRequest = {
                    onChange(SendKeycodeState.None)
                },
                onClick = { keyString ->
                    lifecycleScope.launch {
                        lwjglEvent(keyString, isMouse = false, isPressed = true)
                        delay(50)
                        lwjglEvent(keyString, isMouse = false, isPressed = false)
                    }
                }
            )
        }
    }
}
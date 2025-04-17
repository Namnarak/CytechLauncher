package com.movtery.zalithlauncher.ui.screens.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.movtery.zalithlauncher.bridge.Logger
import com.movtery.zalithlauncher.ui.screens.game.elements.LogBox

@Composable
fun GameScreen() {
    var enableLog by remember { mutableStateOf(true) }
    var logText by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
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
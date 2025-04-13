package com.movtery.zalithlauncher.ui.screens.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.movtery.zalithlauncher.bridge.Logger

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

@Composable
private fun LogBox(
    logText: String,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(logText) {
        if (logText.isNotEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(state = scrollState)
        ) {
            SelectionContainer {
                Text(
                    text = logText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
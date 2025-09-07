package com.movtery.zalithlauncher.ui.screens.game.elements

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.bridge.ZLBridgeStates
import com.movtery.zalithlauncher.ui.components.DraggableBox

@Composable
fun DraggableGameBall(
    alignment: Alignment = Alignment.TopCenter,
    showGameFps: Boolean,
    onClick: () -> Unit = {}
) {
    DraggableBox(
        alignment = alignment,
        onClick = onClick
    ) {
        GameBallContent(showGameFps = showGameFps)
    }
}

@Composable
private fun GameBallContent(
    showGameFps: Boolean
) {
    Row(
        modifier = Modifier
            .padding(all = 2.dp)
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(28.dp),
            imageVector = Icons.Default.Settings,
            contentDescription = null
        )
        if (showGameFps) {
            Row(modifier = Modifier.padding(horizontal = 4.dp)) {
                val fps = ZLBridgeStates.currentFPS
                Text(
                    text = "FPS: $fps",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
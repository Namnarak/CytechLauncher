package com.movtery.zalithlauncher.ui.screens.game.elements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
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
import com.movtery.zalithlauncher.ui.screens.content.elements.MemoryPreview

@Composable
fun DraggableGameBall(
    alignment: Alignment = Alignment.TopCenter,
    showGameFps: Boolean,
    showMemory: Boolean,
    onClick: () -> Unit = {}
) {
    DraggableBox(
        alignment = alignment,
        onClick = onClick
    ) {
        GameBallContent(
            showGameFps = showGameFps,
            showMemory = showMemory
        )
    }
}

@Composable
private fun GameBallContent(
    showGameFps: Boolean,
    showMemory: Boolean
) {
    Row(
        modifier = Modifier.padding(all = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(28.dp),
            imageVector = Icons.Default.Settings,
            contentDescription = null
        )

        AnimatedVisibility(
            visible = showGameFps || showMemory
        ) {
            Spacer(Modifier.width(4.dp))
        }

        //实际内容
        Column(
            modifier = Modifier
                .wrapContentSize()
                .animateContentSize()
        ) {
            CustomAnimatedVisibility(
                visible = showGameFps || showMemory
            ) {
                Spacer(Modifier.height(4.dp))
            }
            //帧率显示
            CustomAnimatedVisibility(
                visible = showGameFps
            ) {
                val fps = ZLBridgeStates.currentFPS
                Text(
                    modifier = Modifier.padding(end = 4.dp),
                    text = "FPS: $fps",
                    style = MaterialTheme.typography.labelMedium
                )
            }
            //内存显示
            CustomAnimatedVisibility(
                visible = showMemory
            ) {
                MemoryPreview(
                    modifier = Modifier.width(168.dp).padding(end = 4.dp),
                    mainColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    textStyle = MaterialTheme.typography.labelSmall,
                    usedText = { usedMemory, totalMemory ->
                        "${usedMemory.toInt()}MB/${totalMemory.toInt()}MB"
                    }
                )
            }
            CustomAnimatedVisibility(
                visible = showGameFps || showMemory
            ) {
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun ColumnScope.CustomAnimatedVisibility(
    visible: Boolean,
    content: @Composable (AnimatedVisibilityScope.() -> Unit)
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandIn(expandFrom = Alignment.CenterStart) + fadeIn(),
        exit = shrinkOut(shrinkTowards = Alignment.CenterStart) + fadeOut(),
        content = content
    )
}
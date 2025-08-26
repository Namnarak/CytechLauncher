package com.movtery.zalithlauncher.coroutine

import androidx.compose.ui.graphics.vector.ImageVector

data class TitledTask(
    val title: String,
    val runningIcon: ImageVector? = null,
    val task: Task
)
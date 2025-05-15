package com.movtery.zalithlauncher.game.download.game

import com.movtery.zalithlauncher.coroutine.Task

/**
 * 安装游戏包装Task
 */
data class GameInstallTask(
    val title: String,
    val task: Task
)
package com.movtery.zalithlauncher.game.path

/**
 * 游戏目录
 */
data class GamePathItem(
    /**
     * 单项唯一ID
     */
    val id: String,
    /**
     * 游戏目录的标题
     */
    val title: String,
    /**
     * 目标路径
     */
    val path: String
)

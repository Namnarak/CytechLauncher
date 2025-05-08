package com.movtery.zalithlauncher.game.download.game

import com.movtery.zalithlauncher.game.addons.modloader.AddonVersion

data class GameDownloadInfo(
    /** Minecraft 版本 */
    val gameVersion: String,
    /** 自定义版本名称 */
    val customVersionName: String,
    /** Addon 列表 */
    val addons: List<AddonVersion>
)

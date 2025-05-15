package com.movtery.zalithlauncher.game.addons.modloader.forgelike

import com.movtery.zalithlauncher.game.addons.modloader.AddonVersion

/**
 * [Reference PCL2](https://github.com/Hex-Dragon/PCL2/blob/44aea3e/Plain%20Craft%20Launcher%202/Modules/Minecraft/ModDownload.vb#L512-L563)
 */
open class ForgeLikeVersion(
    /** 加载器展示名称 */
    val loaderName: String,
    /** 标准化后的版本号，仅可用于比较与排序 */
    val forgeBuildVersion: ForgeBuildVersion,
    /** 可对玩家显示的非格式化版本名 */
    val versionName: String,
    /** 对应的 Minecraft 版本 */
    inherit: String,
    /** 文件扩展名 */
    val fileExtension: String
) : AddonVersion(
    inherit = inherit
) {
    /**
     * Forge：MC 版本是否小于 1.13。（1.13+ 的版本号首位都大于 20）
     * NeoForge：MC 版本是否为 1.20.1。（1.20.1 的版本号首位人为规定为 19 开头）
     */
    val isLegacy: Boolean get() = forgeBuildVersion.major < 20
}
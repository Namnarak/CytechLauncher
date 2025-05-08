package com.movtery.zalithlauncher.game.addons.modloader.optifine

import com.movtery.zalithlauncher.game.addons.modloader.AddonVersion

class OptiFineVersion(
    /** 显示名称 */
    val displayName: String,
    /** 文件名称 */
    val fileName: String,
    /** 版本名称 */
    val version: String,
    /** Minecraft 版本 */
    inherit: String,
    /** 发布时间，格式为“yyyy/mm/dd” */
    val releaseDate: String,
    /** 最低需求 Forge 版本：null 为不兼容，空字符串为无限制 */
    val forgeVersion: String?,
    /** 是否为预览版本 */
    val isPreview: Boolean
) : AddonVersion(
    inherit = inherit
)
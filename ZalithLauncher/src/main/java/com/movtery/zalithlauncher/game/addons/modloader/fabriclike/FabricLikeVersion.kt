package com.movtery.zalithlauncher.game.addons.modloader.fabriclike

import com.movtery.zalithlauncher.game.addons.modloader.AddonVersion

open class FabricLikeVersion(
    /** Minecraft 版本 */
    inherit: String,
    /** 加载器名称 */
    val loaderName: String,
    /** 加载器版本 */
    val version: String,
    /** 版本状态: true 为稳定版 (Quilt忽略此值) */
    val stable: Boolean = true
) : AddonVersion(
    inherit = inherit
)
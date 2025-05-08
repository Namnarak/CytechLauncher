package com.movtery.zalithlauncher.game.addons.modloader.fabriclike.models

import kotlinx.serialization.Serializable

@Serializable
data class FabricLikeLoader(
    val separator: String,
    val build: Int,
    val maven: String,
    /** 加载器版本 */
    val version: String,
    /** 版本状态: true 为稳定版 (Quilt忽略此值) */
    val stable: Boolean = true
)
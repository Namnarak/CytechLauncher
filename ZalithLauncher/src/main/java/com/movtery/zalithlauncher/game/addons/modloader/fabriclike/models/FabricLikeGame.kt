package com.movtery.zalithlauncher.game.addons.modloader.fabriclike.models

import kotlinx.serialization.Serializable

@Serializable
data class FabricLikeGame(
    /** 对应的 Minecraft 版本 */
    val version: String,
    /** 是否为正式版 */
    val stable: Boolean
)

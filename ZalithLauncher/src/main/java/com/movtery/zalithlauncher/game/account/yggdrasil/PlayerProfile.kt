package com.movtery.zalithlauncher.game.account.yggdrasil

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class PlayerProfile(
    val id: String,
    val name: String,
    val skins: List<Skin>,
    val capes: List<Cape>,
    val profileActions: JsonElement? = null
) {
    @Serializable
    data class Skin(
        val id: String,
        val state: String,
        val url: String,
        val textureKey: String,
        val variant: String
    )

    @Serializable
    data class Cape(
        val id: String,
        val state: String,
        val url: String,
        val alias: String
    )
}

/**
 * 空披风，可用来表示不选择、重置披风
 */
val EmptyCape = PlayerProfile.Cape("", "", "", "")

package com.movtery.zalithlauncher.game.account.yggdrasil

import com.movtery.zalithlauncher.game.account.wardrobe.SkinModelType
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
 * 该皮肤是否正在使用中
 */
fun PlayerProfile.Skin.isUsing(): Boolean = this.state == "ACTIVE"

/**
 * 该披风是否正在使用中
 */
fun PlayerProfile.Cape.isUsing(): Boolean = this.state == "ACTIVE"

/**
 * 查找玩家当前正在使用的皮肤
 */
fun List<PlayerProfile.Skin>.findUsing(): PlayerProfile.Skin? = this.find { it.isUsing() }

/**
 * 获取玩家皮肤模型类型
 */
fun PlayerProfile.Skin.getSkinModel(): SkinModelType {
    return when (variant) {
        "CLASSIC" -> SkinModelType.STEVE
        "SLIM" -> SkinModelType.ALEX
        else -> SkinModelType.NONE
    }
}
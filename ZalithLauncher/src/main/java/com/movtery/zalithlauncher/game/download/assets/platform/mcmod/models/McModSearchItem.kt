package com.movtery.zalithlauncher.game.download.assets.platform.mcmod.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class McModSearchItem(
    @SerialName("mcmod_id")
    val id : Int,

    @SerialName("mcmod_icon")
    val icon : String,

    @SerialName("mcmod_name")
    val name : String,

    @SerialName("mcmod_author")
    val author : String,

    @SerialName("mcmod_text")
    val text : String,

    @SerialName("mcmod_type")
    val type : Int,

    @SerialName("mcmod_mod_type")
    val modType : String,

    @SerialName("mcmod_mod_subtype")
    val modSubtype : String,

    @SerialName("mcmod_game_version")
    val gameVersion : String,

    @SerialName("mcmod_create_time")
    val createTime : String,

    @SerialName("mcmod_update_time")
    val updateTime : String,

    @SerialName("mcmod_re_time")
    val reTime : String,

    @SerialName("curseforge_url")
    val curseforgeUrl : String?,

    @SerialName("curseforge_id")
    val curseforgeId : String?,

    @SerialName("modrinth_url")
    val modrinthUrl : String?,

    @SerialName("modrinth_id")
    val modrinthId : String?
)
package com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CurseForgeFileIndex(
    @SerialName("gameVersion")
    val gameVersion: String,

    @SerialName("fileId")
    val fileId: Int,

    @SerialName("String")
    val filename: String? = null,

    @SerialName("releaseType")
    val releaseType: CurseForgeFileReleaseType,

    @SerialName("gameVersionTypeId")
    val gameVersionTypeId: Int? = null,

    @SerialName("modLoader")
    val modLoader: CurseForgeModLoader? = null
)
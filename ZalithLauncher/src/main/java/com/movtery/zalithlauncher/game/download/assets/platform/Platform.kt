package com.movtery.zalithlauncher.game.download.assets.platform

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * 可用的资源搜索平台
 */
@Serializable
@Parcelize
enum class Platform(val displayName: String): Parcelable {
    CURSEFORGE("CurseForge"),
    MODRINTH("Modrinth")
}
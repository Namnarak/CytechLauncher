package com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models

import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ModrinthProjectType(val platform: PlatformClasses) {
    @SerialName("mod")
    MOD(PlatformClasses.MOD),

    @SerialName("modpack")
    MODPACK(PlatformClasses.MOD_PACK),

    @SerialName("resourcepack")
    RESOURCEPACK(PlatformClasses.RESOURCE_PACK),

    @SerialName("shader")
    SHADER(PlatformClasses.SHADERS)
}
package com.movtery.zalithlauncher.game.version.installed

import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.version.installed.VersionType.MODLOADERS
import com.movtery.zalithlauncher.game.version.installed.VersionType.UNKNOWN
import com.movtery.zalithlauncher.game.version.installed.VersionType.VANILLA

/**
 * 版本类型，区分原版、模组加载器
 */
enum class VersionType {
    /**
     * 原版
     */
    VANILLA,

    /**
     * 带有模组加载器
     */
    MODLOADERS,

    /**
     * 不清楚，无法判断
     */
    UNKNOWN
}

private val loaders = listOf(
    ModLoader.FORGE, ModLoader.NEOFORGE,
    ModLoader.FABRIC, ModLoader.QUILT,
    ModLoader.LITE_LOADER
)

/**
 * 通过版本信息，尝试识别版本类型
 */
fun VersionInfo?.getVersionType(): VersionType {
    return when {
        this != null -> {
            when {
                loaderInfo == null || loaderInfo.loader == ModLoader.OPTIFINE -> VANILLA
                loaderInfo.loader in loaders -> MODLOADERS
                else -> UNKNOWN
            }
        }
        else -> UNKNOWN
    }
}


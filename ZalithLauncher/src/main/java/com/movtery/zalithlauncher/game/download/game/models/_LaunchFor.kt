package com.movtery.zalithlauncher.game.download.game.models

import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.FabricLikeVersion
import com.movtery.zalithlauncher.game.addons.modloader.forgelike.ForgeLikeVersion
import com.movtery.zalithlauncher.game.addons.modloader.optifine.OptiFineVersion

/**
 * 将 OptiFine 版本转换为 LaunchFor Info
 */
fun OptiFineVersion.toLaunchForInfo(): LaunchFor.Info {
    return LaunchFor.Info(
        version = this.realVersion,
        name = ModLoader.OPTIFINE.displayName
    )
}

/**
 * 将 Forge Like 版本转换为 LaunchFor Info
 */
fun ForgeLikeVersion.toLaunchForInfo(): LaunchFor.Info {
    return LaunchFor.Info(
        version = this.versionName,
        name = this.loaderName
    )
}

/**
 * 将 Fabric Like 版本转换为 LaunchFor Info
 */
fun FabricLikeVersion.toLaunchForInfo(): LaunchFor.Info {
    return LaunchFor.Info(
        version = this.version,
        name = this.loaderName
    )
}
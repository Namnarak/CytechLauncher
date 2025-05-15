package com.movtery.zalithlauncher.game.addons.modloader.fabriclike.fabric

import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.FabricLikeVersion

class FabricVersion(
    inherit: String,
    version: String,
    stable: Boolean
) : FabricLikeVersion(
    inherit = inherit,
    loaderName = ModLoader.FABRIC.displayName,
    version = version,
    stable = stable
)
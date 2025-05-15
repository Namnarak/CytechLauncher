package com.movtery.zalithlauncher.game.addons.modloader.forgelike.neoforge

import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.addons.modloader.forgelike.ForgeBuildVersion
import com.movtery.zalithlauncher.game.addons.modloader.forgelike.ForgeLikeVersion

/**
 * [Reference PCL2](https://github.com/Hex-Dragon/PCL2/blob/44aea3e/Plain%20Craft%20Launcher%202/Modules/Minecraft/ModDownload.vb#L773-L807)
 */
class NeoForgeVersion(
    private val rawVersion: String,
    val isLegacyForge: Boolean
) : ForgeLikeVersion(
    loaderName = ModLoader.NEOFORGE.displayName,
    forgeBuildVersion = parseVersion(rawVersion),
    versionName = parseVersionName(rawVersion),
    inherit = parseInherit(rawVersion),
    fileExtension = "jar"
) {
    val baseUrl: String
        get() {
            val packageName = if (isLegacyForge) "forge" else "neoforge"
            return "https://maven.neoforged.net/releases/net/neoforged/$packageName/$rawVersion/$packageName-$rawVersion"
        }

    val isBeta: Boolean
        get() = rawVersion.contains("beta")

    companion object {
        private fun parseVersion(rawVersion: String): ForgeBuildVersion {
            return when {
                rawVersion.contains("1.20.1") -> {
                    val versionPart = rawVersion.replace("1.20.1-", "")
                    ForgeBuildVersion.parse("19.$versionPart")
                }
                else -> ForgeBuildVersion.parse(rawVersion.substringBefore("-"))
            }
        }

        private fun parseVersionName(rawVersion: String): String {
            return if (rawVersion.contains("1.20.1")) {
                rawVersion.replace("1.20.1-", "")
            } else {
                rawVersion
            }
        }

        private fun parseInherit(rawVersion: String): String {
            return if (rawVersion.contains("1.20.1")) {
                "1.20.1"
            } else {
                val version = parseVersion(rawVersion)
                buildString {
                    append("1.").append(version.major)
                    if (version.minor != 0) append(".").append(version.minor)
                }
            }
        }
    }
}
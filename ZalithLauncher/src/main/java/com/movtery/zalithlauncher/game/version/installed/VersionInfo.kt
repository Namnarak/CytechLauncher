package com.movtery.zalithlauncher.game.version.installed

import android.os.Parcelable
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import kotlinx.parcelize.Parcelize

@Parcelize
class VersionInfo(
    val minecraftVersion: String,
    val quickPlay: QuickPlay,
    val loaderInfo: LoaderInfo?
): Parcelable {
    /**
     * 拼接Minecraft的版本信息，包括ModLoader信息
     * @return 用", "分割的信息字符串
     */
    fun getInfoString(): String {
        val infoList = mutableListOf<String>().apply {
            add(minecraftVersion)
            loaderInfo?.takeIf { it.version.isNotBlank() }?.let { info ->
                add("${info.loader.displayName} - ${info.version}")
            }
        }
        return infoList.joinToString(", ")
    }

    /**
     * [Reference PCL2](https://github.com/Hex-Dragon/PCL2/blob/dc611a982f8f97fab2c4275d1176db484f8549a4/Plain%20Craft%20Launcher%202/Modules/Minecraft/ModMinecraft.vb#L426-L438)
     */
    fun getMcVersionCode(): McVersionCode {
        return when {
            minecraftVersion.contains("w") || minecraftVersion.equals("pending", ignoreCase = true) -> {
                //快照或未发布版本，特殊处理
                McVersionCode(99, 99)
            }

            minecraftVersion.startsWith("1.") -> {
                val parts = minecraftVersion.split(" ", "_", "-", ".")
                val main = parts.getOrNull(1)?.takeIf { it.length <= 2 }?.toIntOrNull() ?: 0
                val sub = parts.getOrNull(2)?.takeIf { it.length <= 2 }?.toIntOrNull() ?: 0
                McVersionCode(main, sub)
            }

            else -> {
                McVersionCode(0, 0)
            }
        }
    }

    data class McVersionCode(
        val main: Int,
        val sub: Int
    )

    @Parcelize
    data class LoaderInfo(
        val loader: ModLoader,
        val version: String
    ): Parcelable {
        /**
         * 通过加载器名称，获得对应的环境变量键名
         */
        fun getLoaderEnvKey(): String? {
            return when(loader) {
                ModLoader.OPTIFINE -> "INST_OPTIFINE"
                ModLoader.FORGE -> "INST_FORGE"
                ModLoader.NEOFORGE -> "INST_NEOFORGE"
                ModLoader.FABRIC -> "INST_FABRIC"
                ModLoader.QUILT -> "INST_QUILT"
                ModLoader.LITE_LOADER -> "INST_LITELOADER"
                else -> null
            }
        }
    }

    @Parcelize
    data class QuickPlay(
        val hasQuickPlaysSupport: Boolean,
        val isQuickPlaySingleplayer: Boolean,
        val isQuickPlayMultiplayer: Boolean
    ): Parcelable
}
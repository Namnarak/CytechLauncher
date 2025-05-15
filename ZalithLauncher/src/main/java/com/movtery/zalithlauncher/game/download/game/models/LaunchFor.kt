package com.movtery.zalithlauncher.game.download.game.models

import com.google.gson.annotations.SerializedName

/**
 * 当前版本的版本信息，在安装过程中写入，为启动器提供更好的版本识别
 */
class LaunchFor(
    @SerializedName("infos")
    val infos: Array<Info>
) {
    class Info(
        /**
         * 版本
         * Minecraft 版本如：1.21.4
         * NeoForge 版本如：21.4.136
         */
        @SerializedName("version")
        val version: String,
        /**
         * 名称
         * 如 Minecraft、NeoForge
         */
        @SerializedName("name")
        val name: String
    )
}
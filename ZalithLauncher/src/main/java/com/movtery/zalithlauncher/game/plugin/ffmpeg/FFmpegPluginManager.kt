package com.movtery.zalithlauncher.game.plugin.ffmpeg

import android.content.pm.ApplicationInfo
import android.util.Log
import java.io.File

object FFmpegPluginManager {
    var libraryPath: String? = null
        private set

    var executablePath: String? = null
        private set

    /**
     * 插件是否可用
     */
    var isAvailable: Boolean = false
        private set

    fun parsePlugin(info: ApplicationInfo) {
        if (info.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
            if (info.packageName == "net.kdt.pojavlaunch.ffmpeg") {
                runCatching {
                    libraryPath = info.nativeLibraryDir
                    val ffmpegExecutable = File(libraryPath, "libffmpeg.so")
                    executablePath = ffmpegExecutable.absolutePath
                    isAvailable = ffmpegExecutable.exists()
                }.onFailure {
                    Log.w("FFmpegPluginManager", "Failed to discover plugin", it)
                }
            }
        }
    }
}
package com.movtery.zalithlauncher.game.plugin.ffmpeg

import android.content.Context
import android.content.pm.PackageManager
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

    /**
     * 加载 FFmpeg 插件
     */
    fun loadPlugin(context: Context) {
        val manager: PackageManager = context.packageManager
        runCatching {
            val info = manager.getPackageInfo(
                "net.kdt.pojavlaunch.ffmpeg",
                PackageManager.GET_SHARED_LIBRARY_FILES
            )
            libraryPath = info.applicationInfo!!.nativeLibraryDir
            val ffmpegExecutable = File(libraryPath, "libffmpeg.so")
            executablePath = ffmpegExecutable.absolutePath
            isAvailable = ffmpegExecutable.exists()
        }.onFailure { e ->
            Log.w("FFmpegPluginManager", "Failed to discover plugin", e)
        }
    }
}
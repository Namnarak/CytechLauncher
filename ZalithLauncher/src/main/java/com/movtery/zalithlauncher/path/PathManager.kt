package com.movtery.zalithlauncher.path

import android.content.Context
import java.io.File

class PathManager {
    companion object {
        lateinit var DIR_FILES_PRIVATE: File
        lateinit var DIR_FILES_EXTERNAL: File
        lateinit var DIR_CACHE: File

        lateinit var DIR_GAME: File
        lateinit var DIR_ACCOUNT: File
        lateinit var DIR_ACCOUNT_SKIN: File

        lateinit var FILE_CRASH_REPORT: File
        lateinit var FILE_SETTINGS: File
        lateinit var FILE_MINECRAFT_VERSIONS: File

        fun refreshPaths(context: Context) {
            DIR_FILES_PRIVATE = context.filesDir
            DIR_FILES_EXTERNAL = context.getExternalFilesDir(null)!!
            DIR_CACHE = context.cacheDir

            DIR_GAME = File(DIR_FILES_PRIVATE, "games")
            DIR_ACCOUNT = File(DIR_GAME, "accounts")
            DIR_ACCOUNT_SKIN = File(DIR_ACCOUNT, "skins")

            FILE_CRASH_REPORT = File(DIR_FILES_EXTERNAL, "crash_report.log")
            FILE_SETTINGS = File(DIR_FILES_PRIVATE, "settings.json")
            FILE_MINECRAFT_VERSIONS = File(DIR_GAME, "minecraft_versions.json")

            createDirs()
        }

        private fun createDirs() {
            DIR_GAME.mkdirs()
            DIR_ACCOUNT.mkdirs()
            DIR_ACCOUNT_SKIN.mkdirs()
        }
    }
}
package com.movtery.zalithlauncher.path

import android.content.Context
import java.io.File

class PathManager {
    companion object {
        lateinit var DIR_FILES_PRIVATE: File
        lateinit var DIR_FILES_EXTERNAL: File
        lateinit var DIR_CACHE: File

        lateinit var FILE_CRASH_REPORT: File
        lateinit var FILE_SETTINGS: File

        fun refreshPaths(context: Context) {
            DIR_FILES_PRIVATE = context.filesDir
            DIR_FILES_EXTERNAL = context.getExternalFilesDir(null)!!
            DIR_CACHE = context.cacheDir

            FILE_CRASH_REPORT = File(DIR_FILES_EXTERNAL, "crash_report.log")
            FILE_SETTINGS = File(DIR_FILES_PRIVATE, "settings.json")
        }
    }
}
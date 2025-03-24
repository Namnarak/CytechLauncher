package com.movtery.zalithlauncher

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import com.movtery.zalithlauncher.context.Contexts
import com.movtery.zalithlauncher.path.PathManager
import java.io.PrintStream
import java.text.DateFormat
import java.util.Date

class ZLApplication : Application() {
    override fun onCreate() {
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            runCatching {
                PrintStream(PathManager.FILE_CRASH_REPORT).use { stream ->
                    stream.append("================ ZalithLauncher Crash Report ================\n")
                    stream.append("- Time: ${DateFormat.getDateTimeInstance().format(Date())}\n")
                    stream.append("- Device: ${Build.PRODUCT} ${Build.MODEL}\n")
                    stream.append("- Android Version: ${Build.VERSION.RELEASE}\n")
                    stream.append("- Launcher Version: test\n")
                    stream.append("===================== Crash Stack Trace =====================\n")
                    stream.append(Log.getStackTraceString(throwable))
                }
            }.onFailure { t ->
                Log.e("Application", "An exception occurred while saving the crash report: ", t)
                Log.e("Application", "Crash stack trace: ", throwable)
            }
        }

        super.onCreate()
        PathManager.DIR_FILES_PRIVATE = getDir("files", MODE_PRIVATE)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(Contexts.getWrapper(base))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Contexts.refresh(this)
    }
}
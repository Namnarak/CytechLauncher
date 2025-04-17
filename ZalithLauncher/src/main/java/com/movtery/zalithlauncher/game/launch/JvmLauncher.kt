package com.movtery.zalithlauncher.game.launch

import android.app.Activity
import android.net.Uri
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.bridge.Logger
import com.movtery.zalithlauncher.game.multirt.Runtime
import com.movtery.zalithlauncher.game.multirt.RuntimesManager
import com.movtery.zalithlauncher.game.path.getGameHome
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.activities.runJar
import com.movtery.zalithlauncher.utils.string.StringUtils.Companion.getMessageOrToString
import com.movtery.zalithlauncher.utils.string.StringUtils.Companion.splitPreservingQuotes
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

open class JvmLauncher(
    private val activity: Activity,
    private val jarInfo: JarInfo,
    private val callFinish: () -> Unit = {}
) : Launcher() {
    companion object {
        fun executeJarWithUri(activity: Activity, uri: Uri, jreName: String? = null) {
            runCatching {
                val cacheFile = File(PathManager.DIR_CACHE, "temp-jar.jar")
                activity.contentResolver.openInputStream(uri)?.use { contentStream ->
                    FileOutputStream(cacheFile).use { fileOutputStream ->
                        contentStream.copyTo(fileOutputStream)
                    }
                    runJar(activity, cacheFile, jreName, null)
                } ?: throw IOException("Failed to open content stream")
            }.onFailure { e ->
                finalErrorDialog(activity, e.getMessageOrToString())
            }
        }

        private fun finalErrorDialog(
            activity: Activity,
            error: String,
            onDismiss: () -> Unit = {}
        ) {
            activity.runOnUiThread {
                MaterialAlertDialogBuilder(activity)
                    .setTitle(R.string.generic_error)
                    .setMessage(error)
                    .setCancelable(false)
                    .setPositiveButton(R.string.generic_confirm) { dialog, _ ->
                        dialog.dismiss()
                        onDismiss()
                    }
                    .show()
            }
        }
    }

    private val jarFile = File(jarInfo.jarPath)

    override suspend fun launch() {
        redirectAndPrintJRELog()
        val (runtime, argList) = getStartupNeeded() ?: return
        launchJvm(activity, runtime, argList, AllSettings.jvmArgs.getValue())
    }

    override fun chdir(): String {
        return getGameHome()
    }

    override fun getLogName(): String = "latest_jvm"

    private fun getStartupNeeded(): Pair<Runtime, List<String>>? {
        val args = jarInfo.jvmArgs?.splitPreservingQuotes()

        val runtime = jarInfo.jreName?.let { jreName ->
            RuntimesManager.forceReload(jreName)
        } ?: run {
            selectRuntime()
        } ?: return null

        val argList: MutableList<String> = ArrayList(
            getCacioJavaArgs(runtime.javaVersion == 8)
        ).apply {
            args?.let { val1 -> addAll(val1) }
            //Jar文件路径
            add("-jar")
            add(jarFile.absolutePath)
        }

        Logger.appendToLog("--------- Start launching the jvm")
        Logger.appendToLog("Info: Java arguments: \r\n${argList.joinToString("\r\n")}")
        Logger.appendToLog("---------\r\n")

        return Pair(runtime, argList)
    }

    /**
     * [Modified from PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher/blob/19dc51fa8a8ec73a4c67e1a70deaaec63e58ab78/app_pojavlauncher/src/main/java/net/kdt/pojavlaunch/JavaGUILauncherActivity.java#L200-L219)
     */
    private fun selectRuntime(): Runtime? {
        val javaVersion = RuntimesManager.getJavaVersionFromJar(jarFile)
        if (javaVersion == -1) {
            //读取失败，使用启动器默认 Java 环境
            return RuntimesManager.forceReload(AllSettings.javaRuntime.getValue())
        }

        val nearestRuntime = RuntimesManager.getNearestJreName(javaVersion)
            ?: run {
                finalErrorDialog(activity, activity.getString(R.string.multirt_no_compatible_multirt, javaVersion), callFinish)
                return null
            }

        val selectedRuntime = RuntimesManager.forceReload(nearestRuntime)
        val selectedJavaVersion = maxOf(javaVersion, selectedRuntime.javaVersion)

        // Don't allow versions higher than Java 17 because our caciocavallo implementation does not allow for it
        if (selectedJavaVersion > 17) {
            finalErrorDialog(activity, activity.getString(R.string.execute_jar_incompatible_runtime, selectedJavaVersion), callFinish)
            return null
        }

        return selectedRuntime
    }
}
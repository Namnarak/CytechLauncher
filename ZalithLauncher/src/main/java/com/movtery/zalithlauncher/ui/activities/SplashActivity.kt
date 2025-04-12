package com.movtery.zalithlauncher.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.movtery.zalithlauncher.SplashException
import com.movtery.zalithlauncher.components.Components
import com.movtery.zalithlauncher.components.InstallableItem
import com.movtery.zalithlauncher.components.UnpackComponentsTask
import com.movtery.zalithlauncher.components.jre.Jre
import com.movtery.zalithlauncher.components.jre.UnpackJreTask
import com.movtery.zalithlauncher.context.readAssetFile
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.state.ColorThemeState
import com.movtery.zalithlauncher.state.LocalColorThemeState
import com.movtery.zalithlauncher.ui.base.BaseComponentActivity
import com.movtery.zalithlauncher.ui.screens.splash.SplashScreen
import com.movtery.zalithlauncher.ui.theme.ZalithLauncherTheme
import com.movtery.zalithlauncher.utils.getSystemLanguage
import com.movtery.zalithlauncher.utils.string.StringUtils.Companion.getLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseComponentActivity() {
    private val unpackItems: MutableList<InstallableItem> = ArrayList()
    private var finishedTaskCount by mutableIntStateOf(0)
    private var eulaDate: String = AllSettings.splashEulaDate.getValue()
    private var eulaText: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eulaText = getEulaText()

        initUnpackItems()
        checkAllTask()

        setContent {
            val colorThemeState = remember { ColorThemeState() }

            if (eulaText == null && checkTasks()) return@setContent

            CompositionLocalProvider(
                LocalColorThemeState provides colorThemeState
            ) {
                ZalithLauncherTheme {
                    SplashScreen(
                        eulaText = eulaText,
                        eulaDate = eulaDate,
                        checkTasks = { checkTasks() },
                        startAllTask = { startAllTask() },
                        unpackItems = unpackItems
                    )
                }
            }
        }
    }

    private fun getEulaText(): String? {
        val language = getSystemLanguage()
        val fileName = when(language) {
            "zh_cn" -> "eula_zh-cn.txt"
            else -> "eula.txt"
        }
        val eulaText: String = runCatching {
            readAssetFile(fileName)
        }.onFailure {
            Log.e("SplashActivity", "Failed to read $fileName", it)
        }.getOrNull() ?: return null

        val newDate = eulaText.getLine(2)?.also {
            Log.i("SplashActivity", "The content of the date line of the existing EULA has been read: $it")
        } ?: return null
        if (eulaDate != newDate) {
            eulaDate = newDate
            return eulaText
        } else {
            return null
        }
    }

    private fun initUnpackItems() {
        Components.entries.forEach { component ->
            val task = UnpackComponentsTask(this@SplashActivity, component)
            if (!task.isCheckFailed()) {
                unpackItems.add(
                    InstallableItem(
                        component.displayName,
                        getString(component.summary),
                        task
                    )
                )
            }
        }
        Jre.entries.forEach { jre ->
            val task = UnpackJreTask(this@SplashActivity, jre)
            if (!task.isCheckFailed()) {
                unpackItems.add(
                    InstallableItem(
                        jre.jreName,
                        getString(jre.summary),
                        task
                    )
                )
            }
        }
        unpackItems.sort()
    }

    private fun checkAllTask() {
        unpackItems.forEach { item ->
            if (!item.task.isNeedUnpack()) {
                item.isFinished = true
                finishedTaskCount++
            }
        }
    }

    private fun startAllTask() {
        lifecycleScope.launch {
            val jobs = unpackItems
                .filter { !it.isFinished }
                .map { item ->
                    launch(Dispatchers.IO) {
                        item.isRunning = true
                        runCatching {
                            item.task.run()
                        }.onFailure {
                            throw SplashException(it)
                        }
                        finishedTaskCount++
                        item.isRunning = false
                        item.isFinished = true
                    }
                }
            jobs.joinAll()
        }.invokeOnCompletion {
            AllSettings.javaRuntime.apply {
                //检查并设置默认的Java环境
                if (getValue().isEmpty()) put(Jre.JRE_8.jreName).save()
            }
            checkTasks()
        }
    }

    private fun checkTasks(): Boolean {
        val toMain = finishedTaskCount >= unpackItems.size
        if (toMain) {
            Log.i("SplashActivity", "All content that needs to be extracted is already the latest version!")
            swapToMain()
        }
        return toMain
    }

    private fun swapToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
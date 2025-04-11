package com.movtery.zalithlauncher.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.movtery.zalithlauncher.SplashException
import com.movtery.zalithlauncher.components.InstallableItem
import com.movtery.zalithlauncher.components.jre.Jre
import com.movtery.zalithlauncher.components.jre.UnpackJreTask
import com.movtery.zalithlauncher.context.readAssetFile
import com.movtery.zalithlauncher.info.InfoDistributor
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.state.ColorThemeState
import com.movtery.zalithlauncher.state.LocalColorThemeState
import com.movtery.zalithlauncher.state.MutableStates
import com.movtery.zalithlauncher.ui.base.BaseComponentActivity
import com.movtery.zalithlauncher.ui.components.DownShadow
import com.movtery.zalithlauncher.ui.screens.main.EULA_SCREEN_TAG
import com.movtery.zalithlauncher.ui.screens.main.EulaScreen
import com.movtery.zalithlauncher.ui.screens.main.UNPACK_SCREEN_TAG
import com.movtery.zalithlauncher.ui.screens.main.UnpackScreen
import com.movtery.zalithlauncher.ui.screens.navigateTo
import com.movtery.zalithlauncher.ui.theme.ZalithLauncherTheme
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
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
                    Column {
                        TopBar(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .background(color = MaterialTheme.colorScheme.primaryContainer)
                                .zIndex(10f),
                            textColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            val backgroundColor = if (isSystemInDarkTheme()) {
                                MaterialTheme.colorScheme.surfaceContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }

                            NavigationUI(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color = backgroundColor)
                            )

                            DownShadow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.TopStart),
                                height = 4.dp
                            )
                        }
                    }
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

    @Composable
    private fun TopBar(
        modifier: Modifier = Modifier,
        textColor: Color
    ) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = InfoDistributor.LAUNCHER_NAME,
                color = textColor
            )
        }
    }

    @Composable
    private fun NavigationUI(
        modifier: Modifier = Modifier
    ) {
        val navController = rememberNavController()

        LaunchedEffect(navController) {
            val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
                MutableStates.splashScreenTag = destination.route
            }
            navController.addOnDestinationChangedListener(listener)
        }

        val startDestination = if (eulaText != null) EULA_SCREEN_TAG else UNPACK_SCREEN_TAG

        NavHost(
            modifier = modifier,
            navController = navController,
            startDestination = startDestination,
            enterTransition = {
                fadeIn(animationSpec = getAnimateTween())
            },
            exitTransition = {
                fadeOut(animationSpec = getAnimateTween())
            }
        ) {
            composable(
                route = EULA_SCREEN_TAG
            ) {
                EulaScreen(eulaText!!) {
                    navController.navigateTo(UNPACK_SCREEN_TAG)
                    AllSettings.splashEulaDate.put(eulaDate).save()
                    checkTasks()
                }
            }
            composable(
                route = UNPACK_SCREEN_TAG
            ) {
                UnpackScreen(unpackItems) {
                    startAllTask()
                }
            }
        }
    }
}
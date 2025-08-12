package com.movtery.zalithlauncher.ui.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.notification.NotificationManager
import com.movtery.zalithlauncher.ui.base.BaseComponentActivity
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.LaunchGameOperation
import com.movtery.zalithlauncher.ui.screens.main.MainScreen
import com.movtery.zalithlauncher.ui.screens.navigateTo
import com.movtery.zalithlauncher.ui.theme.ZalithLauncherTheme
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.LaunchGameViewModel
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import kotlinx.coroutines.launch

class MainActivity : BaseComponentActivity() {
    /**
     * 屏幕堆栈管理ViewModel
     */
    private val screenBackStackModel: ScreenBackStackViewModel by viewModels()

    /**
     * 启动游戏ViewModel
     */
    private val launchGameViewModel: LaunchGameViewModel by viewModels()

    /**
     * 错误信息ViewModel
     */
    private val errorViewModel: ErrorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //初始化通知管理（创建渠道）
        NotificationManager.initManager(this)

        //错误信息展示
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                errorViewModel.errorEvents.collect { tm ->
                    //展示一个一次性的错误信息对话框
                    MaterialAlertDialogBuilder(this@MainActivity)
                        .setTitle(tm.title)
                        .setMessage(tm.message)
                        .setPositiveButton(R.string.generic_confirm) { dialog, _ ->
                            dialog.dismiss()
                        }.setCancelable(false)
                        .show()
                }
            }
        }

        setContent {
            ZalithLauncherTheme {
                Box {
                    MainScreen(
                        screenBackStackModel = screenBackStackModel,
                        launchGameViewModel = launchGameViewModel,
                        summitError = {
                            errorViewModel.showError(it)
                        }
                    )

                    //启动游戏操作流程
                    LaunchGameOperation(
                        activity = this@MainActivity,
                        launchGameOperation = launchGameViewModel.launchGameOperation,
                        updateOperation = { launchGameViewModel.updateOperation(it) },
                        summitError = {
                            errorViewModel.showError(it)
                        },
                        toAccountManageScreen = {
                            screenBackStackModel.mainScreenBackStack.navigateTo(NormalNavKey.AccountManager)
                        },
                        toVersionManageScreen = {
                            screenBackStackModel.mainScreenBackStack.navigateTo(NormalNavKey.VersionsManager)
                        }
                    )

                    LauncherVersion(
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (VersionsManager.versions.value.isEmpty()) {
            VersionsManager.refresh()
        }
    }
}
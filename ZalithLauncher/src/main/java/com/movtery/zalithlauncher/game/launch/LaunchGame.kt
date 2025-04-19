package com.movtery.zalithlauncher.game.launch

import android.content.Context
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.version.download.DownloadMode
import com.movtery.zalithlauncher.game.version.download.MinecraftDownloader
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.state.ObjectStates
import com.movtery.zalithlauncher.ui.activities.runGame
import com.movtery.zalithlauncher.utils.network.NetWorkUtils

object LaunchGame {
    private var isLaunching: Boolean = false

    fun launchGame(context: Context) {
        if (isLaunching) return
        val version = VersionsManager.currentVersion ?: return

        isLaunching = true

        val downloadTask = MinecraftDownloader(
            context = context,
            version = version.getVersionInfo()?.minecraftVersion ?: version.getVersionName(),
            customName = version.getVersionName(),
            verifyIntegrity = !AllSettings.skipGameIntegrityCheck.getValue(),
            mode = DownloadMode.VERIFY_AND_REPAIR,
            onCompletion = {
                runGame(context, version)
            }
        ).getDownloadTask()

        fun runDownloadTask() {
            TaskSystem.submitTask(downloadTask) { isLaunching = false }
        }

        val loginTask = if (NetWorkUtils.isNetworkAvailable(context)) {
            AccountsManager.performLoginTask(
                context,
                AccountsManager.getCurrentAccount()!!,
                onFailed = { error ->
                    ObjectStates.updateThrowable(
                        ObjectStates.ThrowableMessage(
                            title = context.getString(R.string.account_logging_in_failed),
                            message = error
                        )
                    )
                },
                onFinally = { runDownloadTask() }
            )
        } else {
            null
        }

        loginTask?.let { task ->
            TaskSystem.submitTask(task)
        } ?: run {
            runDownloadTask()
        }
    }
}

package com.movtery.zalithlauncher.game.launch

import android.content.Context
import android.widget.Toast
import androidx.navigation.NavController
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.version.download.DownloadMode
import com.movtery.zalithlauncher.game.version.download.MinecraftDownloader
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.state.ObjectStates
import com.movtery.zalithlauncher.ui.activities.runGame
import com.movtery.zalithlauncher.ui.screens.content.ACCOUNT_MANAGE_SCREEN_TAG
import com.movtery.zalithlauncher.ui.screens.content.VERSIONS_MANAGE_SCREEN_TAG
import com.movtery.zalithlauncher.ui.screens.navigateTo
import com.movtery.zalithlauncher.utils.network.NetWorkUtils

object LaunchGame {
    private var isLaunching: Boolean = false

    fun launchGame(
        context: Context,
        navController: NavController
    ) {
        if (isLaunching) return

        val version = VersionsManager.currentVersion ?: run {
            Toast.makeText(context, R.string.game_launch_no_version, Toast.LENGTH_SHORT).show()
            navController.navigateTo(VERSIONS_MANAGE_SCREEN_TAG)
            return
        }

        val account = AccountsManager.getCurrentAccount() ?: run {
            Toast.makeText(context, R.string.game_launch_no_account, Toast.LENGTH_SHORT).show()
            navController.navigateTo(ACCOUNT_MANAGE_SCREEN_TAG)
            return
        }

        isLaunching = true

        val downloadTask = MinecraftDownloader(
            context = context,
            version = version.getVersionInfo()?.minecraftVersion ?: version.getVersionName(),
            customName = version.getVersionName(),
            verifyIntegrity = !version.skipGameIntegrityCheck(),
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
                context = context,
                account = account,
                onSuccess = { acc, _ ->
                    acc.save()
                },
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

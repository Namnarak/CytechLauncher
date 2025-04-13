package com.movtery.zalithlauncher.game.launch

import android.app.Activity
import com.movtery.zalithlauncher.game.multirt.Runtime
import com.movtery.zalithlauncher.game.path.getGameHome

open class DefaultLauncher(
    private val activity: Activity,
    private val runtime: Runtime,
    private val jvmArgs: List<String>,
    private val userArgs: String
) : Launcher() {
    override suspend fun launch() {
        redirectAndPrintJRELog()
        launchJvm(activity, runtime, jvmArgs, userArgs)
    }

    override fun chdir(): String {
        return getGameHome()
    }
}
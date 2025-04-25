package com.movtery.zalithlauncher.ui.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.ui.base.BaseComponentActivity
import com.movtery.zalithlauncher.ui.screens.main.MainScreen
import com.movtery.zalithlauncher.ui.theme.ZalithLauncherTheme

class MainActivity : BaseComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZalithLauncherTheme {
                MainScreen()
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
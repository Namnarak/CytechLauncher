package com.movtery.zalithlauncher.ui.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.notification.NotificationManager
import com.movtery.zalithlauncher.ui.base.BaseComponentActivity
import com.movtery.zalithlauncher.ui.screens.main.MainScreen
import com.movtery.zalithlauncher.ui.theme.ZalithLauncherTheme

class MainActivity : BaseComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //初始化通知管理（创建渠道）
        NotificationManager.initManager(this)

        setContent {
            ZalithLauncherTheme {
                Box {
                    MainScreen()
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
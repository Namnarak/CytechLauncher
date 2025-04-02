package com.movtery.zalithlauncher.ui.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.movtery.zalithlauncher.state.ColorThemeState
import com.movtery.zalithlauncher.state.LocalColorThemeState
import com.movtery.zalithlauncher.state.LocalMainScreenTag
import com.movtery.zalithlauncher.state.MainScreenTagState
import com.movtery.zalithlauncher.ui.base.BaseComponentActivity
import com.movtery.zalithlauncher.ui.screens.main.MainScreen
import com.movtery.zalithlauncher.ui.theme.ZalithLauncherTheme

class MainActivity : BaseComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val colorThemeState = remember { ColorThemeState() }
            val mainScreenTagState = remember { MainScreenTagState() }

            CompositionLocalProvider(
                LocalColorThemeState provides colorThemeState,
                LocalMainScreenTag provides mainScreenTagState
            ) {
                ZalithLauncherTheme {
                    MainScreen()
                }
            }
        }
    }
}
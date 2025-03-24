package com.movtery.zalithlauncher.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.screens.settings.layouts.SettingsBackground

const val LAUNCHER_SETTINGS_TAG = "LauncherSettingsScreen"

@Composable
fun LauncherSettingsScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(state = rememberScrollState())
            .padding(all = 12.dp)
    ) {
        SettingsBackground {
            SliderSettingsLayout(
                unit = AllSettings.launcherAnimateSpeed,
                title = stringResource(R.string.settings_launcher_animate_speed_title),
                summary = stringResource(R.string.settings_launcher_animate_speed_summary),
                valueRange = 0f..10f,
                steps = 10,
                suffix = "x"
            )
        }
    }
}
package com.movtery.zalithlauncher.ui.screens.content.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.state.MutableStates
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.control.mouse.ControlMode
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsBackground
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState

const val CONTROL_SETTINGS_SCREEN_TAG = "ControlSettingsScreen"

@Composable
fun ControlSettingsScreen() {
    BaseScreen(
        screenTag = CONTROL_SETTINGS_SCREEN_TAG,
        currentTag = MutableStates.settingsScreenTag
    ) { isVisible ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(state = rememberScrollState())
                .padding(all = 12.dp)
        ) {
            val yOffset by swapAnimateDpAsState(
                targetValue = (-40).dp,
                swapIn = isVisible
            )

            SettingsBackground(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = 0,
                            y = yOffset.roundToPx()
                        )
                    }
            ) {
                SliderSettingsLayout(
                    unit = AllSettings.mouseSize,
                    title = stringResource(R.string.settings_control_mouse_size_title),
                    valueRange = 10f..50f,
                    suffix = "Dp",
                    fineTuningControl = true
                )

                SliderSettingsLayout(
                    unit = AllSettings.mouseSpeed,
                    title = stringResource(R.string.settings_control_mouse_speed_title),
                    valueRange = 25f..300f,
                    suffix = "%",
                    fineTuningControl = true
                )

                EnumSettingsLayout(
                    unit = AllSettings.mouseControlMode,
                    entries = ControlMode.entries,
                    title = stringResource(R.string.settings_control_mouse_control_mode_title),
                    summary = stringResource(R.string.settings_control_mouse_control_mode_summary),
                    getRadioText = { enum ->
                        stringResource(enum.nameRes)
                    },
                    getRadioEnable = { true }
                )
            }
        }
    }
}
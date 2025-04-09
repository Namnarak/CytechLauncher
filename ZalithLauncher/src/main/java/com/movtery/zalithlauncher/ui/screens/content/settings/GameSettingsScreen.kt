package com.movtery.zalithlauncher.ui.screens.content.settings

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.movtery.zalithlauncher.game.multirt.RuntimesManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.state.MutableStates
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsBackground
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.animation.getAnimateTweenBounce

const val GAME_SETTINGS_TAG = "GameSettingsScreen"

@Composable
fun GameSettingsScreen() {
    BaseScreen(
        screenTag = GAME_SETTINGS_TAG,
        currentTag = MutableStates.settingsScreenTag
    ) { isVisible ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(state = rememberScrollState())
                .padding(all = 12.dp)
        ) {
            val yOffset1 by animateDpAsState(
                targetValue = if (isVisible) 0.dp else (-40).dp,
                animationSpec = if (isVisible) getAnimateTweenBounce() else getAnimateTween()
            )

            SettingsBackground(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = 0,
                            y = yOffset1.roundToPx()
                        )
                    }
            ) {
                SwitchSettingsLayout(
                    unit = AllSettings.versionIsolation,
                    title = stringResource(R.string.settings_game_version_isolation_title),
                    summary = stringResource(R.string.settings_game_version_isolation_summary)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            val yOffset2 by animateDpAsState(
                targetValue = if (isVisible) 0.dp else (-40).dp,
                animationSpec = if (isVisible) getAnimateTweenBounce(delayMillis = 50)
                else getAnimateTween(delayMillis = 50)
            )
            SettingsBackground(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = 0,
                            y = yOffset2.roundToPx()
                        )
                    }
            ) {
                ListSettingsLayout(
                    unit = AllSettings.javaRuntime,
                    items = RuntimesManager.getRuntimes(),
                    title = stringResource(R.string.settings_game_java_runtime_title),
                    summary = stringResource(R.string.settings_game_java_runtime_summary),
                    getItemText = { it.name },
                    getItemId = { it.name }
                )
            }
        }
    }
}
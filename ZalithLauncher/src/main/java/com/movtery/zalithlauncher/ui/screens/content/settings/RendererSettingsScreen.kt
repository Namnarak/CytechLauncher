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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.renderer.Renderers
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.state.MutableStates
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsBackground
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState

const val RENDERER_SETTINGS_SCREEN_TAG = "RendererSettingsScreen"

@Composable
fun RendererSettingsScreen() {
    BaseScreen(
        screenTag = RENDERER_SETTINGS_SCREEN_TAG,
        currentTag = MutableStates.settingsScreenTag
    ) { isVisible ->
        val context = LocalContext.current

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(state = rememberScrollState())
                .padding(all = 12.dp)
        ) {
            val yOffset by swapAnimateDpAsState(
                targetValue =  (-40).dp,
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
                val allRenderers = Renderers.getCompatibleRenderers(context).second
                ListSettingsLayout(
                    unit = AllSettings.renderer,
                    items = allRenderers,
                    title = stringResource(R.string.settings_renderer_global_renderer_title),
                    summary = stringResource(R.string.settings_renderer_global_renderer_summary),
                    getItemText = { it.getRendererName() },
                    getItemId = { it.getUniqueIdentifier() }
                )

                SliderSettingsLayout(
                    unit = AllSettings.resolutionRatio,
                    title = stringResource(R.string.settings_renderer_resolution_scale_title),
                    summary = stringResource(R.string.settings_renderer_resolution_scale_summary),
                    valueRange = 25f..300f,
                    suffix = "%"
                )

                SwitchSettingsLayout(
                    unit = AllSettings.sustainedPerformance,
                    title = stringResource(R.string.settings_renderer_sustained_performance_title),
                    summary = stringResource(R.string.settings_renderer_sustained_performance_summary)
                )
            }
        }
    }
}
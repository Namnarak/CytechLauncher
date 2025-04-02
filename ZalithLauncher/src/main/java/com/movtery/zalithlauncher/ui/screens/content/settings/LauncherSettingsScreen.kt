package com.movtery.zalithlauncher.ui.screens.content.settings

import androidx.compose.animation.core.animateDpAsState
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
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.state.LocalColorThemeState
import com.movtery.zalithlauncher.state.LocalSettingsScreenTag
import com.movtery.zalithlauncher.ui.base.BaseComponentActivity
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsBackground
import com.movtery.zalithlauncher.ui.theme.ColorThemeType
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.animation.getAnimateTweenBounce

const val LAUNCHER_SETTINGS_TAG = "LauncherSettingsScreen"

@Composable
fun LauncherSettingsScreen() {
    val context = LocalContext.current

    BaseScreen(
        screenTag = LAUNCHER_SETTINGS_TAG,
        tagProvider = LocalSettingsScreenTag
    ) { isVisible ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(state = rememberScrollState())
                .padding(all = 12.dp)
        ) {
            val currentColorThemeState = LocalColorThemeState.current

            val yOffset by animateDpAsState(
                targetValue = if (isVisible) 0.dp else (-40).dp,
                animationSpec = if (isVisible) getAnimateTweenBounce() else getAnimateTween()
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
                EnumSettingsLayout(
                    unit = AllSettings.launcherColorTheme,
                    title = stringResource(R.string.settings_launcher_color_theme_title),
                    summary = stringResource(R.string.settings_launcher_color_theme_summary),
                    entries = ColorThemeType.entries,
                    getRadioText = { enum ->
                        when (enum) {
                            ColorThemeType.DYNAMIC -> stringResource(R.string.theme_color_dynamic)
                            ColorThemeType.EMBERMIRE -> stringResource(R.string.theme_color_embermire)
                            ColorThemeType.VELVET_ROSE -> stringResource(R.string.theme_color_velvet_rose)
                            ColorThemeType.MISTWAVE -> stringResource(R.string.theme_color_mistwave)
                            ColorThemeType.GLACIER -> stringResource(R.string.theme_color_glacier)
                            ColorThemeType.VERDANTFIELD -> stringResource(R.string.theme_color_verdant_field)
                            ColorThemeType.ASHVEIL -> stringResource(R.string.theme_color_ashveil)
                            ColorThemeType.URBAN_ASH -> stringResource(R.string.theme_color_urban_ash)
                            ColorThemeType.VERDANT_DAWN -> stringResource(R.string.theme_color_verdant_dawn)
                            ColorThemeType.CELESTINE_VEIL -> stringResource(R.string.theme_color_celestine_veil)
                        }
                    }
                ) { type ->
                    currentColorThemeState.updateValue(type)
                }

                SwitchSettingsLayout(
                    unit = AllSettings.launcherFullScreen,
                    title = stringResource(R.string.settings_launcher_full_screen_title),
                    summary = stringResource(R.string.settings_launcher_full_screen_summary)
                ) {
                    val activity = context as? BaseComponentActivity
                    activity?.fullScreenViewModel?.triggerRefresh()
                }

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
}
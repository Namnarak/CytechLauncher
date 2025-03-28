package com.movtery.zalithlauncher.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.state.LocalColorThemeState
import com.movtery.zalithlauncher.ui.base.BaseComponentActivity
import com.movtery.zalithlauncher.ui.screens.settings.layouts.SettingsBackground
import com.movtery.zalithlauncher.ui.theme.ColorThemeType

const val LAUNCHER_SETTINGS_TAG = "LauncherSettingsScreen"

@Composable
fun LauncherSettingsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(state = rememberScrollState())
            .padding(all = 12.dp)
    ) {
        val currentColorThemeState = LocalColorThemeState.current

        SettingsBackground {
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
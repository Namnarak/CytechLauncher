package com.movtery.zalithlauncher.ui.screens.content

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.state.MutableStates
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.screens.content.settings.GAME_SETTINGS_TAG
import com.movtery.zalithlauncher.ui.screens.content.settings.GameSettingsScreen
import com.movtery.zalithlauncher.ui.screens.content.settings.LAUNCHER_SETTINGS_TAG
import com.movtery.zalithlauncher.ui.screens.content.settings.LauncherSettingsScreen
import com.movtery.zalithlauncher.ui.screens.content.settings.RENDERER_SETTINGS_SCREEN_TAG
import com.movtery.zalithlauncher.ui.screens.content.settings.RendererSettingsScreen
import com.movtery.zalithlauncher.ui.screens.navigateOnce
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.animation.getAnimateTweenBounce

const val SETTINGS_SCREEN_TAG = "SettingsScreen"

@Composable
fun SettingsScreen(
    mainNavController: NavController
) {
    BaseScreen(
        screenTag = SETTINGS_SCREEN_TAG,
        currentTag = MutableStates.mainScreenTag
    ) { isVisible ->
        val settingsNavController = rememberNavController()

        if (!isVisible) {
            MutableStates.settingsScreenTag = null
        }

        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            TabMenu(
                isVisible = isVisible,
                settingsNavController = settingsNavController,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(2.5f)
                    .padding(start = 12.dp, top = 12.dp, bottom = 12.dp)
            )

            Box(
                modifier = Modifier.weight(7.5f)
            ) {
                NavigationUI(
                    mainNavController = mainNavController,
                    settingsNavController = settingsNavController
                )
            }
        }
    }
}

@Composable
private fun TabMenu(
    isVisible: Boolean,
    settingsNavController: NavController,
    modifier: Modifier = Modifier
) {
    val xOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else (-40).dp,
        animationSpec = if (isVisible) getAnimateTweenBounce() else getAnimateTween()
    )

    Surface(
        modifier = modifier.offset {
            IntOffset(
                x = xOffset.roundToPx(),
                y = 0
            )
        },
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.inversePrimary,
        shadowElevation = 4.dp
    ) {
        val settingItems = listOf(
            SettingsItem(RENDERER_SETTINGS_SCREEN_TAG, R.drawable.ic_setting_renderer, R.string.settings_tab_renderer, PaddingValues(all = 1.dp)),
            SettingsItem(GAME_SETTINGS_TAG, R.drawable.ic_setting_game, R.string.settings_tab_game, PaddingValues(all = 2.dp)),
            SettingsItem(LAUNCHER_SETTINGS_TAG, R.drawable.ic_setting_launcher, R.string.settings_tab_launcher)
        )
        LazyColumn(
            contentPadding = PaddingValues(all = 12.dp)
        ) {
            items(settingItems.size) { index ->
                val item = settingItems[index]
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            painter = painterResource(item.iconRes),
                            contentDescription = stringResource(item.textRes),
                            modifier = Modifier
                                .size(24.dp)
                                .padding(item.iconPadding)
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(item.textRes),
                            softWrap = true,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    selected = MutableStates.settingsScreenTag == item.screenTag,
                    onClick = {
                        settingsNavController.navigateOnce(item.screenTag)
                    }
                )
            }
        }
    }
}

private data class SettingsItem(
    val screenTag: String,
    val iconRes: Int,
    val textRes: Int,
    val iconPadding: PaddingValues = PaddingValues()
)

@Composable
private fun NavigationUI(
    mainNavController: NavController,
    settingsNavController: NavHostController,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(settingsNavController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            MutableStates.settingsScreenTag = destination.route
        }
        settingsNavController.addOnDestinationChangedListener(listener)
    }

    NavHost(
        modifier = modifier,
        navController = settingsNavController,
        startDestination = RENDERER_SETTINGS_SCREEN_TAG
    ) {
        composable(
            route = RENDERER_SETTINGS_SCREEN_TAG
        ) {
            RendererSettingsScreen()
        }
        composable(
            route = GAME_SETTINGS_TAG
        ) {
            GameSettingsScreen()
        }
        composable(
            route = LAUNCHER_SETTINGS_TAG
        ) {
            LauncherSettingsScreen()
        }
    }
}
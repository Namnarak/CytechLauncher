package com.movtery.zalithlauncher.ui.screens.content

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.movtery.zalithlauncher.ui.components.TabLayout
import com.movtery.zalithlauncher.ui.screens.content.settings.GAME_SETTINGS_TAG
import com.movtery.zalithlauncher.ui.screens.content.settings.GameSettingsScreen
import com.movtery.zalithlauncher.ui.screens.content.settings.LAUNCHER_SETTINGS_TAG
import com.movtery.zalithlauncher.ui.screens.content.settings.LauncherSettingsScreen
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
        TabLayout(
            modifier = Modifier.padding(all = 8.dp)
        ) {
            TabItem(
                painter = painterResource(R.drawable.ic_setting_game),
                contentDescription = null,
                iconPadding = PaddingValues(all = 2.dp),
                text = stringResource(R.string.settings_tab_game),
                selected = MutableStates.settingsScreenTag == GAME_SETTINGS_TAG
            ) {
                settingsNavController.navigateOnce(GAME_SETTINGS_TAG)
            }
            TabItem(
                painter = painterResource(R.drawable.ic_setting_launcher),
                contentDescription = null,
                text = stringResource(R.string.settings_tab_launcher),
                selected = MutableStates.settingsScreenTag == LAUNCHER_SETTINGS_TAG
            ) {
                settingsNavController.navigateOnce(LAUNCHER_SETTINGS_TAG)
            }
        }
    }
}

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
        startDestination = GAME_SETTINGS_TAG
    ) {
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
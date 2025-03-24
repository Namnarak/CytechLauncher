package com.movtery.zalithlauncher.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.getAnimateTween
import com.movtery.zalithlauncher.state.LocalMainScreenTag
import com.movtery.zalithlauncher.state.LocalSettingsScreenTag
import com.movtery.zalithlauncher.state.SettingsScreenTagState
import com.movtery.zalithlauncher.ui.components.TabLayout
import com.movtery.zalithlauncher.ui.screens.settings.LAUNCHER_SETTINGS_TAG
import com.movtery.zalithlauncher.ui.screens.settings.LauncherSettingsScreen

const val SETTINGS_SCREEN_TAG = "SettingsScreen"

@Composable
fun SettingsScreen(
    mainNavController: NavController
) {
    val currentTag = LocalMainScreenTag.current.currentTag
    var isVisible by rememberSaveable { mutableStateOf(false) }

    isVisible = currentTag == SETTINGS_SCREEN_TAG

    val settingsNavController = rememberNavController()

    val settingsScreenTagState = remember { SettingsScreenTagState() }
    CompositionLocalProvider(LocalSettingsScreenTag provides settingsScreenTagState) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            TabMenu(
                isVisible = isVisible,
                settingsNavController = settingsNavController,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 12.dp, top = 12.dp, bottom = 12.dp)
            )

            Box(
                modifier = Modifier.weight(1f)
            ) {
                NavigationUI(
                    isVisible = isVisible,
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
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(getAnimateTween()) + slideInHorizontally(getAnimateTween()) { -60 },
        exit = fadeOut(getAnimateTween()) + slideOutHorizontally(getAnimateTween()) { -60 }
    ) {
        Surface(
            modifier = modifier,
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.inversePrimary,
            shadowElevation = 4.dp
        ) {
            fun navigate(tag: String) {
                settingsNavController.navigate(tag) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }

            val currentSettingsTag = LocalSettingsScreenTag.current.currentTag

            TabLayout(
                modifier = Modifier.padding(all = 8.dp)
            ) {
                TabItem(
                    painter = painterResource(R.drawable.ic_setting_launcher),
                    contentDescription = null,
                    text = stringResource(R.string.settings_tab_launcher),
                    selected = currentSettingsTag == LAUNCHER_SETTINGS_TAG
                ) {
                    navigate(LAUNCHER_SETTINGS_TAG)
                }
                TabItem(
                    painter = painterResource(R.drawable.ic_setting_launcher),
                    contentDescription = null,
                    text = stringResource(R.string.settings_tab_launcher),
                    selected = false
                ) {

                }
            }
        }
    }
}

@Composable
private fun NavigationUI(
    isVisible: Boolean,
    mainNavController: NavController,
    settingsNavController: NavHostController,
    modifier: Modifier = Modifier
) {
    val screenTagState = LocalSettingsScreenTag.current

    LaunchedEffect(settingsNavController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            screenTagState.updateTag(destination.route)
        }
        settingsNavController.addOnDestinationChangedListener(listener)
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(getAnimateTween()) + slideInVertically(getAnimateTween()) { -60 },
        exit = fadeOut(getAnimateTween()) + slideOutVertically(getAnimateTween()) { -60 }
    ) {
        NavHost(
            modifier = modifier,
            navController = settingsNavController,
            startDestination = LAUNCHER_SETTINGS_TAG
        ) {
            composable(
                route = LAUNCHER_SETTINGS_TAG
            ) {
                LauncherSettingsScreen()
            }
        }
    }
}
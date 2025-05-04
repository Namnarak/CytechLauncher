package com.movtery.zalithlauncher.ui.screens.content.download

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.movtery.zalithlauncher.ui.screens.content.download.game.DOWNLOAD_GAME_WITH_ADDON_SCREEN_TAG
import com.movtery.zalithlauncher.ui.screens.content.download.game.DownloadGameScreenStates
import com.movtery.zalithlauncher.ui.screens.content.download.game.DownloadGameWithAddonScreen
import com.movtery.zalithlauncher.ui.screens.content.download.game.SELECT_GAME_VERSION_SCREEN_TAG
import com.movtery.zalithlauncher.ui.screens.content.download.game.SelectGameVersionScreen
import com.movtery.zalithlauncher.ui.screens.navigateTo
import com.movtery.zalithlauncher.utils.animation.TransitionAnimationType
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.animation.getAnimateType

const val DOWNLOAD_GAME_SCREEN_TAG = "DownloadGameScreen"

@Composable
fun DownloadGameScreen() {
    val navController = rememberNavController()

    LaunchedEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            DownloadGameScreenStates.screenTag = destination.route
        }
        navController.addOnDestinationChangedListener(listener)
    }

    NavHost(
        modifier = Modifier.fillMaxSize(),
        navController = navController,
        startDestination = SELECT_GAME_VERSION_SCREEN_TAG,
        enterTransition = {
            if (getAnimateType() != TransitionAnimationType.CLOSE) {
                fadeIn(animationSpec = getAnimateTween())
            } else {
                EnterTransition.None
            }
        },
        exitTransition = {
            if (getAnimateType() != TransitionAnimationType.CLOSE) {
                fadeOut(animationSpec = getAnimateTween())
            } else {
                ExitTransition.None
            }
        }
    ) {
        composable(
            route = SELECT_GAME_VERSION_SCREEN_TAG
        ) {
            SelectGameVersionScreen { versionString ->
                //导航至DownloadGameWithAddonScreen
                navController.navigateTo("$DOWNLOAD_GAME_WITH_ADDON_SCREEN_TAG?gameVersion=$versionString", true)
            }
        }
        composable(
            route = "${DOWNLOAD_GAME_WITH_ADDON_SCREEN_TAG}?gameVersion={gameVersion}"
        ) { backStackEntry ->
            val gameVersion = backStackEntry.arguments?.getString("gameVersion") ?: throw IllegalArgumentException("The game version is not set!")
            DownloadGameWithAddonScreen(gameVersion)
        }
    }
}
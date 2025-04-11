package com.movtery.zalithlauncher.ui.screens.splash

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.movtery.zalithlauncher.components.InstallableItem
import com.movtery.zalithlauncher.info.InfoDistributor
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.state.MutableStates
import com.movtery.zalithlauncher.ui.components.DownShadow
import com.movtery.zalithlauncher.ui.screens.navigateTo
import com.movtery.zalithlauncher.utils.animation.getAnimateTween

@Composable
fun SplashScreen(
    eulaText: String?,
    eulaDate: String,
    checkTasks: () -> Unit,
    startAllTask: () -> Unit,
    unpackItems: List<InstallableItem>,
) {
    Column {
        TopBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(color = MaterialTheme.colorScheme.primaryContainer)
                .zIndex(10f),
            textColor = MaterialTheme.colorScheme.onPrimaryContainer
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val backgroundColor = if (isSystemInDarkTheme()) {
                MaterialTheme.colorScheme.surfaceContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }

            NavigationUI(
                eulaText = eulaText,
                eulaDate = eulaDate,
                checkTasks = checkTasks,
                startAllTask = startAllTask,
                unpackItems = unpackItems,
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = backgroundColor)
            )

            DownShadow(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart),
                height = 4.dp
            )
        }
    }
}

@Composable
private fun TopBar(
    modifier: Modifier = Modifier,
    textColor: Color
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            modifier = Modifier.align(Alignment.CenterVertically),
            text = InfoDistributor.LAUNCHER_NAME,
            color = textColor
        )
    }
}

@Composable
private fun NavigationUI(
    eulaText: String?,
    eulaDate: String,
    checkTasks: () -> Unit,
    startAllTask: () -> Unit,
    unpackItems: List<InstallableItem>,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    LaunchedEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            MutableStates.splashScreenTag = destination.route
        }
        navController.addOnDestinationChangedListener(listener)
    }

    val startDestination = if (eulaText != null) EULA_SCREEN_TAG else UNPACK_SCREEN_TAG

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            fadeIn(animationSpec = getAnimateTween())
        },
        exitTransition = {
            fadeOut(animationSpec = getAnimateTween())
        }
    ) {
        composable(
            route = EULA_SCREEN_TAG
        ) {
            EulaScreen(eulaText!!) {
                navController.navigateTo(UNPACK_SCREEN_TAG)
                AllSettings.splashEulaDate.put(eulaDate).save()
                checkTasks()
            }
        }
        composable(
            route = UNPACK_SCREEN_TAG
        ) {
            UnpackScreen(unpackItems) {
                startAllTask()
            }
        }
    }
}
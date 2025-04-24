package com.movtery.zalithlauncher.ui.screens.content

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.movtery.zalithlauncher.ui.components.secondaryContainerDrawerItemColors
import com.movtery.zalithlauncher.ui.screens.content.versions.VERSION_OVERVIEW_SCREEN_TAG
import com.movtery.zalithlauncher.ui.screens.content.versions.VersionOverViewScreen
import com.movtery.zalithlauncher.ui.screens.navigateOnce
import com.movtery.zalithlauncher.utils.animation.TransitionAnimationType
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.animation.getAnimateType
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState

const val VERSION_SETTINGS_SCREEN_TAG = "VersionSettingsScreen"

@Composable
fun VersionSettingsScreen() {
    BaseScreen(
        screenTag = VERSION_SETTINGS_SCREEN_TAG,
        currentTag = MutableStates.mainScreenTag
    ) { isVisible ->
        val navController = rememberNavController()

        if (!isVisible) {
            MutableStates.versionSettingsScreenTag = null
        }

        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            TabMenu(
                isVisible = isVisible,
                navController = navController,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(2.5f)
                    .padding(start = 12.dp, top = 12.dp, bottom = 12.dp)
            )

            Box(
                modifier = Modifier.weight(7.5f)
            ) {
                NavigationUI(
                    navController = navController
                )
            }
        }
    }
}

@Composable
private fun TabMenu(
    isVisible: Boolean,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val xOffset by swapAnimateDpAsState(
        targetValue = (-40).dp,
        swapIn = isVisible
    )

    Surface(
        modifier = modifier.offset {
            IntOffset(
                x = xOffset.roundToPx(),
                y = 0
            )
        },
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.secondaryContainer,
        shadowElevation = 4.dp
    ) {
        @Composable
        fun VersionSettingsIcon(image: ImageVector, textRes: Int) {
            Icon(
                imageVector = image,
                contentDescription = stringResource(textRes),
                modifier = Modifier.size(24.dp)
            )
        }

        val settingItems = listOf(
            VersionSettingsItem(VERSION_OVERVIEW_SCREEN_TAG, { VersionSettingsIcon(Icons.Outlined.Dashboard, R.string.versions_manage_settings_overview) }, R.string.versions_manage_settings_overview)
        )

        LazyColumn(
            contentPadding = PaddingValues(all = 12.dp)
        ) {
            items(settingItems.size) { index ->
                val item = settingItems[index]
                NavigationDrawerItem(
                    icon = {
                        item.icon()
                    },
                    label = {
                        Text(
                            text = stringResource(item.textRes),
                            softWrap = true,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    selected = MutableStates.versionSettingsScreenTag == item.screenTag,
                    onClick = {
                        navController.navigateOnce(item.screenTag)
                    },
                    colors = secondaryContainerDrawerItemColors()
                )
            }
        }
    }
}

private data class VersionSettingsItem(
    val screenTag: String,
    val icon: @Composable () -> Unit,
    val textRes: Int
)

@Composable
private fun NavigationUI(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            MutableStates.versionSettingsScreenTag = destination.route
        }
        navController.addOnDestinationChangedListener(listener)
    }

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = VERSION_OVERVIEW_SCREEN_TAG,
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
            route = VERSION_OVERVIEW_SCREEN_TAG
        ) {
            VersionOverViewScreen()
        }
    }
}
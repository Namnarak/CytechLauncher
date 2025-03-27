package com.movtery.zalithlauncher.ui.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ZLApplication
import com.movtery.zalithlauncher.state.ColorThemeState
import com.movtery.zalithlauncher.state.LocalColorThemeState
import com.movtery.zalithlauncher.state.LocalMainScreenTag
import com.movtery.zalithlauncher.state.MainScreenTagState
import com.movtery.zalithlauncher.ui.base.BaseComponentActivity
import com.movtery.zalithlauncher.ui.screens.ACCOUNT_MANAGE_SCREEN_TAG
import com.movtery.zalithlauncher.ui.screens.AccountManageScreen
import com.movtery.zalithlauncher.ui.screens.LAUNCHER_SCREEN_TAG
import com.movtery.zalithlauncher.ui.screens.LauncherScreen
import com.movtery.zalithlauncher.ui.screens.SETTINGS_SCREEN_TAG
import com.movtery.zalithlauncher.ui.screens.SettingsScreen
import com.movtery.zalithlauncher.ui.screens.navigateTo
import com.movtery.zalithlauncher.ui.theme.ZalithLauncherTheme
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.string.ShiftDirection
import com.movtery.zalithlauncher.utils.string.StringUtils
import com.movtery.zalithlauncher.viewmodel.TaskViewModel
import com.movtery.zalithlauncher.viewmodel.TaskViewModelFactory

class MainActivity : BaseComponentActivity() {
    private val taskSystem by lazy { (application as ZLApplication).taskSystem }

    val taskViewModel: TaskViewModel by viewModels {
        TaskViewModelFactory(taskSystem)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainUI()
        }
    }

    @Preview
    @Composable
    fun MainUI() {
        val navController = rememberNavController()

        val colorThemeState = remember { ColorThemeState() }
        val mainScreenTagState = remember { MainScreenTagState() }

        CompositionLocalProvider(
            LocalColorThemeState provides colorThemeState,
            LocalMainScreenTag provides mainScreenTagState
        ) {
            ZalithLauncherTheme {
                Column(
                    modifier = Modifier.fillMaxHeight()
                ) {
                    TopBar(
                        navController = navController,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .background(color = MaterialTheme.colorScheme.primaryContainer)
                            .zIndex(10f)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        NavigationUI(
                            navController = navController,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = MaterialTheme.colorScheme.background)
                        )

                        //叠加的一层阴影效果
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .align(Alignment.TopStart)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color.Black.copy(alpha = 0.15f), Color.Transparent)
                                    )
                                )
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun TopBar(
        navController: NavHostController,
        modifier: Modifier = Modifier
    ) {
        var appTitle by rememberSaveable { mutableStateOf("ZalithLauncher") }
        val currentTag = LocalMainScreenTag.current.currentTag

        val inLauncherScreen = currentTag == null || currentTag == LAUNCHER_SCREEN_TAG

        ConstraintLayout (
            modifier = modifier
        ) {
            val (backButton, title, download, settings) = createRefs()

            val backButtonX by animateDpAsState(
                targetValue = if (inLauncherScreen) -(60).dp else 0.dp,
                animationSpec = getAnimateTween()
            )

            IconButton(
                modifier = Modifier
                    .offset { IntOffset(x = backButtonX.roundToPx(), y = 0) }
                    .constrainAs(backButton) {
                        start.linkTo(parent.start, margin = 12.dp)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
                    .fillMaxHeight(),
                onClick = {
                    if (!inLauncherScreen) {
                        //不在主屏幕时才允许返回
                        navController.popBackStack()
                    }
                }
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(R.drawable.ic_arrow),
                    contentDescription = stringResource(R.string.generic_back),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            val appTitleX by animateDpAsState(
                targetValue = if (inLauncherScreen) 0.dp else 48.dp,
                animationSpec = getAnimateTween()
            )

            Text(
                text = appTitle,
                modifier = Modifier
                    .offset { IntOffset(x = appTitleX.roundToPx(), y = 0) }
                    .constrainAs(title) {
                        centerVerticallyTo(parent)
                        start.linkTo(parent.start, margin = 18.dp)
                    }
                    .clickable {
                        appTitle = StringUtils.shiftString(appTitle, ShiftDirection.RIGHT, 1)
                    },
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            IconButton(
                modifier = Modifier
                    .constrainAs(download) {
                        centerVerticallyTo(parent)
                        end.linkTo(settings.start)
                    }
                    .fillMaxHeight()
                    .padding(end = 4.dp),
                onClick = {

                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_download),
                    contentDescription = stringResource(R.string.generic_download),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            IconButton(
                modifier = Modifier
                    .constrainAs(settings) {
                        centerVerticallyTo(parent)
                        end.linkTo(parent.end)
                    }
                    .fillMaxHeight()
                    .padding(end = 12.dp),
                onClick = {
                    if (currentTag == LAUNCHER_SCREEN_TAG) {
                        navController.navigateTo(SETTINGS_SCREEN_TAG)
                    } else {
                        navController.popBackStack(LAUNCHER_SCREEN_TAG, inclusive = false)
                    }
                }
            ) {
                Crossfade(
                    targetState = currentTag,
                    label = "SettingsIconCrossfade",
                    animationSpec = getAnimateTween()
                ) { tag ->
                    val isLauncherScreen = tag == LAUNCHER_SCREEN_TAG
                    Icon(
                        painter = if (isLauncherScreen) {
                            painterResource(R.drawable.ic_setting)
                        } else {
                            painterResource(R.drawable.ic_menu_home)
                        },
                        contentDescription = if (isLauncherScreen) {
                            stringResource(R.string.generic_setting)
                        } else {
                            stringResource(R.string.generic_main_menu)
                        },
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }

    @Composable
    fun NavigationUI(
        navController: NavHostController,
        modifier: Modifier = Modifier
    ) {
        val screenTagState = LocalMainScreenTag.current

        LaunchedEffect(navController) {
            val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
                screenTagState.updateTag(destination.route)
            }
            navController.addOnDestinationChangedListener(listener)
        }

        NavHost(
            modifier = modifier,
            navController = navController,
            startDestination = LAUNCHER_SCREEN_TAG,
            enterTransition = {
                fadeIn(animationSpec = getAnimateTween())
            },
            exitTransition = {
                fadeOut(animationSpec = getAnimateTween())
            }
        ) {
            composable(
                route = LAUNCHER_SCREEN_TAG
            ) {
                LauncherScreen(navController)
            }
            composable(
                route = SETTINGS_SCREEN_TAG
            ) {
                SettingsScreen(navController)
            }
            composable(
                route = ACCOUNT_MANAGE_SCREEN_TAG
            ) {
                AccountManageScreen()
            }
        }
    }
}
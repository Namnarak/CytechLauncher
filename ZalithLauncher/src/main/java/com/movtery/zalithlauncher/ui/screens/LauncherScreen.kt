package com.movtery.zalithlauncher.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.getAnimateTween
import com.movtery.zalithlauncher.setting.getAnimateTweenBounce
import com.movtery.zalithlauncher.state.LocalMainScreenTag
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.IconTextItemLayout
import com.movtery.zalithlauncher.ui.components.ScalingActionButton

const val LAUNCHER_SCREEN_TAG: String = "LauncherScreen"

@Composable
fun LauncherScreen(
    navController: NavController
) {
    BaseScreen(
        screenTag = LAUNCHER_SCREEN_TAG,
        tagProvider = LocalMainScreenTag
    ) { isVisible ->
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .weight(7f)
                    .fillMaxHeight()
            ) {
                MainMenu(
                    isVisible = isVisible,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(all = 6.dp),
                    navController = navController
                )
            }

            Box(
                modifier = Modifier
                    .weight(3f)
                    .fillMaxHeight()
            ) {
                RightMenu(
                    isVisible = isVisible,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(top = 12.dp, end = 12.dp, bottom = 12.dp),
                    navController = navController
                )
            }
        }
    }
}

@Composable
private fun MainMenu(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    navController: NavController
) {
    val yOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else (-40).dp,
        animationSpec = if (isVisible) getAnimateTweenBounce() else getAnimateTween()
    )

    Column(
        modifier = modifier.offset {
            IntOffset(
                x = 0,
                y = yOffset.roundToPx()
            )
        }
    ) {
        MenuActionButton(
            painter = painterResource(R.drawable.ic_about),
            text = stringResource(R.string.main_about),
            modifier = Modifier.fillMaxWidth()
        ) {}
        MenuActionButton(
            painter = painterResource(R.drawable.ic_controls),
            text = stringResource(R.string.main_control),
            modifier = Modifier.fillMaxWidth()
        ) {}
        MenuActionButton(
            painter = painterResource(R.drawable.ic_java),
            text = stringResource(R.string.main_install_jar),
            modifier = Modifier.fillMaxWidth()
        ) {}
        MenuActionButton(
            painter = painterResource(R.drawable.ic_share),
            text = stringResource(R.string.main_send_log),
            modifier = Modifier.fillMaxWidth()
        ) {}
    }
}

@Composable
private fun MenuActionButton(
    painter: Painter,
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    IconTextItemLayout(
        painter = painter,
        contentDescription = text,
        text = text,
        fontSize = 12.sp,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.secondaryContainer,
        iconSize = 24.dp,
        iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
        textColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = modifier.padding(all = 6.dp),
        onClick = onClick
    )
}

@Composable
private fun RightMenu(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    navController: NavController
) {
    val xOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 40.dp,
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
        Row {
            ScalingActionButton(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Bottom)
                    .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
            ) {
                Text(text = stringResource(R.string.main_launch_game))
            }
        }
    }
}
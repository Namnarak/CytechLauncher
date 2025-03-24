package com.movtery.zalithlauncher.ui.screens

import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.getAnimateSpeed
import com.movtery.zalithlauncher.state.LocalMainScreenTag
import com.movtery.zalithlauncher.ui.components.IconTextItemLayout
import com.movtery.zalithlauncher.ui.components.ScalingActionButton

const val LAUNCHER_SCREEN_TAG: String = "LauncherScreen"

@Composable
fun LauncherScreen(
    navController: NavController
) {
    val currentTag = LocalMainScreenTag.current.currentTag
    var isVisible by rememberSaveable { mutableStateOf(false) }

    isVisible = currentTag == LAUNCHER_SCREEN_TAG

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

    LaunchedEffect(Unit) {
        isVisible = true
    }
}

@Composable
private fun MainMenu(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    navController: NavController
) {
    val transition = updateTransition(targetState = isVisible, label = "mainMenuTransition")

    val offsetY by transition.animateDp(
        transitionSpec = { tween(getAnimateSpeed()) },
        label = "offsetY"
    ) { visible ->
        if (visible) 0.dp else (-60).dp
    }

    val alpha by transition.animateFloat(
        transitionSpec = { tween(getAnimateSpeed()) },
        label = "alpha"
    ) { visible ->
        if (visible) 1f else 0f
    }

    Column(
        modifier = modifier
            .offset(y = offsetY)
            .alpha(alpha = alpha)
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
        color = MaterialTheme.colorScheme.background,
        iconSize = 24.dp,
        iconTint = MaterialTheme.colorScheme.onSecondary,
        textColor = MaterialTheme.colorScheme.onSecondary,
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
    val transition = updateTransition(targetState = isVisible, label = "rightMenuTransition")

    val offsetX by transition.animateDp(
        transitionSpec = { tween(getAnimateSpeed()) },
        label = "offsetX"
    ) { visible ->
        if (visible) 0.dp else 60.dp
    }

    val alpha by transition.animateFloat(
        transitionSpec = { tween(getAnimateSpeed()) },
        label = "alpha"
    ) { visible ->
        if (visible) 1f else 0f
    }

    Surface(
        modifier = modifier
            .offset(x = offsetX)
            .alpha(alpha = alpha),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.background
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
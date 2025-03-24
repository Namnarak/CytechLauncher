package com.movtery.zalithlauncher.ui.screens

import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.state.LocalMainScreenTag

const val SETTINGS_SCREEN_TAG = "SettingsScreen"

@Composable
fun SettingsScreen(
    navController: NavController
) {
    val currentTag = LocalMainScreenTag.current.currentTag
    var isVisible by rememberSaveable { mutableStateOf(false) }

    isVisible = currentTag == SETTINGS_SCREEN_TAG

    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        TabMenu(
            isVisible = isVisible,
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 12.dp, top = 12.dp, bottom = 12.dp)
        )

        Box(
            modifier = Modifier.weight(1f)
        ) {

        }
    }
}

@Composable
private fun TabMenu(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = updateTransition(targetState = isVisible, label = "tabMenuTransition")

    val offsetX by transition.animateDp(
        transitionSpec = { tween(1900) },
        label = "offsetX"
    ) { visible ->
        if (visible) 0.dp else (-200).dp
    }

    val alpha by transition.animateFloat(
        transitionSpec = { tween(1900) },
        label = "alpha"
    ) { visible ->
        if (visible) 1f else 0f
    }

    Surface(
        modifier = modifier
            .offset(x = offsetX)
            .alpha(alpha),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.verticalScroll(state = rememberScrollState())
        ) {
            TabItem(
                painter = painterResource(R.drawable.ic_download),
                contentDescription = null,
                text = "下载"
            ) {}
            TabItem(
                painter = painterResource(R.drawable.ic_menu_home),
                contentDescription = null,
                text = "主界面"
            )
            TabItem(
                painter = painterResource(R.drawable.ic_setting),
                contentDescription = null,
                text = "设置"
            ) {}
            TabItem(
                painter = painterResource(R.drawable.ic_setting),
                contentDescription = null,
                text = "TEST VERY VERY VERY LONG STRING"
            )
        }
    }
}

@Composable
private fun TabItem(
    modifier: Modifier = Modifier,
    painter: Painter,
    contentDescription: String?,
    text: String,
    width: Dp = 120.dp,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .clickable {
                onClick?.invoke()
            }
            .padding(start = 12.dp, top = 8.dp, end = 8.dp, bottom = 8.dp)
            .width(width = width)
    ) {
        Icon(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier
                .width(24.dp)
                .height(24.dp)
                .align(Alignment.CenterVertically)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            modifier = Modifier.align(Alignment.CenterVertically),
            softWrap = true,
            fontSize = 12.sp
        )
    }
}
package com.movtery.zalithlauncher.ui.screens.content

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.state.MutableStates
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.ScalingActionButton
import com.movtery.zalithlauncher.ui.screens.content.elements.AccountAvatar
import com.movtery.zalithlauncher.ui.screens.content.elements.VersionIconImage
import com.movtery.zalithlauncher.ui.screens.navigateTo
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.animation.getAnimateTweenBounce

const val LAUNCHER_SCREEN_TAG: String = "LauncherScreen"

@Composable
fun LauncherScreen(
    navController: NavController
) {
    BaseScreen(
        screenTag = LAUNCHER_SCREEN_TAG,
        currentTag = MutableStates.mainScreenTag
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
                        .padding(all = 12.dp),
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
    val xOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 40.dp,
        animationSpec = if (isVisible) getAnimateTweenBounce() else getAnimateTween()
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                translationX = xOffset.roundToPx().toFloat()
            }
    ) {
        Box(modifier = modifier) {
            var isExpanded by rememberSaveable { mutableStateOf(false) }
            Row {
                val rowXOffset by animateDpAsState(
                    targetValue = if (isExpanded) 0.dp else 240.dp,
                    animationSpec = getAnimateTween()
                )
                val surfaceAlpha by animateFloatAsState(
                    targetValue = if (isExpanded) 1f else 0f,
                    animationSpec = getAnimateTween()
                )
                Spacer(modifier = Modifier.weight(0.3f))
                Surface(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = rowXOffset.roundToPx(),
                                y = 0
                            )
                        }
                        .weight(0.7f)
                        .fillMaxHeight()
                        .alpha(surfaceAlpha)
                        .align(Alignment.CenterVertically),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.extraLarge,
                    shadowElevation = 4.dp
                ) {
                    Column(modifier = Modifier.fillMaxHeight()) {
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(state = scrollState)
                                .padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 4.dp)
                                .weight(1f)
                        ) {
                            MenuActionButton(
                                enabled = isExpanded,
                                painter = painterResource(R.drawable.ic_about),
                                text = stringResource(R.string.main_about),
                                modifier = Modifier.fillMaxWidth()
                            ) {}
                            MenuActionButton(
                                enabled = isExpanded,
                                painter = painterResource(R.drawable.ic_controls),
                                text = stringResource(R.string.main_control),
                                modifier = Modifier.fillMaxWidth()
                            ) {}
                            MenuActionButton(
                                enabled = isExpanded,
                                painter = painterResource(R.drawable.ic_java),
                                text = stringResource(R.string.main_install_jar),
                                modifier = Modifier.fillMaxWidth()
                            ) {}
                            MenuActionButton(
                                enabled = isExpanded,
                                painter = painterResource(R.drawable.ic_share),
                                text = stringResource(R.string.main_send_log),
                                modifier = Modifier.fillMaxWidth()
                            ) {}
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.size(34.dp)
                    .align(Alignment.CenterEnd),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 4.dp
            ) {
                val rotation by animateFloatAsState(
                    targetValue = if (isExpanded) -180f else 0f,
                    animationSpec = getAnimateTween()
                )

                IconButton(
                    modifier = Modifier
                        .size(34.dp)
                        .rotate(rotation),
                    onClick = {
                        isExpanded = !isExpanded
                    }
                ) {
                    Icon(
                        modifier = Modifier.padding(all = 8.dp)
                            .offset(x = (-1).dp),
                        painter = painterResource(R.drawable.ic_rounded_triangle),
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuActionButton(
    enabled: Boolean,
    painter: Painter,
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    ScalingActionButton(
        modifier = modifier,
        onClick = {
            if (enabled) onClick()
        },
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                painter = painter,
                contentDescription = text,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                fontSize = 12.sp
            )
        }
    }
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
        ConstraintLayout {
            val (accountAvatar, versionManagerLayout, launchButton) = createRefs()

            val account by AccountsManager.currentAccountFlow.collectAsState()

            AccountAvatar(
                modifier = Modifier
                    .constrainAs(accountAvatar) {
                        top.linkTo(parent.top)
                        bottom.linkTo(launchButton.top, margin = 32.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                account = account
            ) {
                navController.navigateTo(ACCOUNT_MANAGE_SCREEN_TAG)
            }
            VersionManagerLayout(
                modifier = Modifier
                    .height(56.dp)
                    .padding(8.dp)
                    .constrainAs(versionManagerLayout) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(launchButton.top)
                    }
            ) {
                navController.navigateTo(VERSIONS_MANAGE_SCREEN_TAG)
            }
            ScalingActionButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(launchButton) {
                        bottom.linkTo(parent.bottom, margin = 8.dp)
                    }
                    .padding(PaddingValues(horizontal = 12.dp)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                onClick = {},
            ) {
                Text(text = stringResource(R.string.main_launch_game))
            }
        }
    }
}

@Composable
private fun VersionManagerLayout(
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .clip(shape = MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 4.dp)
        ) {
            if (VersionsManager.isRefreshing) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp).align(Alignment.Center))
                }
            } else {
                val version = VersionsManager.currentVersion
                VersionIconImage(
                    version = version,
                    modifier = Modifier.size(28.dp).align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(
                    modifier = Modifier.weight(1f)
                        .align(Alignment.CenterVertically)
                ) {
                    val title = version?.getVersionName() ?: stringResource(R.string.versions_manage_no_versions)
                    Text(
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                        overflow = TextOverflow.Clip,
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = textColor,
                        maxLines = 1
                    )
                    version?.getVersionInfo()?.getInfoString()?.let { infoText ->
                        Text(
                            modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                            overflow = TextOverflow.Clip,
                            text = infoText,
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
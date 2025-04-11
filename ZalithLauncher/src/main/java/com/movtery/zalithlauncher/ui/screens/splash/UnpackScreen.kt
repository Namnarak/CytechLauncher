package com.movtery.zalithlauncher.ui.screens.splash

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.components.InstallableItem
import com.movtery.zalithlauncher.state.MutableStates
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.ScalingActionButton
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.animation.getAnimateTweenBounce

const val UNPACK_SCREEN_TAG = "UnpackScreen"

@Composable
fun UnpackScreen(
    items: List<InstallableItem>,
    onAgreeClick: () -> Unit = {}
) {
    BaseScreen(
        screenTag = UNPACK_SCREEN_TAG,
        currentTag = MutableStates.splashScreenTag
    ) { isVisible ->
        Row(modifier = Modifier.fillMaxSize()) {
            val xOffset by animateDpAsState(
                targetValue = if (isVisible) 0.dp else 40.dp,
                animationSpec = if (isVisible) getAnimateTweenBounce() else getAnimateTween()
            )
            val yOffset by animateDpAsState(
                targetValue = if (isVisible) 0.dp else (-40).dp,
                animationSpec = if (isVisible) getAnimateTweenBounce() else getAnimateTween()
            )

            Surface(
                modifier = Modifier
                    .weight(7f)
                    .fillMaxHeight()
                    .padding(start = 12.dp, top = 12.dp, bottom = 12.dp)
                    .offset {
                        IntOffset(
                            x = 0,
                            y = yOffset.roundToPx()
                        )
                    },
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.extraLarge,
                shadowElevation = 4.dp
            ) {
                UnpackTaskList(
                    items = items,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Surface(
                modifier = Modifier
                    .weight(3f)
                    .fillMaxHeight()
                    .padding(all = 12.dp)
                    .offset {
                        IntOffset(
                            x = xOffset.roundToPx(),
                            y = 0
                        )
                    },
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.inversePrimary,
                shadowElevation = 4.dp
            ) {
                ActionMenu(
                    modifier = Modifier.fillMaxSize(),
                    onAgreeClick = onAgreeClick
                )
            }
        }
    }
}

@Composable
private fun ActionMenu(
    modifier: Modifier = Modifier,
    onAgreeClick: () -> Unit = {}
) {
    var installing by remember { mutableStateOf(false) }

    ConstraintLayout(modifier = modifier) {
        val (text, button) = createRefs()

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PaddingValues(horizontal = 16.dp))
                .constrainAs(text) {
                    top.linkTo(parent.top, margin = 16.dp)
                },
            text = if (installing) {
                stringResource(R.string.splash_screen_installing)
            } else {
                stringResource(R.string.splash_screen_unpack_desc)
            },
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            style = MaterialTheme.typography.bodyMedium
        )

        ScalingActionButton(
            enabled = !installing,
            modifier = Modifier
                .fillMaxWidth()
                .padding(PaddingValues(horizontal = 12.dp))
                .constrainAs(button) {
                    bottom.linkTo(parent.bottom, margin = 8.dp)
                },
            onClick = {
                installing = true
                onAgreeClick()
            }
        ) {
            Text(text = stringResource(R.string.splash_screen_agree))
        }
    }
}

@Composable
private fun UnpackTaskList(
    items: List<InstallableItem>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(all = 12.dp)
    ) {
        items(items.size) { index ->
            val item = items[index]
            TaskItem(
                item = item,
                modifier = Modifier.padding(bottom = if (index == items.size - 1) 0.dp else 12.dp)
            )
        }
    }
}

@Composable
private fun TaskItem(
    item: InstallableItem,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.inversePrimary,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shadowElevation = 4.dp
    ) {
        Row {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, top = 8.dp, bottom = 8.dp)
                    .animateContentSize(animationSpec = getAnimateTween())
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.labelMedium
                )
                item.summary?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                if (item.isRunning) {
                    item.task.taskMessage?.let { taskMessage ->
                        Text(
                            text = taskMessage,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1
                        )
                    }
                }
            }

            val iconModifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(PaddingValues(horizontal = 12.dp, vertical = 8.dp))
                .size(18.dp)
            if (item.isRunning) {
                CircularProgressIndicator(
                    modifier = iconModifier,
                    strokeWidth = 2.dp
                )
            } else if (item.isFinished) {
                Icon(
                    modifier = iconModifier,
                    imageVector = Icons.Default.Done,
                    contentDescription = null
                )
            }
        }
    }
}
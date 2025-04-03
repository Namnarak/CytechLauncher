package com.movtery.zalithlauncher.ui.screens.content

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.path.GamePathManager
import com.movtery.zalithlauncher.state.MutableStates
import com.movtery.zalithlauncher.ui.activities.MainActivity
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.ScalingActionButton
import com.movtery.zalithlauncher.ui.screens.content.elements.GamePathItemLayout
import com.movtery.zalithlauncher.utils.StoragePermissionsUtils
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.animation.getAnimateTweenBounce

const val VERSIONS_MANAGE_SCREEN_TAG = "VersionsManageScreen"

@Composable
fun VersionsManageScreen() {
    BaseScreen(
        screenTag = VERSIONS_MANAGE_SCREEN_TAG,
        currentTag = MutableStates.mainScreenTag
    ) { isVisible ->
        Row {
            GamePathLayout(
                isVisible = isVisible,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(2.5f)
                    .padding(start = 12.dp, top = 12.dp, bottom = 12.dp)
            )

            VersionsLayout(
                isVisible = isVisible,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(7.5f)
                    .padding(all = 12.dp)
            )
        }
    }
}

@Composable
fun GamePathLayout(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val surfaceXOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else (-40).dp,
        animationSpec = if (isVisible) getAnimateTweenBounce() else getAnimateTween()
    )

    Surface(
        modifier = modifier
            .offset {
                IntOffset(
                    x = surfaceXOffset.roundToPx(),
                    y = 0
                )
            },
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.inversePrimary,
        shadowElevation = 4.dp
    ) {
        Column {
            val gamePaths by GamePathManager.gamePathData.collectAsState()

            LazyColumn(
                modifier = Modifier
                    .padding(all = 12.dp)
                    .clip(shape = MaterialTheme.shapes.large)
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(gamePaths.size) { index ->
                    GamePathItemLayout(
                        item = gamePaths[index],
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {},
                        onDelete = {},
                        onRename = {}
                    )
                }
            }

            val context = LocalContext.current
            ScalingActionButton(
                modifier = Modifier
                    .padding(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 8.dp)
                    .fillMaxWidth(),
                onClick = {
                    (context as? MainActivity)?.let { activity ->
                        StoragePermissionsUtils.checkPermissions(activity = activity, hasPermission = {})
                    }
                }
            ) {
                Text(text = stringResource(R.string.versions_manage_game_path_add_new))
            }
        }
    }
}

@Composable
fun VersionsLayout(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val surfaceYOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else (-40).dp,
        animationSpec = if (isVisible) getAnimateTweenBounce() else getAnimateTween()
    )

    Surface(
        modifier = modifier
            .offset {
                IntOffset(
                    x = 0,
                    y = surfaceYOffset.roundToPx()
                )
            },
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.inversePrimary,
        shadowElevation = 4.dp
    ) {

    }
}
package com.movtery.zalithlauncher.ui.screens.content.elements

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.path.GamePathItem
import com.movtery.zalithlauncher.game.path.GamePathManager
import com.movtery.zalithlauncher.state.ObjectStates
import com.movtery.zalithlauncher.ui.components.ScalingActionButton
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.SimpleEditDialog
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.string.StringUtils.Companion.getMessageOrToString

sealed interface GamePathOperation {
    data object None: GamePathOperation
    data object PathExists: GamePathOperation
    data class AddNewPath(val path: String): GamePathOperation
    data class RenamePath(val item: GamePathItem): GamePathOperation
    data class DeletePath(val item: GamePathItem): GamePathOperation
}

@Composable
fun GamePathItemLayout(
    item: GamePathItem,
    selected: Boolean,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    selectedColor: Color = MaterialTheme.colorScheme.primaryContainer,
    unSelectedContentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    color: Color = Color.Transparent,
    onClick: () -> Unit = {},
    onRename: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val notDefault = item.id != "default"
    var isPopupVisible by remember { mutableStateOf(false) }

    val backgroundColor: Color by animateColorAsState(
        targetValue = if (selected) selectedColor else color,
        animationSpec = getAnimateTween()
    )
    val contentColor1: Color by animateColorAsState(
        targetValue = if (selected) contentColor else unSelectedContentColor,
        animationSpec = getAnimateTween()
    )

    Row(
        modifier = modifier
            .clip(shape = MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
            .background(color = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(start = 12.dp, top = 4.dp, bottom = 4.dp)
                .weight(1f)
        ) {
            Text(
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                overflow = TextOverflow.Clip,
                text = if (notDefault) item.title else stringResource(R.string.versions_manage_game_path_default),
                color = contentColor1,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1
            )
            Text(
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                overflow = TextOverflow.Clip,
                text = item.path,
                color = contentColor1,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        }

        Row(
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
            IconButton(
                onClick = {
                    isPopupVisible = !isPopupVisible
                }
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(R.drawable.ic_more),
                    contentDescription = stringResource(R.string.generic_more),
                    tint = contentColor1
                )
            }
        }

        if (isPopupVisible) {
            Popup(
                onDismissRequest = {
                    isPopupVisible = false
                },
                properties = PopupProperties(
                    focusable = true,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                )
            ) {
                Surface(
                    modifier = Modifier.width(240.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 4.dp)
                    ) {
                        ScalingActionButton(
                            enabled = notDefault,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                onRename()
                                isPopupVisible = false
                            }
                        ) {
                            Text(text = stringResource(R.string.generic_rename))
                        }
                        ScalingActionButton(
                            enabled = notDefault,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                onDelete()
                                isPopupVisible = false
                            }
                        ) {
                            Text(text = stringResource(R.string.generic_delete))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GamePathOperation(
    gamePathOperation: GamePathOperation,
    changeState: (GamePathOperation) -> Unit
) {
    runCatching {
        when(gamePathOperation) {
            is GamePathOperation.None -> {}
            is GamePathOperation.AddNewPath -> {
                NameEditPathDialog(
                    onDismissRequest = { changeState(GamePathOperation.None) },
                    onConfirm = { value ->
                        if (GamePathManager.containsPath(gamePathOperation.path)) {
                            changeState(GamePathOperation.PathExists)
                        } else {
                            GamePathManager.addNewPath(title = value, path = gamePathOperation.path)
                            changeState(GamePathOperation.None)
                        }
                    }
                )
            }
            is GamePathOperation.RenamePath -> {
                NameEditPathDialog(
                    initValue = gamePathOperation.item.title,
                    onDismissRequest = { changeState(GamePathOperation.None) },
                    onConfirm = { value ->
                        GamePathManager.modifyTitle(gamePathOperation.item.id, value)
                        changeState(GamePathOperation.None)
                    }
                )
            }
            is GamePathOperation.DeletePath -> {
                SimpleAlertDialog(
                    title = stringResource(R.string.versions_manage_game_path_delete_title),
                    text = stringResource(R.string.versions_manage_game_path_delete_message),
                    onDismiss = { changeState(GamePathOperation.None) },
                    onConfirm = {
                        GamePathManager.removePath(gamePathOperation.item.id)
                        changeState(GamePathOperation.None)
                    }
                )
            }
            is GamePathOperation.PathExists -> {
                SimpleAlertDialog(
                    title = stringResource(R.string.versions_manage_game_path_exists_title),
                    text = stringResource(R.string.versions_manage_game_path_exists_message),
                    onDismiss = { changeState(GamePathOperation.None) }
                )
            }
        }
    }.onFailure { e ->
        ObjectStates.updateThrowable(
            ObjectStates.ThrowableMessage(
                title = stringResource(R.string.versions_manage_game_path_error_title),
                message = e.getMessageOrToString()
            )
        )
    }
}

@Composable
private fun NameEditPathDialog(
    initValue: String = "",
    onDismissRequest: () -> Unit = {},
    onConfirm: (value: String) -> Unit = {}
) {
    var value by remember { mutableStateOf(initValue) }
    SimpleEditDialog(
        title = stringResource(R.string.versions_manage_game_path_add_new),
        value = value,
        onValueChange = { value = it.trim() },
        label = { Text(text = stringResource(R.string.versions_manage_game_path_edit_title)) },
        isError = value.isEmpty(),
        supportingText = {
            if (value.isEmpty()) Text(text = stringResource(R.string.generic_cannot_empty))
        },
        onDismissRequest = onDismissRequest,
        onConfirm = {
            if (value.isNotEmpty()) {
                onConfirm(value)
            }
        }
    )
}
package com.movtery.zalithlauncher.ui.screens.content.elements

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.path.GamePathItem
import com.movtery.zalithlauncher.ui.components.ScalingActionButton

@Composable
fun GamePathItemLayout(
    item: GamePathItem,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    onClick: () -> Unit = {},
    onRename: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val notDefault = item.id != "default"
    var isPopupVisible by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
                .weight(1f)
        ) {
            Text(
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                overflow = TextOverflow.Clip,
                text = if (notDefault) item.title else stringResource(R.string.versions_manage_game_path_default),
                color = contentColor,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1
            )
            Text(
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                overflow = TextOverflow.Clip,
                text = item.path,
                color = contentColor,
                style = MaterialTheme.typography.labelLarge,
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
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(R.drawable.ic_more),
                    contentDescription = stringResource(R.string.generic_more),
                    tint = contentColor
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
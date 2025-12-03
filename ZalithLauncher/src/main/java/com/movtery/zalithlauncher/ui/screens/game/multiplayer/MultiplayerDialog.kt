/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.ui.screens.game.multiplayer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.terracotta.Terracotta
import com.movtery.zalithlauncher.ui.components.BackgroundCard

@Composable
fun MultiplayerDialog(
    onClose: () -> Unit
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth(0.7f)) {
            Surface(
                modifier = Modifier.padding(all = 16.dp),
                shape = MaterialTheme.shapes.extraLarge,
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(all = 16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.terracotta_menu),
                        style = MaterialTheme.typography.titleLarge
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        //房主
                        SimpleCard(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(3f / 2f),
                            icon = Icons.Filled.Home,
                            title = stringResource(R.string.terracotta_status_waiting_host_title),
                            description = stringResource(R.string.terracotta_status_waiting_host_desc)
                        ) {

                        }

                        //房客
                        SimpleCard(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(3f / 2f),
                            icon = Icons.Filled.Group,
                            title = stringResource(R.string.terracotta_status_waiting_guest_title),
                            description = stringResource(R.string.terracotta_status_waiting_guest_desc)
                        ) {

                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        //版本号
                        Column(modifier = Modifier.weight(1f)) {
                            val meta = Terracotta.getMetadata()
                            Text(text = stringResource(R.string.terracotta_metadata_ver, meta.terracottaVersion))
                            Text(text = stringResource(R.string.terracotta_metadata_easytier_ver, meta.easyTierVersion))
                        }

                        //关闭
                        TextButton(
                            onClick = onClose
                        ) {
                            Text(text = stringResource(R.string.generic_close))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SimpleCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    BackgroundCard(
        modifier = modifier,
        influencedByBackground = false,
        onClick = onClick
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            val (topSpacer, icons, desc, bottomSpacer) = createRefs()

            Spacer(
                Modifier.constrainAs(topSpacer) {
                    top.linkTo(parent.top)
                    bottom.linkTo(icons.top)
                    height = Dimension.fillToConstraints
                }
            )

            //图文区
            Column(
                modifier = Modifier.constrainAs(icons) {
                    top.linkTo(topSpacer.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            //这个卡片的描述
            Text(
                modifier = Modifier.constrainAs(desc) {
                    top.linkTo(icons.bottom, margin = 12.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(
                Modifier.constrainAs(bottomSpacer) {
                    top.linkTo(desc.bottom)
                    bottom.linkTo(parent.bottom)
                    height = Dimension.fillToConstraints
                }
            )
        }
    }
}
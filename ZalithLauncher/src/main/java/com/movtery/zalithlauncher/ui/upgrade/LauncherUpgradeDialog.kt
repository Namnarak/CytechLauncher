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

package com.movtery.zalithlauncher.ui.upgrade

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.upgrade.RemoteData
import com.movtery.zalithlauncher.utils.formatDate
import java.util.Locale

@Composable
fun UpgradeDialog(
    data: RemoteData,
    onDismissRequest: () -> Unit,
    onFilesClick: () -> Unit,
    onIgnored: () -> Unit,
    onLinkClick: (String) -> Unit
) {
    val language = remember(Unit) {
        Locale.getDefault().language
    }
    val body = remember(language, data) {
        data.bodies.find { it.language == language } ?: data.defaultBody
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = stringResource(R.string.upgrade_new))
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                //版本号
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.upgrade_version_change, data.version)
                )

                //更新时间
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(
                        R.string.upgrade_version_create_at,
                        formatDate(
                            input = data.createdAt,
                            pattern = stringResource(R.string.date_format)
                        )
                    )
                )

                //更新日志
                Spacer(Modifier.height(8.dp))
                BodyUI(
                    modifier = Modifier.fillMaxWidth(),
                    body = body,
                    onLinkClick = onLinkClick
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onFilesClick
            ) {
                Text(text = stringResource(R.string.upgrade_more))
            }
        },
        dismissButton = {
            FilledTonalButton(
                onClick = onIgnored
            ) {
                Text(text = stringResource(R.string.generic_ignore))
            }
        }
    )
}

@Composable
private fun BodyUI(
    body: RemoteData.RemoteBody,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        body.chunks.forEach { chunk ->
            BodyChunk(
                modifier = Modifier.fillMaxWidth(),
                chunk = chunk,
                onLinkClick = onLinkClick
            )
        }
    }
}

@Composable
private fun BodyChunk(
    chunk: RemoteData.RemoteBody.TextChunk,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = chunk.title,
            style = MaterialTheme.typography.titleMedium
        )
        chunk.texts.forEach { text ->
            BodyText(
                modifier = Modifier.fillMaxWidth(),
                text = text,
                onLinkClick = onLinkClick
            )
        }
    }
}

@Composable
private fun BodyText(
    text: RemoteData.RemoteBody.TextChunk.Text,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val indentation = remember {
            text.indentation.coerceAtLeast(0) * 8
        }

        //文本缩进
        Row(
            modifier = Modifier.padding(start = indentation.dp)
        ) {
            Text(text = "•")
        }

        //真实的文本部分
        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = text.text,
                style = MaterialTheme.typography.bodyMedium
            )
            text.links.forEach { link ->
                Text(
                    modifier = Modifier.clickable {
                        onLinkClick(link.link)
                    },
                    text = link.text,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textDecoration = TextDecoration.Underline
                    )
                )
            }
        }
    }
}
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

package com.movtery.zalithlauncher.ui.screens.content

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.path.URL_EASYTIER
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.AnimatedRow
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsBackground
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel

@Composable
fun MultiplayerScreen(
    backScreenViewModel: ScreenBackStackViewModel,
    eventViewModel: EventViewModel
) {
    BaseScreen(
        screenKey = NormalNavKey.Multiplayer,
        currentKey = backScreenViewModel.mainScreen.currentKey
    ) { isVisible ->
        AnimatedRow(
            modifier = Modifier.fillMaxSize(),
            isVisible = isVisible,
            delayIncrement = 0 //同时进行
        ) { scope ->
            AnimatedItem(scope) { xOffset ->
                MainMenu(
                    modifier = Modifier
                        .weight(0.5f)
                        .offset { IntOffset(x = -xOffset.roundToPx(), y = 0) }
                        .padding(start = 12.dp),
                    eventViewModel = eventViewModel
                )
            }

            AnimatedItem(scope) { xOffset ->
                TutorialMenu(
                    modifier = Modifier
                        .weight(0.5f)
                        .offset { IntOffset(x = xOffset.roundToPx(), y = 0) }
                        .padding(end = 12.dp)
                )
            }
        }
    }
}

/**
 * 主菜单：所有主要操作都在这里
 */
@Composable
private fun MainMenu(
    modifier: Modifier = Modifier,
    eventViewModel: EventViewModel
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        //关于地区的警告
        BackgroundCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors().copy(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Text(
                modifier = Modifier.padding(all = 16.dp),
                text = stringResource(R.string.terracotta_warning_region)
            )
        }

        //多人联机设置菜单
        SettingsBackground(modifier = Modifier.fillMaxWidth()) {
            //启用多人联机
            SwitchSettingsLayout(
                modifier = Modifier.fillMaxWidth(),
                unit = AllSettings.enableTerracotta,
                title = stringResource(R.string.terracotta_enable),
                verticalAlignment = Alignment.CenterVertically
            )

            val terracottaEnabled = AllSettings.enableTerracotta.state

            //分享联机核心日志
            Column(
                modifier = Modifier.fillMaxWidth()
                    .clip(shape = RoundedCornerShape(22.0.dp))
                    .clickable(onClick = {}, enabled = terracottaEnabled)//TODO 分享联机核心日志
                    .padding(horizontal = 8.dp, vertical = 16.dp)
                    .padding(bottom = 4.dp)
                    .alpha(if (terracottaEnabled) 1f else 0.5f)
            ) {
                Text(
                    text = stringResource(R.string.terracotta_export_log_share),
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }

        //用户须知
        SingleTitleCard(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.terracotta_confirm_title),
            text = {
                Text(stringResource(R.string.terracotta_confirm_software))
                Text(stringResource(R.string.terracotta_confirm_p2p))
                Text(stringResource(R.string.terracotta_confirm_law))
            }
        )

        //关于 EasyTier
        BackgroundCard(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            onClick = {
                eventViewModel.sendEvent(EventViewModel.Event.OpenLink(URL_EASYTIER))
            }
        ) {
            Row(
                modifier = Modifier.padding(all = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = stringResource(R.string.terracotta_easytier)
                )

                Text(stringResource(R.string.terracotta_easytier))
            }
        }
    }
}

/**
 * 教程Tab分区
 * @param text 板块标题字符串资源
 */
private data class TabItem(
    val text: Int
)

/**
 * 教程菜单
 */
@Composable
private fun TutorialMenu(
    modifier: Modifier = Modifier
) {
    BackgroundCard(
        modifier = modifier
            .fillMaxHeight()
            .padding(vertical = 12.dp),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        val tabs = remember {
            listOf(
                TabItem(R.string.terracotta_tutorial_host_tab),
                TabItem(R.string.terracotta_tutorial_guest_tab)
            )
        }

        val pagerState = rememberPagerState(pageCount = { tabs.size })
        var selectedTabIndex by remember { mutableIntStateOf(0) }

        LaunchedEffect(selectedTabIndex) {
            pagerState.animateScrollToPage(selectedTabIndex)
        }

        //顶贴标签栏
        SecondaryTabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, item ->
                Tab(
                    selected = index == selectedTabIndex,
                    onClick = {
                        selectedTabIndex = index
                    },
                    text = {
                        MarqueeText(text = stringResource(item.text))
                    }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            when(page) {
                0 -> {
                    //房主教程
                    DoubleTitleColumn(
                        firstTitle = stringResource(R.string.terracotta_tutorial_host_tip),
                        firstText = {
                            Text(stringResource(R.string.terracotta_tutorial_step_enable_multiplayer))
                            Text(stringResource(R.string.terracotta_tutorial_step_open_multiplayer_menu))
                            Text(stringResource(R.string.terracotta_tutorial_host_step_become_host))
                            Text(stringResource(R.string.terracotta_tutorial_host_step_open_lan))
                            Text(stringResource(R.string.terracotta_tutorial_step_vpn_permission))
                            Text(stringResource(R.string.terracotta_tutorial_host_step_copy_invite))
                            Text(stringResource(R.string.terracotta_tutorial_host_step_send_invite))
                        },
                        secondTitle = stringResource(R.string.terracotta_tutorial_note_title),
                        secondText = {
                            Text(stringResource(R.string.terracotta_tutorial_step_offline_account_support))
                            Text(stringResource(R.string.terracotta_tutorial_step_interoperability))
                        }
                    )
                }
                1 -> {
                    //房客教程
                    DoubleTitleColumn(
                        firstTitle = stringResource(R.string.terracotta_tutorial_guest_tip),
                        firstText = {
                            Text(stringResource(R.string.terracotta_tutorial_step_enable_multiplayer))
                            Text(stringResource(R.string.terracotta_tutorial_step_open_multiplayer_menu))
                            Text(stringResource(R.string.terracotta_tutorial_guest_step_become_guest))
                            Text(stringResource(R.string.terracotta_tutorial_step_vpn_permission))
                            Text(stringResource(R.string.terracotta_tutorial_guest_step_join_room))
                        },
                        secondTitle = stringResource(R.string.terracotta_tutorial_note_title),
                        secondText = {
                            Text(stringResource(R.string.terracotta_tutorial_step_offline_account_support))
                            Text(stringResource(R.string.terracotta_tutorial_step_interoperability))
                            Text(stringResource(R.string.terracotta_tutorial_guest_step_alternate_server))
                        }
                    )
                }
            }
        }
    }
}

/**
 * 单标题文本卡片，标题+正文的布局
 */
@Composable
private fun SingleTitleCard(
    modifier: Modifier = Modifier,
    title: String,
    shape: Shape = MaterialTheme.shapes.extraLarge,
    text: @Composable ColumnScope.() -> Unit
) {
    BackgroundCard(modifier = modifier, shape = shape) {
        TitleTextLayout(
            modifier = Modifier.padding(all = 16.dp),
            title = title,
            text = text
        )
    }
}

/**
 * 双标题文本Column，第一个标题+文本+第二个标题+文本
 */
@Composable
private fun DoubleTitleColumn(
    modifier: Modifier = Modifier,
    firstTitle: String,
    secondTitle: String,
    firstText: @Composable ColumnScope.() -> Unit,
    secondText: @Composable ColumnScope.() -> Unit,
    scrollState: ScrollState = rememberScrollState()
) {
    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TitleTextLayout(firstTitle, firstText)
        TitleTextLayout(secondTitle, secondText)
    }
}

@Composable
private fun TitleTextLayout(
    title: String,
    text: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            content = text
        )
    }
}
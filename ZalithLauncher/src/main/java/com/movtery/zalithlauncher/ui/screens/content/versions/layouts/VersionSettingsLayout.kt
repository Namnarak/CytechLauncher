package com.movtery.zalithlauncher.ui.screens.content.versions.layouts

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.game.version.installed.SettingState
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.ui.components.TitleAndSummary
import com.movtery.zalithlauncher.ui.screens.content.elements.VersionIconImage

@DslMarker
annotation class VersionSettingsLayoutDsl

@VersionSettingsLayoutDsl
class VersionSettingsLayoutScope {

    @Composable
    fun VersionOverviewItem(
        modifier: Modifier = Modifier,
        version: Version,
        versionName: String = version.getVersionName(),
        versionSummary: String,
        refreshKey: Any? = null
    ) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            VersionIconImage(
                version = version,
                modifier = Modifier.size(34.dp),
                refreshKey = refreshKey
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    overflow = TextOverflow.Clip,
                    maxLines = 1,
                    text = versionName,
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    overflow = TextOverflow.Clip,
                    maxLines = 1,
                    text = versionSummary,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }

    @Composable
    fun StatefulSwitchLayoutFollowGlobal(
        modifier: Modifier = Modifier,
        currentValue: SettingState,
        onValueChange: (SettingState) -> Unit,
        title: String,
        summary: String? = null
    ) {
        var value by remember { mutableStateOf(currentValue) }

        SwitchLayoutFollowGlobal(
            modifier = modifier,
            value = value,
            onValueChange = {
                value = it
                onValueChange(it)
            },
            title = title,
            summary = summary
        )
    }

    @Composable
    fun SwitchLayoutFollowGlobal(
        modifier: Modifier = Modifier,
        value: SettingState,
        onValueChange: (SettingState) -> Unit = {},
        title: String,
        summary: String? = null
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(shape = MaterialTheme.shapes.extraLarge)
                .padding(all = 8.dp)
                .padding(bottom = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            ) {
                TitleAndSummary(title, summary)
            }

            val allItems = SettingState.entries
            var selectedTab by remember { mutableIntStateOf(allItems.indexOf(value)) }

            Surface(
                modifier = Modifier
                    .width(240.dp)
                    .align(Alignment.CenterVertically),
                shape = MaterialTheme.shapes.extraLarge,
                shadowElevation = 2.dp
            ) {
                TabRow(
                    modifier = Modifier.fillMaxWidth(),
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    indicator = @Composable { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.PrimaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab])
                            )
                        }
                    },
                    divider = { /* 禁用底部横线 */ },
                    tabs = {
                        repeat(allItems.size) { index ->
                            val state = allItems[index]
                            Tab(
                                selected = value == state,
                                onClick = {
                                    selectedTab = index
                                    onValueChange(state)
                                }
                            ) {
                                Text(
                                    modifier = Modifier.padding(all = 12.dp),
                                    text = stringResource(state.textRes),
                                    style = MaterialTheme.typography.labelMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}
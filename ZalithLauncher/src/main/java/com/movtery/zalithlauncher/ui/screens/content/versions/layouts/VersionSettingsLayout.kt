package com.movtery.zalithlauncher.ui.screens.content.versions.layouts

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.game.version.installed.Version
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
}
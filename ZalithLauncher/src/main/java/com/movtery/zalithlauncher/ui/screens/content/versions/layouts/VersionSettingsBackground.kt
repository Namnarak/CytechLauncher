package com.movtery.zalithlauncher.ui.screens.content.versions.layouts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.ui.components.BackgroundCard

@Composable
fun VersionSettingsBackground(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
    content: @Composable VersionSettingsLayoutScope.() -> Unit
) {
    BackgroundCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .clip(shape = MaterialTheme.shapes.large)
                .padding(paddingValues)
        ) {
            val scope = remember { VersionSettingsLayoutScope() }

            with(scope) {
                content()
            }
        }
    }
}
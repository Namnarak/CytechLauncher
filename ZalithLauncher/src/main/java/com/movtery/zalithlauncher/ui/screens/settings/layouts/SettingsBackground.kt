package com.movtery.zalithlauncher.ui.screens.settings.layouts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun SettingsBackground(
    modifier: Modifier = Modifier,
    content: @Composable SettingsLayoutScope.() -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.large,
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.clip(shape = MaterialTheme.shapes.large)
        ) {
            val scope = remember { SettingsLayoutScope() }

            with(scope) {
                content()
            }
        }
    }
}
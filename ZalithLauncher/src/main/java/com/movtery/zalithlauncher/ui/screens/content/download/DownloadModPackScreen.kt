package com.movtery.zalithlauncher.ui.screens.content.download

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.content.download.assets.mod.SearchModPackScreen
import com.movtery.zalithlauncher.ui.screens.content.download.assets.mod.SearchModPackScreenKey
import com.movtery.zalithlauncher.ui.screens.content.download.common.downloadModPackBackStack
import com.movtery.zalithlauncher.ui.screens.content.download.common.downloadModPackScreenKey
import kotlinx.serialization.Serializable

@Serializable
data object DownloadModPackScreenKey: NestedNavKey {
    override fun isLastScreen(): Boolean = downloadModPackBackStack.size <= 1
}

@Composable
fun DownloadModPackScreen() {
    val currentKey = downloadModPackBackStack.lastOrNull()

    LaunchedEffect(currentKey) {
        downloadModPackScreenKey = currentKey
    }

    NavDisplay(
        backStack = downloadModPackBackStack,
        modifier = Modifier.fillMaxSize(),
        onBack = {
            val key = downloadModPackBackStack.lastOrNull()
            if (key is NestedNavKey && !key.isLastScreen()) return@NavDisplay
            downloadModPackBackStack.removeLastOrNull()
        },
        entryProvider = entryProvider {
            entry<SearchModPackScreenKey> {
                SearchModPackScreen()
            }
        }
    )
}
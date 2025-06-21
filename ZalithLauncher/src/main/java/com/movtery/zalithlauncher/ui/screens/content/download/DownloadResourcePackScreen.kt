package com.movtery.zalithlauncher.ui.screens.content.download

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.content.download.assets.mod.SearchResourcePackScreen
import com.movtery.zalithlauncher.ui.screens.content.download.assets.mod.SearchResourcePackScreenKey
import com.movtery.zalithlauncher.ui.screens.content.download.common.downloadResourcePackBackStack
import com.movtery.zalithlauncher.ui.screens.content.download.common.downloadResourcePackScreenKey
import kotlinx.serialization.Serializable

@Serializable
data object DownloadResourcePackScreenKey: NestedNavKey {
    override fun isLastScreen(): Boolean = downloadResourcePackBackStack.size <= 1
}

@Composable
fun DownloadResourcePackScreen() {
    val currentKey = downloadResourcePackBackStack.lastOrNull()

    LaunchedEffect(currentKey) {
        downloadResourcePackScreenKey = currentKey
    }

    NavDisplay(
        backStack = downloadResourcePackBackStack,
        modifier = Modifier.fillMaxSize(),
        onBack = {
            val key = downloadResourcePackBackStack.lastOrNull()
            if (key is NestedNavKey && !key.isLastScreen()) return@NavDisplay
            downloadResourcePackBackStack.removeLastOrNull()
        },
        entryProvider = entryProvider {
            entry<SearchResourcePackScreenKey> {
                SearchResourcePackScreen()
            }
        }
    )
}
package com.movtery.zalithlauncher.ui.screens.content.download

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.content.download.assets.mod.SearchModScreen
import com.movtery.zalithlauncher.ui.screens.content.download.assets.mod.SearchModScreenKey
import com.movtery.zalithlauncher.ui.screens.content.download.common.downloadModBackStack
import com.movtery.zalithlauncher.ui.screens.content.download.common.downloadModScreenKey
import kotlinx.serialization.Serializable

@Serializable
data object DownloadModScreenKey: NestedNavKey {
    override fun isLastScreen(): Boolean = downloadModBackStack.size <= 1
}

@Composable
fun DownloadModScreen() {
    val currentKey = downloadModBackStack.lastOrNull()

    LaunchedEffect(currentKey) {
        downloadModScreenKey = currentKey
    }

    NavDisplay(
        backStack = downloadModBackStack,
        modifier = Modifier.fillMaxSize(),
        onBack = {
            val key = downloadModBackStack.lastOrNull()
            if (key is NestedNavKey && !key.isLastScreen()) return@NavDisplay
            downloadModBackStack.removeLastOrNull()
        },
        entryProvider = entryProvider {
            entry<SearchModScreenKey> {
                SearchModScreen()
            }
        }
    )
}
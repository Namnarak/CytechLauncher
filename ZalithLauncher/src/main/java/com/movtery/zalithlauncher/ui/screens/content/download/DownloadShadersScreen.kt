package com.movtery.zalithlauncher.ui.screens.content.download

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.content.download.assets.mod.SearchShadersScreen
import com.movtery.zalithlauncher.ui.screens.content.download.assets.mod.SearchShadersScreenKey
import com.movtery.zalithlauncher.ui.screens.content.download.common.downloadShadersBackStack
import com.movtery.zalithlauncher.ui.screens.content.download.common.downloadShadersScreenKey
import kotlinx.serialization.Serializable

@Serializable
data object DownloadShadersScreenKey: NestedNavKey {
    override fun isLastScreen(): Boolean = downloadShadersBackStack.size <= 1
}

@Composable
fun DownloadShadersScreen() {
    val currentKey = downloadShadersBackStack.lastOrNull()

    LaunchedEffect(currentKey) {
        downloadShadersScreenKey = currentKey
    }

    NavDisplay(
        backStack = downloadShadersBackStack,
        modifier = Modifier.fillMaxSize(),
        onBack = {
            val key = downloadShadersBackStack.lastOrNull()
            if (key is NestedNavKey && !key.isLastScreen()) return@NavDisplay
            downloadShadersBackStack.removeLastOrNull()
        },
        entryProvider = entryProvider {
            entry<SearchShadersScreenKey> {
                SearchShadersScreen()
            }
        }
    )
}
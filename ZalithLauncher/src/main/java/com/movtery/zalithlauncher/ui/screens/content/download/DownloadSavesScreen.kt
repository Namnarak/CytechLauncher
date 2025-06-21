package com.movtery.zalithlauncher.ui.screens.content.download

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.content.download.assets.mod.SearchSavesScreen
import com.movtery.zalithlauncher.ui.screens.content.download.assets.mod.SearchSavesScreenKey
import com.movtery.zalithlauncher.ui.screens.content.download.common.downloadSavesBackStack
import com.movtery.zalithlauncher.ui.screens.content.download.common.downloadSavesScreenKey
import kotlinx.serialization.Serializable

@Serializable
data object DownloadSavesScreenKey: NestedNavKey {
    override fun isLastScreen(): Boolean = downloadSavesBackStack.size <= 1
}

@Composable
fun DownloadSavesScreen() {
    val currentKey = downloadSavesBackStack.lastOrNull()

    LaunchedEffect(currentKey) {
        downloadSavesScreenKey = currentKey
    }

    NavDisplay(
        backStack = downloadSavesBackStack,
        modifier = Modifier.fillMaxSize(),
        onBack = {
            val key = downloadSavesBackStack.lastOrNull()
            if (key is NestedNavKey && !key.isLastScreen()) return@NavDisplay
            downloadSavesBackStack.removeLastOrNull()
        },
        entryProvider = entryProvider {
            entry<SearchSavesScreenKey> {
                SearchSavesScreen()
            }
        }
    )
}
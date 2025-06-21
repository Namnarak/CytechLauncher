package com.movtery.zalithlauncher.ui.screens.content.download.assets.mod

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavKey
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeSavesCategory
import com.movtery.zalithlauncher.ui.screens.content.download.DownloadSavesScreenKey
import com.movtery.zalithlauncher.ui.screens.content.download.common.downloadSavesScreenKey
import com.movtery.zalithlauncher.ui.screens.content.downloadScreenKey
import kotlinx.serialization.Serializable

@Serializable
data object SearchSavesScreenKey : NavKey

@Composable
fun SearchSavesScreen() {
    var searchPlatform by remember { mutableStateOf<Platform>(Platform.CURSEFORGE) }

    SearchAssetsScreen(
        parentScreenKey = DownloadSavesScreenKey,
        parentCurrentKey = downloadScreenKey,
        screenKey = SearchSavesScreenKey,
        currentKey = downloadSavesScreenKey,
        platformClasses = PlatformClasses.SAVES,
        searchPlatform = searchPlatform,
        enablePlatform = false,
        onPlatformChange = {},
        categories = CurseForgeSavesCategory.entries,
        mapCategories = { platform, string ->
            CurseForgeSavesCategory.entries.find { it.describe() == string }
        }
    )
}
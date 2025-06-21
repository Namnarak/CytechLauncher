package com.movtery.zalithlauncher.ui.screens.content.download.assets.mod

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavKey
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeShadersCategory
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthFeatures
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthShadersCategory
import com.movtery.zalithlauncher.ui.screens.content.download.DownloadShadersScreenKey
import com.movtery.zalithlauncher.ui.screens.content.download.common.downloadShadersScreenKey
import com.movtery.zalithlauncher.ui.screens.content.downloadScreenKey
import kotlinx.serialization.Serializable

@Serializable
data object SearchShadersScreenKey : NavKey

@Composable
fun SearchShadersScreen() {
    var searchPlatform by remember { mutableStateOf<Platform>(Platform.MODRINTH) }

    val categories = remember(searchPlatform) {
        when (searchPlatform) {
            Platform.CURSEFORGE -> CurseForgeShadersCategory.entries
            Platform.MODRINTH -> ModrinthShadersCategory.entries
        }
    }

    SearchAssetsScreen(
        parentScreenKey = DownloadShadersScreenKey,
        parentCurrentKey = downloadScreenKey,
        screenKey = SearchShadersScreenKey,
        currentKey = downloadShadersScreenKey,
        platformClasses = PlatformClasses.SHADERS,
        searchPlatform = searchPlatform,
        onPlatformChange = {
            searchPlatform = it
        },
        categories = categories,
        mapCategories = { platform, string ->
            when (platform) {
                Platform.MODRINTH -> {
                    ModrinthShadersCategory.entries.find { it.facetValue() == string }
                        ?: ModrinthFeatures.entries.find { it.facetValue() == string }
                }
                Platform.CURSEFORGE -> {
                    CurseForgeShadersCategory.entries.find { it.describe() == string }
                }
            }
        }
    )
}
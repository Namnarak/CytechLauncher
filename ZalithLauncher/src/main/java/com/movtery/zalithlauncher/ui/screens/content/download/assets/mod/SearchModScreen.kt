package com.movtery.zalithlauncher.ui.screens.content.download.assets.mod

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavKey
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeModCategory
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.curseForgeModLoaderFilters
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthFeatures
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthModCategory
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.modrinthModModLoaderFilters
import com.movtery.zalithlauncher.ui.screens.content.download.DownloadModScreenKey
import com.movtery.zalithlauncher.ui.screens.content.download.common.downloadModScreenKey
import com.movtery.zalithlauncher.ui.screens.content.downloadScreenKey
import kotlinx.serialization.Serializable

@Serializable
data object SearchModScreenKey : NavKey

@Composable
fun SearchModScreen() {
    var searchPlatform by remember { mutableStateOf<Platform>(Platform.CURSEFORGE) }

    val categories = remember(searchPlatform) {
        when (searchPlatform) {
            Platform.CURSEFORGE -> CurseForgeModCategory.entries
            Platform.MODRINTH -> ModrinthModCategory.entries
        }
    }

    val modLoaderFilters = remember(searchPlatform) {
        when (searchPlatform) {
            Platform.CURSEFORGE -> curseForgeModLoaderFilters
            Platform.MODRINTH -> modrinthModModLoaderFilters
        }
    }

    SearchAssetsScreen(
        parentScreenKey = DownloadModScreenKey,
        parentCurrentKey = downloadScreenKey,
        screenKey = SearchModScreenKey,
        currentKey = downloadModScreenKey,
        platformClasses = PlatformClasses.MOD,
        searchPlatform = searchPlatform,
        onPlatformChange = {
            searchPlatform = it
        },
        categories = categories,
        enableModLoader = true,
        modloaders = modLoaderFilters,
        mapCategories = { platform, string ->
            when (platform) {
                Platform.MODRINTH -> {
                    ModrinthModCategory.entries.find { it.facetValue() == string }
                        ?: ModrinthFeatures.entries.find { it.facetValue() == string }
                }
                Platform.CURSEFORGE -> {
                    CurseForgeModCategory.entries.find { it.describe() == string }
                }
            }
        }
    )
}
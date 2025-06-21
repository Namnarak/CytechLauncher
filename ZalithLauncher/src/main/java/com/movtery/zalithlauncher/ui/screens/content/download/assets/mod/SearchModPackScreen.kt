package com.movtery.zalithlauncher.ui.screens.content.download.assets.mod

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavKey
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeModpackCategory
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.curseForgeModLoaderFilters
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthFeatures
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthModpackCategory
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.modrinthModpackModLoaderFilters
import com.movtery.zalithlauncher.ui.screens.content.download.DownloadModPackScreenKey
import com.movtery.zalithlauncher.ui.screens.content.download.common.downloadModPackScreenKey
import com.movtery.zalithlauncher.ui.screens.content.downloadScreenKey
import kotlinx.serialization.Serializable

@Serializable
data object SearchModPackScreenKey : NavKey

@Composable
fun SearchModPackScreen() {
    var searchPlatform by remember { mutableStateOf<Platform>(Platform.CURSEFORGE) }

    val categories = remember(searchPlatform) {
        when (searchPlatform) {
            Platform.CURSEFORGE -> CurseForgeModpackCategory.entries
            Platform.MODRINTH -> ModrinthModpackCategory.entries
        }
    }

    val modLoaderFilters = remember(searchPlatform) {
        when (searchPlatform) {
            Platform.CURSEFORGE -> curseForgeModLoaderFilters
            Platform.MODRINTH -> modrinthModpackModLoaderFilters
        }
    }

    SearchAssetsScreen(
        parentScreenKey = DownloadModPackScreenKey,
        parentCurrentKey = downloadScreenKey,
        screenKey = SearchModPackScreenKey,
        currentKey = downloadModPackScreenKey,
        platformClasses = PlatformClasses.MOD_PACK,
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
                    ModrinthModpackCategory.entries.find { it.facetValue() == string }
                        ?: ModrinthFeatures.entries.find { it.facetValue() == string }
                }
                Platform.CURSEFORGE -> {
                    CurseForgeModpackCategory.entries.find { it.describe() == string }
                }
            }
        }
    )
}
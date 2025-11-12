/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.game.download.assets.platform

import com.movtery.zalithlauncher.game.download.assets.mapExceptionToMessage
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.CurseForgeSearchRequest
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeCategory
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeModLoader
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.ModrinthSearchRequest
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthFacet
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthModLoaderCategory
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.VersionFacet
import com.movtery.zalithlauncher.game.download.assets.utils.localizedModSearchKeywords
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.DownloadAssetsState
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.SearchAssetsState
import com.movtery.zalithlauncher.utils.logging.Logger.lError
import com.movtery.zalithlauncher.utils.logging.Logger.lWarning
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import java.io.File

suspend fun searchAssets(
    searchPlatform: Platform,
    searchFilter: PlatformSearchFilter,
    platformClasses: PlatformClasses,
    onSuccess: suspend (PlatformSearchResult) -> Unit,
    onError: (SearchAssetsState.Error) -> Unit
) {
    runCatching {
        val (containsChinese, englishKeywords) = searchFilter.searchName.localizedModSearchKeywords(platformClasses)
        val query = englishKeywords?.joinToString(" ") ?: searchFilter.searchName
        val result = when (searchPlatform) {
            Platform.CURSEFORGE -> {
                val curseforgeCategories = searchFilter.categories.map { category ->
                    category as? CurseForgeCategory
                }.toTypedArray()

                searchWithCurseforge(
                    request = CurseForgeSearchRequest(
                        classId = platformClasses.curseforge.classID,
                        categories = setOfNotNull(
                            *curseforgeCategories
                        ),
                        searchFilter = query,
                        gameVersion = searchFilter.gameVersion,
                        sortField = searchFilter.sortField,
                        modLoader = searchFilter.modloader as? CurseForgeModLoader,
                        index = searchFilter.index,
                        pageSize = searchFilter.limit
                    ),
                    retry = 1 //只尝试一次
                )
            }
            Platform.MODRINTH -> {
                val modrinthVersion = searchFilter.gameVersion?.let { version ->
                    VersionFacet(version)
                }
                val modrinthCategories = searchFilter.categories.map { category ->
                    category as? ModrinthFacet
                }.toTypedArray()
                val modrinthModLoader = searchFilter.modloader?.let { modloader ->
                    modloader as? ModrinthModLoaderCategory
                }

                searchWithModrinth(
                    request = ModrinthSearchRequest(
                        query = query,
                        facets = listOfNotNull(
                            platformClasses.modrinth!!, //必须为非空处理
                            modrinthVersion,
                            *modrinthCategories,
                            modrinthModLoader
                        ),
                        index = searchFilter.sortField,
                        offset = searchFilter.index,
                        limit = searchFilter.limit
                    ),
                    retry = 1 //只尝试一次
                )
            }
        }
        onSuccess(
            if (containsChinese) result.processChineseSearchResults(searchFilter.searchName, platformClasses)
            else result
        )
    }.onFailure { e ->
        if (e !is CancellationException) {
            lError("An exception occurred while searching for assets.", e)
            val pair = mapExceptionToMessage(e)
            val state = SearchAssetsState.Error(pair.first, pair.second)
            onError(state)
        } else {
            lWarning("The search task has been cancelled.")
        }
    }
}

suspend fun getVersions(
    projectID: String,
    platform: Platform,
    pageCallback: (chunk: Int, page: Int) -> Unit = { _, _ -> },
) = when (platform) {
    Platform.CURSEFORGE -> getAllVersionsFromCurseForge(projectID, pageCallback = pageCallback)
    Platform.MODRINTH -> getVersionsFromModrinth(projectID)
}

suspend fun <E> getVersions(
    projectID: String,
    platform: Platform,
    pageCallback: (chunk: Int, page: Int) -> Unit = { _, _ -> },
    onSuccess: suspend (List<PlatformVersion>) -> Unit,
    onError: (DownloadAssetsState<List<E>>) -> Unit
) {
    runCatching {
        val result = getVersions(projectID, platform, pageCallback)
        onSuccess(result)
    }.onFailure { e ->
        if (e !is CancellationException) {
            lError("An exception occurred while retrieving the project version.", e)
            val pair = mapExceptionToMessage(e)
            val state = DownloadAssetsState.Error<List<E>>(pair.first, pair.second)
            onError(state)
        } else {
            lWarning("The version retrieval task has been cancelled.")
        }
    }
}

suspend fun <E> getProject(
    projectID: String,
    platform: Platform,
    onSuccess: (PlatformProject) -> Unit,
    onError: (DownloadAssetsState<E>, Throwable) -> Unit
) {
    runCatching {
        when (platform) {
            Platform.CURSEFORGE -> getProjectFromCurseForge(projectID)
            Platform.MODRINTH -> getProjectFromModrinth(projectID)
        }
    }.fold(
        onSuccess = onSuccess,
        onFailure = { e ->
            if (e !is CancellationException) {
                lError("An exception occurred while retrieving project information.", e)
                val pair = mapExceptionToMessage(e)
                val state = DownloadAssetsState.Error<E>(pair.first, pair.second)
                onError(state, e)
            } else {
                lWarning("The project retrieval task has been cancelled.")
            }
        }
    )
}

suspend fun getProjectByVersion(
    projectId: String,
    platform: Platform
): PlatformProject = withContext(Dispatchers.IO) {
    when (platform) {
        Platform.MODRINTH -> getProjectFromModrinth(projectID = projectId)
        Platform.CURSEFORGE -> getProjectFromCurseForge(projectID = projectId)
    }
}

suspend fun getVersionByLocalFile(file: File, sha1: String): PlatformVersion? = coroutineScope {
    val modrinthDeferred = async(Dispatchers.IO) {
        runCatching {
            getVersionByLocalFileFromModrinth(sha1)
        }.getOrNull()
    }

    val curseForgeDeferred = async(Dispatchers.IO) {
        runCatching {
            getVersionByLocalFileFromCurseForge(file)
                .data.exactMatches
                ?.takeIf { it.isNotEmpty() }
                ?.firstOrNull()
                ?.file
        }.getOrNull()
    }

    val result = select {
        modrinthDeferred.onAwait { result ->
            if (result != null) {
                curseForgeDeferred.cancel()
                result
            } else {
                null
            }
        }
        curseForgeDeferred.onAwait { result ->
            if (result != null) {
                modrinthDeferred.cancel()
                result
            } else {
                null
            }
        }
    }

    result ?: run {
        if (!modrinthDeferred.isCompleted) modrinthDeferred.await()
        else if (!curseForgeDeferred.isCompleted) curseForgeDeferred.await()
        else null
    }
}
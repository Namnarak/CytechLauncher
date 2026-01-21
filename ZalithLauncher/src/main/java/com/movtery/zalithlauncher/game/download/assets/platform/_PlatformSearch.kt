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
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.CurseForgeSearcher
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.MCIM_CURSEFORGE_API
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeCategory
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeModLoader
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.MCIM_MODRINTH_API
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.ModrinthSearchRequest
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.ModrinthSearcher
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

val modrinthSearcher = ModrinthSearcher()
val mirrorModrinthSearcher = ModrinthSearcher(api = MCIM_MODRINTH_API)

val curseForgeSearcher = CurseForgeSearcher()
val mirrorCurseForgeSearcher = CurseForgeSearcher(api = MCIM_CURSEFORGE_API)

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
            Platform.CURSEFORGE -> curseForgeSearcher.searchAssets(
                query = query,
                searchFilter = searchFilter,
                platformClasses = platformClasses
            )
            Platform.MODRINTH -> modrinthSearcher.searchAssets(
                query = query,
                searchFilter = searchFilter,
                platformClasses = platformClasses
            )
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
    Platform.CURSEFORGE -> curseForgeSearcher.getVersions(
        projectID = projectID,
        pageCallback = pageCallback
    )
    Platform.MODRINTH -> modrinthSearcher.getVersions(
        projectID = projectID,
        pageCallback = pageCallback
    )
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
            Platform.CURSEFORGE -> curseForgeSearcher.getProject(projectID)
            Platform.MODRINTH -> modrinthSearcher.getProject(projectID)
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
        Platform.MODRINTH -> modrinthSearcher.getProject(projectId)
        Platform.CURSEFORGE -> curseForgeSearcher.getProject(projectId)
    }
}

suspend fun getVersionByLocalFile(file: File, sha1: String): PlatformVersion? = coroutineScope {
    val modrinthDeferred = async(Dispatchers.IO) {
        runCatching {
            modrinthSearcher.getVersionByLocalFile(file, sha1)
        }.getOrNull()
    }

    val curseForgeDeferred = async(Dispatchers.IO) {
        runCatching {
            curseForgeSearcher.getVersionByLocalFile(file, sha1)
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
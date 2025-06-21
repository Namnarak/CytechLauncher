package com.movtery.zalithlauncher.game.download.assets.platform

import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSearch.searchWithCurseforge
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSearch.searchWithModrinth
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.CurseForgeSearchRequest
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeCategory
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeModLoader
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.ModrinthSearchRequest
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthFacet
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthModLoaderCategory
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.VersionFacet
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.SearchAssetsState
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import java.net.ConnectException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException

suspend fun searchAssets(
    searchPlatform: Platform,
    searchFilter: PlatformSearchFilter,
    platformClasses: PlatformClasses,
    onSuccess: (PlatformSearchResult) -> Unit,
    onError: (SearchAssetsState.Error) -> Unit
) {
    runCatching {
        when (searchPlatform) {
            Platform.CURSEFORGE -> {
                searchWithCurseforge(
                    request = CurseForgeSearchRequest(
                        classId = platformClasses.curseforge.classID,
                        categories = setOfNotNull(
                            searchFilter.category?.let { category ->
                                category as? CurseForgeCategory
                            }
                        ),
                        searchFilter = searchFilter.searchName,
                        gameVersion = searchFilter.gameVersion,
                        sortField = searchFilter.sortField,
                        modLoader = searchFilter.modloader as? CurseForgeModLoader,
                        index = searchFilter.index,
                        pageSize = searchFilter.limit
                    )
                )
            }
            Platform.MODRINTH -> {
                searchWithModrinth(
                    request = ModrinthSearchRequest(
                        query = searchFilter.searchName,
                        facets = listOfNotNull(
                            platformClasses.modrinth!!, //必须为非空处理
                            searchFilter.gameVersion?.let { version ->
                                VersionFacet(version)
                            },
                            searchFilter.category?.let { category ->
                                category as? ModrinthFacet
                            },
                            searchFilter.modloader?.let { modloader ->
                                modloader as? ModrinthModLoaderCategory
                            }
                        ),
                        index = searchFilter.sortField,
                        offset = searchFilter.index,
                        limit = searchFilter.limit
                    )
                )
            }
        }
    }.fold(
        onSuccess = { result ->
            onSuccess(result)
        },
        onFailure = { e ->
            if (e !is CancellationException) {
                onError(mapExceptionToErrorState(e))
            }
        }
    )
}

private fun mapExceptionToErrorState(e: Throwable): SearchAssetsState.Error {
    return when (e) {
        is HttpRequestTimeoutException -> SearchAssetsState.Error(R.string.error_timeout)
        is UnknownHostException, is UnresolvedAddressException ->
            SearchAssetsState.Error(R.string.error_network_unreachable)
        is ConnectException -> SearchAssetsState.Error(R.string.error_connection_failed)
        is ResponseException -> {
            when (e.response.status) {
                HttpStatusCode.Unauthorized -> SearchAssetsState.Error(R.string.error_unauthorized)
                HttpStatusCode.NotFound -> SearchAssetsState.Error(R.string.error_notfound)
                else -> SearchAssetsState.Error(R.string.error_client_error, arrayOf(e.response.status))
            }
        }
        else -> {
            val errorMessage = e.localizedMessage ?: e::class.simpleName ?: "Unknown error"
            SearchAssetsState.Error(R.string.error_unknown, arrayOf(errorMessage))
        }
    }
}
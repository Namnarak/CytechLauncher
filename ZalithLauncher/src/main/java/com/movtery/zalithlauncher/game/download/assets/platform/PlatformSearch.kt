package com.movtery.zalithlauncher.game.download.assets.platform

import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.CurseForgeSearchRequest
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.CurseForgeSearchResult
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.ModrinthSearchRequest
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.ModrinthSearchResult
import com.movtery.zalithlauncher.info.InfoDistributor
import com.movtery.zalithlauncher.utils.network.httpGet
import com.movtery.zalithlauncher.utils.network.withRetry

object PlatformSearch {
    /**
     * CurseForge 平台的 API 链接
     * [CurseForge REST API](https://docs.curseforge.com/rest-api/?shell#base-url)
     */
    const val CURSEFORGE_API = "https://api.curseforge.com/v1"

    /**
     * Modrinth 平台的 API 链接
     * [Modrinth Docs](https://docs.modrinth.com/api/operations/searchprojects)
     */
    const val MODRINTH_API = "https://api.modrinth.com/v2"

    /**
     * 向 CurseForge 平台发送搜索请求
     * @param request 搜索请求
     * @param apiKey CurseForge API 密钥
     */
    suspend fun searchWithCurseforge(
        request: CurseForgeSearchRequest,
        apiKey: String = InfoDistributor.CURSEFORGE_API
    ): CurseForgeSearchResult = withRetry("PlatformSearch:CurseForge") {
        httpGet(
            url = "$CURSEFORGE_API/mods/search",
            headers = listOf("x-api-key" to apiKey),
            parameters = request.toParameters()
        )
    }

    /**
     * 向 Modrinth 平台发送搜索请求
     * @param request 搜索请求
     */
    suspend fun searchWithModrinth(
        request: ModrinthSearchRequest
    ): ModrinthSearchResult = withRetry("PlatformSearch:Modrinth") {
        httpGet(
            url = "$MODRINTH_API/search",
            parameters = request.toParameters()
        )
    }
}
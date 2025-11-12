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

import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.CurseForgeSearchRequest
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.CurseForgeSearchResult
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeFile
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeFingerprintsMatches
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeProject
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeVersion
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeVersions
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.ModrinthSearchRequest
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.ModrinthSearchResult
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthSingleProject
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthVersion
import com.movtery.zalithlauncher.info.InfoDistributor
import com.movtery.zalithlauncher.utils.file.MurmurHash2Incremental
import com.movtery.zalithlauncher.utils.network.httpGetJson
import com.movtery.zalithlauncher.utils.network.httpPostJson
import com.movtery.zalithlauncher.utils.network.withRetry
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.Parameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.io.File

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
    apiKey: String = InfoDistributor.CURSEFORGE_API,
    retry: Int = 3
): CurseForgeSearchResult = withRetry("PlatformSearch:CurseForge_search", maxRetries = retry) {
    httpGetJson(
        url = "$CURSEFORGE_API/mods/search",
        headers = listOf("x-api-key" to apiKey),
        parameters = request.toParameters()
    )
}

/**
 * 在 CurseForge 平台获取项目详细信息
 * @param apiKey CurseForge API 密钥
 */
suspend fun getProjectFromCurseForge(
    projectID: String,
    apiKey: String = InfoDistributor.CURSEFORGE_API,
    retry: Int = 3
): CurseForgeProject = withRetry("PlatformSearch:CurseForge_getProject", maxRetries = retry) {
    httpGetJson(
        url = "$CURSEFORGE_API/mods/$projectID",
        headers = listOf("x-api-key" to apiKey)
    )
}

/**
 * 在 CurseForge 平台根据分页获取项目的版本列表
 * @param apiKey CurseForge API 密钥
 * @param index 开始处
 * @param pageSize 每页请求数量
 */
suspend fun getVersionsFromCurseForge(
    projectID: String,
    apiKey: String = InfoDistributor.CURSEFORGE_API,
    index: Int = 0,
    pageSize: Int = 100,
    retry: Int = 3
): CurseForgeVersions = withRetry("PlatformSearch:CurseForge_getVersions", maxRetries = retry) {
    httpGetJson(
        url = "$CURSEFORGE_API/mods/$projectID/files",
        headers = listOf("x-api-key" to apiKey),
        parameters = Parameters.build {
            append("index", index.toString())
            append("pageSize", pageSize.toString())
        }
    )
}

/**
 * 持续分页获取 CurseForge 项目的所有版本文件，直到全部加载完成
 * @param projectID 项目ID
 * @param apiKey CurseForge API 密钥
 * @param pageSize 每页请求数量
 * @param chunkSize 一个区间的最大页数
 * @param maxConcurrent 同时最多允许的请求数
 * @param pageCallback 加载每一页时都通过此函数回调
 */
suspend fun getAllVersionsFromCurseForge(
    projectID: String,
    apiKey: String = InfoDistributor.CURSEFORGE_API,
    pageSize: Int = 50,
    chunkSize: Int = 20,
    maxConcurrent: Int = 10,
    pageCallback: (chunk: Int, page: Int) -> Unit = { _ , _ -> },
    retry: Int = 3
): List<CurseForgeFile> = withContext(Dispatchers.IO) {
    getAllVersions(
        pageSize = pageSize,
        chunkSize = chunkSize,
        maxConcurrent = maxConcurrent,
        pageCallback = pageCallback,
        checkNotEmpty = { versions ->
            versions.data.isNotEmpty()
        },
        asyncVersions = { index, pageSize ->
            getVersionsFromCurseForge(
                projectID = projectID,
                apiKey = apiKey,
                index = index,
                pageSize = pageSize,
                retry = retry
            )
        },
        processVersions = { versions ->
            val files = versions?.data ?: emptyArray()
            files.toList() to files.size
        }
    )
}

/**
 * 在 CurseForge 平台获取某项目的某个文件
 */
suspend fun getVersionFromCurseForge(
    projectID: String,
    fileID: String,
    apiKey: String = InfoDistributor.CURSEFORGE_API,
    retry: Int = 3
): CurseForgeVersion = withRetry("PlatformSearch:CurseForge_getVersion", maxRetries = retry) {
    httpGetJson(
        url = "$CURSEFORGE_API/mods/$projectID/files/$fileID",
        headers = listOf("x-api-key" to apiKey)
    )
}

suspend fun getVersionByLocalFileFromCurseForge(
    file: File,
    apiKey: String = InfoDistributor.CURSEFORGE_API,
    retry: Int = 1
): CurseForgeFingerprintsMatches = withRetry("PlatformSearch:CurseForge_getVersionByLocalFile", maxRetries = retry) {
    val hash = MurmurHash2Incremental.computeHash(file, byteToSkip = listOf(0x9, 0xa, 0xd, 0x20))
    httpPostJson(
        url = "$CURSEFORGE_API/fingerprints",
        headers = listOf("x-api-key" to apiKey),
        body = mapOf("fingerprints" to listOf(hash))
    )
}

/**
 * 向 Modrinth 平台发送搜索请求
 * @param request 搜索请求
 */
suspend fun searchWithModrinth(
    request: ModrinthSearchRequest,
    retry: Int = 3
): ModrinthSearchResult = withRetry("PlatformSearch:Modrinth_search", maxRetries = retry) {
    httpGetJson(
        url = "$MODRINTH_API/search",
        parameters = request.toParameters()
    )
}

/**
 * 在 Modrinth 平台获取项目详细信息
 */
suspend fun getProjectFromModrinth(
    projectID: String,
    retry: Int = 3
): ModrinthSingleProject = withRetry("PlatformSearch:Modrinth_getProject", maxRetries = retry) {
    httpGetJson(
        url = "$MODRINTH_API/project/$projectID"
    )
}

/**
 * 获取 Modrinth 项目的所有版本
 * @param pageSize 每页请求数量，null则为获取所有版本
 * @param offset 开始处，null则为获取所有版本
 */
suspend fun getVersionsFromModrinth(
    projectID: String,
    pageSize: Int? = null,
    offset: Int? = null,
    retry: Int = 3
): List<ModrinthVersion> = withRetry("PlatformSearch:Modrinth_getVersions", maxRetries = retry) {
    httpGetJson(
        url = "$MODRINTH_API/project/$projectID/version",
        parameters = if (pageSize != null && offset != null) {
            Parameters.build {
                append("limit", pageSize.toString())
                append("offset", offset.toString())
            }
        } else null
    )
}

suspend fun getVersionByLocalFileFromModrinth(
    sha1: String,
    retry: Int = 1
): ModrinthVersion? = withRetry("PlatformSearch:Modrinth_getVersionByLocalFile", maxRetries = retry) {
    try {
        httpGetJson(
            url = "$MODRINTH_API/version_file/$sha1",
            parameters = Parameters.build {
                append("algorithm", "sha1")
            }
        )
    } catch (_: ClientRequestException) {
        return@withRetry null
    }
}

/**
 * 持续分页获取项目的所有版本文件，直到全部加载完成
 * @param pageSize 每页请求数量
 * @param chunkSize 一个区间的最大页数
 * @param maxConcurrent 同时最多允许的请求数
 * @param pageCallback 加载每一页时都通过此函数回调
 * @param checkNotEmpty 检查请求内容返回结果不为空
 * @param asyncVersions 异步获取单区块的版本数据
 * @param processVersions 加工返回数据，同时需要返回当前结果实际的页面大小
 */
private suspend fun <E, T> getAllVersions(
    pageSize: Int = 100,
    chunkSize: Int = 10,
    maxConcurrent: Int = 5,
    pageCallback: (chunk: Int, page: Int) -> Unit = { _ , _ -> },
    checkNotEmpty: (E) -> Boolean,
    asyncVersions: suspend (index: Int, pageSize: Int) -> E,
    processVersions: suspend (E?) -> Pair<List<T>, Int>
): List<T> = coroutineScope {
    val allVersions = mutableListOf<T>()
    /** 当前区间编号 */
    var currentChunk = 1
    /** 起始页码 */
    var startPage = 0
    /** 是否已经到达过最后一页，控制是否进入下一区间 */
    var reachedEnd = false

    val semaphore = Semaphore(maxConcurrent)

    while (!reachedEnd) {
        //创建当前区间的任务列表
        val jobs = (0 until chunkSize).map { offset ->
            val pageIndex = startPage + offset
            val index = pageIndex * pageSize

            async {
                semaphore.withPermit {
                    val response = asyncVersions(index, pageSize)
                    //检查当前页返回的结果是否正常
                    //如果是最后一页之后的内容，则这里的列表是空的
                    if (checkNotEmpty(response)) {
                        //有东西，回调即可
                        pageCallback(currentChunk, pageIndex + 1)
                        response
                    } else null
                }
            }
        }

        for ((i, job) in jobs.withIndex()) {
            val (files, realSize) = processVersions(job.await())
            files.takeIf { it.isNotEmpty() }?.let { list ->
                allVersions.addAll(list)
            }

            //少于pageSize，已经是最后一页
            if (realSize < pageSize) {
                reachedEnd = true
                //取消后续页
                for (j in (i + 1) until jobs.size) {
                    jobs[j].cancel()
                }
                break
            }
        }

        //如果没发现最后一页，则进入下一区间
        if (!reachedEnd) {
            startPage += chunkSize
            currentChunk++
        }
    }

    return@coroutineScope allVersions
}
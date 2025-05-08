package com.movtery.zalithlauncher.game.addons.modloader.fabriclike

import android.util.Log
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.models.FabricLikeGame
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.models.FabricLikeLoader
import com.movtery.zalithlauncher.path.UrlManager
import com.movtery.zalithlauncher.utils.network.withRetry
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

abstract class FabricLikeVersions(private val baseUrl: String) {
    private var cacheGames: List<FabricLikeGame>? = null
    private var cacheLoaders: List<FabricLikeLoader>? = null

    protected val client = HttpClient(CIO) {
        install(HttpTimeout) {
            requestTimeoutMillis = UrlManager.TIME_OUT.first.toLong()
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                coerceInputValues = true
            })
        }
        expectSuccess = true
    }

    /**
     * 通用 Loader 列表获取
     */
    protected suspend fun fetchLoaderList(
        force: Boolean,
        tag: String,
        mcVersion: String
    ): List<FabricLikeLoader>? = withContext(Dispatchers.Default) {
        try {
            val games: List<FabricLikeGame> = run {
                if (!force && cacheGames != null) return@run cacheGames!!
                withContext(Dispatchers.IO) {
                    withRetry(tag, maxRetries = 2) { client.get(gameUrl).body() }
                }
            }.also {
                cacheGames = it
            }

            if (!games.any { it.version == mcVersion }) {
                Log.w(tag, "The version $mcVersion does not have a corresponding loader.")
                return@withContext null
            }

            if (!force && cacheLoaders != null) {
                cacheLoaders!!
            } else {
                withContext(Dispatchers.IO) {
                    withRetry(tag, maxRetries = 2) { client.get(loaderUrl).body<List<FabricLikeLoader>>() }
                }
            }.also {
                cacheLoaders = it
            }
        } catch (e: CancellationException) {
            Log.d(tag, "Client cancelled.")
            null
        } catch (e: Exception) {
            Log.w(tag, "Failed to fetch loader list!", e)
            throw e
        }
    }


    protected val installerUrl: String
        get() = "$baseUrl/versions/installer"

    protected val loaderUrl: String
        get() = "$baseUrl/versions/loader"

    protected val gameUrl: String
        get() = "$baseUrl/versions/game"
}
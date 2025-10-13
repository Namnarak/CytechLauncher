package com.movtery.zalithlauncher.path

import com.movtery.zalithlauncher.BuildConfig
import com.movtery.zalithlauncher.info.InfoDistributor
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.util.concurrent.TimeUnit

const val URL_USER_AGENT: String = "${InfoDistributor.LAUNCHER_SHORT_NAME}/${BuildConfig.VERSION_NAME}"
val TIME_OUT = Pair(10000, TimeUnit.MILLISECONDS)
const val URL_MCMOD: String = "https://www.mcmod.cn/"
const val URL_MINECRAFT_VERSION_REPOS: String = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
const val URL_MINECRAFT_PURCHASE = "https://www.xbox.com/games/store/minecraft-java-bedrock-edition-for-pc/9nxp44l49shj"
const val URL_PROJECT: String = "https://github.com/ZalithLauncher/ZalithLauncher2"
const val URL_COMMUNITY: String = "https://github.com/ZalithLauncher/ZalithLauncher2/graphs/contributors"
const val URL_WEBLATE: String = "https://hosted.weblate.org/projects/zalithlauncher2"
const val URL_SUPPORT: String = "https://afdian.com/a/MovTery"

val GLOBAL_CLIENT = HttpClient(CIO) {
    install(HttpTimeout) {
        requestTimeoutMillis = TIME_OUT.first.toLong()
    }
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            explicitNulls = true
            coerceInputValues = true
        })
    }
    expectSuccess = true

    defaultRequest {
        headers {
            append(HttpHeaders.UserAgent, URL_USER_AGENT)
        }
    }
}

fun createRequestBuilder(url: String): Request.Builder {
    return createRequestBuilder(url, null)
}

fun createRequestBuilder(url: String, body: RequestBody?): Request.Builder {
    val request = Request.Builder().url(url).header("User-Agent", URL_USER_AGENT)
    body?.let{ request.post(it) }
    return request
}

fun createOkHttpClient(): OkHttpClient = createOkHttpClientBuilder().build()

/**
 * 创建一个OkHttpClient，可自定义一些内容
 */
fun createOkHttpClientBuilder(action: (OkHttpClient.Builder) -> Unit = { }): OkHttpClient.Builder {
    return OkHttpClient.Builder()
        .callTimeout(TIME_OUT.first.toLong(), TIME_OUT.second)
        .apply(action)
}
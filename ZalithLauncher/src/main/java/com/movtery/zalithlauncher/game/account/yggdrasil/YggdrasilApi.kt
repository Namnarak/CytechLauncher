package com.movtery.zalithlauncher.game.account.yggdrasil

import com.movtery.zalithlauncher.game.account.microsoft.MinecraftProfileException
import com.movtery.zalithlauncher.game.account.microsoft.MinecraftProfileException.ExceptionStatus.FREQUENT
import com.movtery.zalithlauncher.game.account.microsoft.MinecraftProfileException.ExceptionStatus.PROFILE_NOT_EXISTS
import com.movtery.zalithlauncher.game.skin.SkinModelType
import com.movtery.zalithlauncher.path.UrlManager.Companion.GLOBAL_CLIENT
import com.movtery.zalithlauncher.utils.logging.Logger.lInfo
import com.movtery.zalithlauncher.utils.network.withRetry
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.io.File

/**
 * 使用 Yggdrasil 上传皮肤
 */
suspend fun uploadSkin(
    apiUrl: String,
    accessToken: String,
    file: File,
    modelType: SkinModelType,
    maxRetries: Int = 1
) {
    val skinData = file.readBytes()
    val logTag = "YggdrasilApi.uploadSkin"

    lInfo("$logTag: uploading skin -> ${file.name}")
    withRetry(logTag = logTag, maxRetries = maxRetries) {
        GLOBAL_CLIENT.submitFormWithBinaryData(
            url = "$apiUrl/minecraft/profile/skins",
            formData = formData {
                append("variant", modelType.modelType)
                append("file", skinData, Headers.build {
                    append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                })
            }
        ) {
            method = HttpMethod.Post
            headers {
                append(HttpHeaders.Authorization, "Bearer $accessToken")
            }
        }
    }
}

/**
 * 使用 Yggdrasil 更改玩家披风
 * @param capeId 披风的uuid，为null则表示重置披风
 */
suspend fun changeCape(
    apiUrl: String,
    accessToken: String,
    capeId: String? = null,
    maxRetries: Int = 1
) {
    val url = "$apiUrl/minecraft/profile/capes/active"
    val logTag = "YggdrasilApi.changeCape"

    if (capeId == null) {
        //重置玩家选择的披风
        lInfo("$logTag: reset cape")
        withRetry(logTag = logTag, maxRetries = maxRetries) {
            GLOBAL_CLIENT.request(url) {
                method = HttpMethod.Delete
                headers {
                    append("Authorization", "Bearer $accessToken")
                    append("Content-Type", "application/json")
                }
            }
        }
    } else {
        lInfo("$logTag: capeId -> $capeId")
        withRetry(logTag = logTag, maxRetries = maxRetries) {
            GLOBAL_CLIENT.request(url) {
                method = HttpMethod.Put
                headers {
                    append("Authorization", "Bearer $accessToken")
                    append("Content-Type", "application/json")
                }
                setBody(JsonObject(mapOf("capeId" to JsonPrimitive(capeId))))
            }
        }
    }
}

/**
 * 使用 Yggdrasil 获取玩家配置信息
 */
suspend fun getPlayerProfile(
    apiUrl: String,
    accessToken: String
) = runCatching {
    GLOBAL_CLIENT.get("$apiUrl/minecraft/profile") {
        header(HttpHeaders.Authorization, "Bearer $accessToken")
    }.body<PlayerProfile>()
}.onFailure { e ->
    if (e is ResponseException) {
        when (e.response.status.value) {
            429 -> throw MinecraftProfileException(FREQUENT)
            404 -> throw MinecraftProfileException(PROFILE_NOT_EXISTS)
        }
    }
}.getOrThrow()
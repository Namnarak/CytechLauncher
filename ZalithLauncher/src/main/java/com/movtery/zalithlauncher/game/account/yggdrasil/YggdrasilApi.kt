package com.movtery.zalithlauncher.game.account.yggdrasil

import com.movtery.zalithlauncher.game.skin.SkinModelType
import com.movtery.zalithlauncher.path.UrlManager.Companion.GLOBAL_CLIENT
import com.movtery.zalithlauncher.utils.network.withRetry
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.JsonObject
import java.io.File

const val MOJANG_API_URL = "https://api.minecraftservices.com"

/**
 * 使用 Yggdrasil 上传皮肤
 */
suspend fun uploadSkin(
    apiUrl: String,
    accessToken: String,
    file: File,
    modelType: SkinModelType,
    maxRetries: Int = 1,
    onSuccess: suspend () -> Unit = {},
    onFailed: suspend (response: HttpResponse, body: JsonObject) -> Unit
) {
    val skinData = file.readBytes()

    val response = withRetry(logTag = "YggdrasilApi.uploadSkin", maxRetries = maxRetries) {
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

    val json = response.body<JsonObject>()
    if (
        response.status == HttpStatusCode.NoContent ||
        response.status == HttpStatusCode.OK ||
        json.isNotEmpty()
    ) {
        onSuccess()
    } else {
        onFailed(response, json)
    }
}
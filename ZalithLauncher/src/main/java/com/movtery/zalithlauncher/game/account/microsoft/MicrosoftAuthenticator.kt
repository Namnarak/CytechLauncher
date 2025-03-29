package com.movtery.zalithlauncher.game.account.microsoft

import android.util.Log
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountType
import com.movtery.zalithlauncher.game.account.microsoft.response.DeviceCodeResponse
import com.movtery.zalithlauncher.game.account.microsoft.response.MinecraftAuthResponse
import com.movtery.zalithlauncher.game.account.microsoft.response.TokenResponse
import com.movtery.zalithlauncher.info.InfoDistributor
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.IOException
import java.util.UUID
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

object MicrosoftAuthenticator {
    private val SCOPES = listOf("XboxLive.signin", "offline_access", "openid", "profile", "email")
    private const val TENANT = "/consumers"

    private const val MICROSOFT_AUTH_URL = "https://login.microsoftonline.com"
    private const val LIVE_AUTH_URL = "https://login.live.com"
    private const val XBL_AUTH_URL = "https://user.auth.xboxlive.com"
    private const val XSTS_AUTH_URL = "https://xsts.auth.xboxlive.com"
    private const val MINECRAFT_SERVICES_URL = "https://api.minecraftservices.com"

    @OptIn(ExperimentalSerializationApi::class)
    private val HTTP_CLIENT = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                coerceInputValues = true
            })
        }
    }

    private suspend inline fun <reified T> submitForm(
        url: String,
        parameters: Parameters,
        context: CoroutineContext = EmptyCoroutineContext
    ): T = withContext(context) {
        return@withContext HTTP_CLIENT.submitForm(
            url = url,
            formParameters = parameters
        ) {
            contentType(ContentType.Application.FormUrlEncoded)
        }.body()
    }

    private suspend inline fun <reified T> httpPostJson(
        url: String,
        body: Any,
        context: CoroutineContext = EmptyCoroutineContext
    ): T = withContext(context) {
        val response = HTTP_CLIENT.post(url) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        return@withContext try {
            Json.decodeFromString(response.bodyAsText())
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to parse response: ${e.message}")
        }
    }

    /**
     * 从 Microsoft 身份验证终端节点获取设备代码响应
     * 设备代码用于在单独的设备或浏览器上授权用户
     */
    suspend fun fetchDeviceCodeResponse(): DeviceCodeResponse = withRetry {
        submitForm(
            url = "$MICROSOFT_AUTH_URL/$TENANT/oauth2/v2.0/devicecode",
            parameters = Parameters.build {
                append("client_id", InfoDistributor.OAUTH_CLIENT_ID)
                append("scope", SCOPES.joinToString(" "))
            }
        )
    }

    /**
     * 使用设备代码流从 Microsoft Azure Active Directory 检索访问令牌和刷新令牌
     * 此函数会定期轮询 Microsoft 令牌端点，直到获取访问令牌或超时
     */
    suspend fun getTokenResponse(
        codeResponse: DeviceCodeResponse
    ): TokenResponse = coroutineScope {
        var pollingInterval = codeResponse.interval * 1000L
        val expireTime = System.currentTimeMillis() + codeResponse.expiresIn * 1000L

        while (System.currentTimeMillis() < expireTime) {
            ensureActive()

            try {
                val response: JsonObject = submitForm(
                    "$MICROSOFT_AUTH_URL$TENANT/oauth2/v2.0/token",
                    parameters = Parameters.build {
                        append("grant_type", "urn:ietf:params:oauth:grant-type:device_code")
                        append("device_code", codeResponse.deviceCode)
                        append("client_id", InfoDistributor.OAUTH_CLIENT_ID)
                        append("tenant", TENANT)
                    }
                )

                if (response["token_type"]?.jsonPrimitive?.content == "Bearer") {
                    return@coroutineScope TokenResponse(
                        accessToken = response["access_token"].text(),
                        refreshToken = response["refresh_token"].text(),
                        expiresIn = response["expires_in"]?.jsonPrimitive?.int ?: 0
                    )
                }
            } catch (e: ClientRequestException) {
                val errorBody = Json.parseToJsonElement(e.response.bodyAsText()).jsonObject
                val error = errorBody["error"]?.jsonPrimitive?.content

                when (error) {
                    "authorization_pending" -> { /* 正常情况，继续轮询 */ }
                    "slow_down" -> { //延长轮询间隔
                        pollingInterval += 1000L
                    }
                    else -> throw e
                }
            }
            delay(pollingInterval)
        }
        throw TimeoutException("Authentication timed out!")
    }

    /**
     * 使用不同的身份验证类型异步验证用户，并检索其 Minecraft 帐户信息
     * 函数通过执行一系列步骤来编排身份验证过程，具体取决于提供的 [authType]。
     *
     * 支持刷新现有访问令牌或使用提供的访问令牌。然后，继续使用 Xbox Live （XBL）、Xbox 安全令牌服务 （XSTS） 进行身份验证，最后访问 Minecraft。
     *
     * 支持验证用户是否拥有游戏，然后创建 'Account' 对象。
     *
     * @param statusUpdate 验证执行到哪个步骤，通过这个进行回调更新
     */
    suspend fun authAsync(
        authType: AuthType,
        refreshToken: String,
        accessToken: String = "NULL",
        statusUpdate: (AsyncStatus) -> Unit
    ): Account = coroutineScope {
        val (finalAccessToken, newRefreshToken) = when (authType) {
            AuthType.Refresh -> refreshAccessToken(refreshToken, statusUpdate)
            else -> Pair(accessToken, refreshToken)
        }

        val xblToken = authenticateXBL(finalAccessToken, statusUpdate)
        val xstsToken = authenticateXSTS(xblToken.first, xblToken.second, statusUpdate)
        val minecraftToken = authenticateMinecraft(xstsToken, statusUpdate)
        verifyGameOwnership(minecraftToken, statusUpdate)

        return@coroutineScope createAccount(minecraftToken, newRefreshToken, xblToken.second, statusUpdate)
    }

    private suspend fun refreshAccessToken(refreshToken: String, update: (AsyncStatus) -> Unit): Pair<String, String> {
        update(AsyncStatus.GETTING_ACCESS_TOKEN)

        return withRetry {
            val response = submitForm<JsonObject>(
                url = "$LIVE_AUTH_URL/oauth20_token.srf",
                parameters = Parameters.build {
                    append("client_id", InfoDistributor.OAUTH_CLIENT_ID)
                    append("refresh_token", refreshToken)
                    append("grant_type", "refresh_token")
                }
            )
            Pair(
                response["access_token"].text(),
                response["refresh_token"]?.jsonPrimitive?.content ?: refreshToken
            )
        }
    }

    private suspend fun authenticateXBL(accessToken: String, update: (AsyncStatus) -> Unit): Pair<String, String> {
        update(AsyncStatus.GETTING_XBL_TOKEN)
        val requestBody = XBLRequest(
            properties = XBLProperties(
                authMethod = "RPS",
                siteName = "user.auth.xboxlive.com",
                rpsTicket = "d=$accessToken"
            ),
            relyingParty = "http://auth.xboxlive.com",
            tokenType = "JWT"
        )

        return withRetry {
            val response = HTTP_CLIENT.post("$XBL_AUTH_URL/user/authenticate") {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body<JsonObject>()

            //提取uhs
            val uhs = response["DisplayClaims"]?.jsonObject
                ?.get("xui")?.jsonArray
                ?.firstOrNull()?.jsonObject
                ?.get("uhs")?.jsonPrimitive
                ?.content ?: throw Exception("Missing uhs in XBL response")

            Pair(response["Token"].text(), uhs)
        }
    }

    private suspend fun authenticateXSTS(xblToken: String, uhs: String, update: (AsyncStatus) -> Unit): XSTSAuthResult {
        update(AsyncStatus.GETTING_XSTS_TOKEN)

        return withRetry {
            val response = httpPostJson<JsonObject>(
                "$XSTS_AUTH_URL/xsts/authorize",
                XSTSRequest(
                    properties = XSTSProperties(
                        sandboxId = "RETAIL",
                        userTokens = listOf(xblToken)
                    ),
                    relyingParty = "rp://api.minecraftservices.com/",
                    tokenType = "JWT"
                )
            )
            XSTSAuthResult(token = response["Token"].text(), uhs = uhs)
        }
    }

    private suspend fun authenticateMinecraft(
        xstsResult: XSTSAuthResult,
        update: (AsyncStatus) -> Unit
    ): String {
        update(AsyncStatus.AUTHENTICATE_MINECRAFT)

        return withRetry {
            val authResponse = httpPostJson<MinecraftAuthResponse>(
                "$MINECRAFT_SERVICES_URL/authentication/login_with_xbox",
                mapOf("identityToken" to "XBL3.0 x=${xstsResult.uhs};${xstsResult.token}")
            )
            authResponse.accessToken
        }
    }

    private suspend fun verifyGameOwnership(accessToken: String, update: (AsyncStatus) -> Unit) {
        update(AsyncStatus.VERIFY_GAME_OWNERSHIP)
        withRetry {
            val response = HTTP_CLIENT.get("$MINECRAFT_SERVICES_URL/entitlements/mcstore") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }
            if (Json.parseToJsonElement(response.bodyAsText()).jsonObject["items"]?.jsonArray?.isEmpty() != false) {
                throw NotPurchasedMinecraftException()
            }
        }
    }

    private suspend fun createAccount(
        accessToken: String,
        refreshToken: String,
        uhs: String,
        statusUpdate: (AsyncStatus) -> Unit
    ): Account {
        statusUpdate(AsyncStatus.GETTING_PLAYER_PROFILE)

        val profile = HTTP_CLIENT.get("$MINECRAFT_SERVICES_URL/minecraft/profile") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }.body<JsonObject>()

        return Account().apply {
            this.username = profile["name"].text()
            this.accessToken = accessToken
            this.accountType = AccountType.MICROSOFT.tag
            this.clientToken = UUID.randomUUID().toString().replace("-", "")
            this.profileId = profile["id"].text()
            this.refreshToken = refreshToken.ifEmpty { "None" }
            this.xuid = uhs
        }
    }

    private fun JsonElement?.text() = this?.jsonPrimitive?.content.orEmpty()

    private suspend fun <T> withRetry(
        maxRetries: Int = 3,
        initialDelay: Long = 1000,
        maxDelay: Long = 10_000,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelay
        var retryCount = 0
        var lastError: Throwable? = null

        while (retryCount < maxRetries) {
            try {
                return block()
            } catch (e: Exception) {
                Log.d("MicrosoftAuthenticator", "Attempt ${retryCount + 1} failed: ${e.message}")
                lastError = e
                if (canRetry(e)) {
                    delay(currentDelay)
                    currentDelay = (currentDelay * 2).coerceAtMost(maxDelay)
                    retryCount++
                } else {
                    throw e //不可重试
                }
            }
        }
        throw lastError ?: Exception("Failed after $maxRetries retries")
    }

    private fun canRetry(e: Exception): Boolean {
        return when (e) {
            is ClientRequestException -> e.response.status.value in 500..599 //5xx错误可重试
            is IOException -> true //网络错误
            else -> false
        }
    }
}
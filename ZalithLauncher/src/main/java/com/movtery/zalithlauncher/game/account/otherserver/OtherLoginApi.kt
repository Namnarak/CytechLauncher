package com.movtery.zalithlauncher.game.account.otherserver

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.otherserver.models.AuthRequest
import com.movtery.zalithlauncher.game.account.otherserver.models.AuthResult
import com.movtery.zalithlauncher.game.account.otherserver.models.Refresh
import com.movtery.zalithlauncher.path.UrlManager
import com.movtery.zalithlauncher.path.UrlManager.Companion.createRequestBuilder
import com.movtery.zalithlauncher.utils.string.StringUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.Objects
import java.util.UUID

object OtherLoginApi {
    private var client: OkHttpClient = UrlManager.createOkHttpClient()
    private var baseUrl: String? = null

    fun setBaseUrl(baseUrl: String) {
        var url = baseUrl
        if (baseUrl.endsWith("/")) {
            url = baseUrl.substring(0, baseUrl.length - 1)
        }
        OtherLoginApi.baseUrl = url
    }

    @Throws(IOException::class)
    suspend fun login(context: Context, userName: String?, password: String?,
              onSuccess: suspend (AuthResult) -> Unit = {},
              onFailed: suspend (error: String) -> Unit = {}
    ) {
        if (Objects.isNull(baseUrl)) {
            onFailed(context.getString(R.string.account_other_login_baseurl_not_set))
            return
        }
        val agent = AuthRequest.Agent().apply {
            this.name = "Minecraft"
            this.version = 1.0
        }
        val authRequest = AuthRequest()
            .apply {
            this.username = userName
            this.password = password
            this.agent = agent
            this.requestUser = true
            this.clientToken = UUID.randomUUID().toString().lowercase()
        }
        val data = Gson().toJson(authRequest)
        callLogin(data, "/authserver/authenticate", onSuccess, onFailed)
    }

    @Throws(IOException::class)
    suspend fun refresh(context: Context, account: Account, select: Boolean,
                onSuccess: suspend (AuthResult) -> Unit = {},
                onFailed: suspend (error: String) -> Unit = {}
    ) {
        if (Objects.isNull(baseUrl)) {
            onFailed(context.getString(R.string.account_other_login_baseurl_not_set))
            return
        }
        val refresh = Refresh()
            .apply {
            this.clientToken = account.clientToken
            this.accessToken = account.accessToken
        }
        if (select) {
            val selectedProfile = Refresh.SelectedProfile().apply {
                this.name = account.username
                this.id = account.profileId
            }
            refresh.selectedProfile = selectedProfile
        }
        val data = Gson().toJson(refresh)
        callLogin(data, "/authserver/refresh", onSuccess, onFailed)
    }

    private suspend fun callLogin(
        data: String, url: String,
        onSuccess: suspend (AuthResult) -> Unit = {},
        onFailed: suspend (error: String) -> Unit = {}
    ) = withContext(Dispatchers.IO) {
        val body = data.toRequestBody("application/json".toMediaTypeOrNull())
        val call = client.newCall(createRequestBuilder(baseUrl + url, body).build())

        call.execute().use { response ->
            val res = response.body?.string()
            if (response.code == 200) {
                val result = Gson().fromJson(res, AuthResult::class.java)
                onSuccess(result)
            } else {
                var errorMessage: String = res ?: "null"
                runCatching parseMessage@{
                    if (errorMessage == "null") return@parseMessage

                    val jsonObject = JSONObject(errorMessage)
                    errorMessage = if (jsonObject.has("errorMessage")) {
                        jsonObject.getString("errorMessage")
                    } else if (jsonObject.has("message")) {
                        jsonObject.getString("message")
                    } else {
                        Log.e("Other Login", "The error message returned by the server could not be retrieved.")
                        return@parseMessage
                    }

                    if (errorMessage.contains("\\u"))
                        errorMessage = StringUtils.decodeUnicode(errorMessage.replace("\\\\u", "\\u"))
                }.onFailure { e -> Log.e("Other Login", "Failed to parse error message.", e) }
                onFailed("(${response.code}) $errorMessage")
            }
        }
    }

    fun getServeInfo(url: String): String? {
        return client.newCall(createRequestBuilder(url).get().build()).execute().use { response ->
            val res = response.body?.string()
            if (response.code == 200) res else null
        }
    }
}
package com.movtery.zalithlauncher.game.account

import android.content.Context
import android.util.Log
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.otherserver.OtherLoginHelper
import com.movtery.zalithlauncher.task.TaskSystem
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.Objects

fun isOtherLoginAccount(account: Account): Boolean {
    return !Objects.isNull(account.otherBaseUrl) && account.otherBaseUrl != "0"
}

fun isMicrosoftAccount(account: Account): Boolean {
    return account.accountType == AccountType.MICROSOFT.tag
}

fun isNoLoginRequired(account: Account?): Boolean {
    return account == null || account.accountType == AccountType.LOCAL.tag
}

fun otherLogin(
    context: Context,
    account: Account,
    onSuccess: (Account) -> Unit = {},
    onFailed: (error: String) -> Unit = {}
) {
    if (TaskSystem.containsTask(account.uniqueUUID)) return

    OtherLoginHelper(
        baseUrl = account.otherBaseUrl!!,
        serverName = account.accountType!!,
        email = account.otherAccount!!,
        password = account.otherPassword!!,
        object : OtherLoginHelper.OnLoginListener {
            override fun onSuccess(account: Account) {
                onSuccess(account)
            }

            override fun onFailed(error: String) {
                onFailed(error)
            }
        }
    ).justLogin(context, account)
}

/**
 * 获取账号类型名称
 */
fun getAccountTypeName(context: Context, account: Account): String {
    return if (isMicrosoftAccount(account)) {
        context.getString(R.string.account_type_microsoft)
    } else if (isOtherLoginAccount(account)) {
        account.accountType ?: "Unknown"
    } else {
        context.getString(R.string.account_type_local)
    }
}

/**
 * 修改自源代码：[HMCL Core: AuthlibInjectorServer.java](https://github.com/HMCL-dev/HMCL/blob/main/HMCLCore/src/main/java/org/jackhuang/hmcl/auth/authlibinjector/AuthlibInjectorServer.java#L60-#L76)
 * <br>原项目版权归原作者所有，遵循GPL v3协议
 */
fun tryGetFullServerUrl(baseUrl: String): String {
    fun String.addSlashIfMissing(): String {
        if (!endsWith("/")) return "$this/"
        return this
    }

    var url = addHttpsIfMissing(baseUrl)
    runCatching {
        var conn = URL(url).openConnection() as HttpURLConnection
        conn.getHeaderField("x-authlib-injector-api-location")?.let { ali ->
            val absoluteAli = URL(conn.url, ali)
            url = url.addSlashIfMissing()
            val absoluteUrl = absoluteAli.toString().addSlashIfMissing()
            if (url != absoluteUrl) {
                conn.disconnect()
                url = absoluteUrl
                conn = absoluteAli.openConnection() as HttpURLConnection
            }
        }

        return url.addSlashIfMissing()
    }.getOrElse { e ->
        Log.e("getFullServerUrl", "Failed to get full server url", e)
    }
    return baseUrl
}

/**
 * 修改自源代码：[HMCL Core: AuthlibInjectorServer.java](https://github.com/HMCL-dev/HMCL/blob/main/HMCLCore/src/main/java/org/jackhuang/hmcl/auth/authlibinjector/AuthlibInjectorServer.java#L90-#L96)
 * <br>原项目版权归原作者所有，遵循GPL v3协议
 */
private fun addHttpsIfMissing(baseUrl: String): String {
    return if (!baseUrl.startsWith("http://", true) && !baseUrl.startsWith("https://")) {
        "https://$baseUrl".lowercase(Locale.ROOT)
    } else baseUrl.lowercase(Locale.ROOT)
}
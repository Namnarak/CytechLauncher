package com.movtery.zalithlauncher.game.account

import android.util.Log
import com.movtery.zalithlauncher.game.skin.SkinFileDownloader
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.utils.GSON
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.UUID

class Account {
    var accessToken: String = "0" // access token
    var clientToken: String = "0" // clientID: refresh and invalidate
    var profileId: String = "00000000-0000-0000-0000-000000000000" // profile UUID, for obtaining skin
    var username: String = "Steve"
    var refreshToken: String = "0"
    var xuid: String? = null
    var otherBaseUrl: String? = null
    var otherAccount: String? = null
    var otherPassword: String? = null
    var accountType: String? = null
    val uniqueUUID: String = UUID.randomUUID().toString().lowercase()

    fun save() {
        val accountFile = File(PathManager.DIR_ACCOUNT, uniqueUUID)
        accountFile.writeText(GSON.toJson(this))
    }

    /**
     * 下载并更新账号的皮肤文件
     */
    fun downloadSkin() {
        when {
            accountType == AccountType.MICROSOFT.tag -> updateSkin("https://sessionserver.mojang.com")
            otherBaseUrl != null -> updateSkin(otherBaseUrl!!.removeSuffix("/") + "/sessionserver/")
            else -> {}
        }
    }

    private fun updateSkin(url: String) {
        val skinFile = File(PathManager.DIR_ACCOUNT_SKIN, "$uniqueUUID.png")
        if (skinFile.exists()) FileUtils.deleteQuietly(skinFile) //清除一次皮肤文件

        runCatching {
            SkinFileDownloader().yggdrasil(url, skinFile, profileId)
            Log.i("Account", "Update skin success")
        }.onFailure { e ->
            Log.e("Account", "Could not update skin", e)
        }
    }
}
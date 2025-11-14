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

package com.movtery.zalithlauncher.game.account

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.movtery.zalithlauncher.game.account.wardrobe.SkinFileDownloader
import com.movtery.zalithlauncher.game.account.wardrobe.SkinModelType
import com.movtery.zalithlauncher.game.account.wardrobe.getLocalUUIDWithSkinModel
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.utils.logging.Logger.lError
import com.movtery.zalithlauncher.utils.logging.Logger.lInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.UUID

@Entity(tableName = "accounts")
data class Account(
    /**
     * 唯一 UUID，标识该账号
     */
    @PrimaryKey
    val uniqueUUID: String = UUID.randomUUID().toString().lowercase(),
    var accessToken: String = "0",
    var clientToken: String = "0",
    var username: String = "Steve",
    var profileId: String = getLocalUUIDWithSkinModel(username, SkinModelType.NONE),
    var refreshToken: String = "0",
    var xUid: String? = null,
    var otherBaseUrl: String? = null,
    var otherAccount: String? = null,
    var otherPassword: String? = null,
    var accountType: String? = null,
    var skinModelType: SkinModelType = SkinModelType.NONE
) {
    val hasSkinFile: Boolean
        get() = getSkinFile().exists()

    fun getSkinFile() = File(PathManager.DIR_ACCOUNT_SKIN, "$uniqueUUID.png")

    /**
     * 下载并更新账号的皮肤文件
     */
    suspend fun downloadSkin() = withContext(Dispatchers.IO) {
        when {
            isMicrosoftAccount() -> updateSkin("https://sessionserver.mojang.com")
            isAuthServerAccount() -> updateSkin(otherBaseUrl!!.removeSuffix("/") + "/sessionserver/")
            else -> {}
        }
    }

    private suspend fun updateSkin(url: String) {
        val skinFile = getSkinFile()
        if (skinFile.exists()) FileUtils.deleteQuietly(skinFile) //清除一次皮肤文件

        runCatching {
            SkinFileDownloader().yggdrasil(url, skinFile, profileId) { modelType ->
                this.skinModelType = modelType
            }
            lInfo("Update skin success")
        }.onFailure { e ->
            lError("Could not update skin", e)
        }
        //刷新头像
        AccountsManager.refreshAccountsAvatar()
    }
}
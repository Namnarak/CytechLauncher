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

package com.movtery.zalithlauncher.game.account.wardrobe

import com.google.gson.JsonObject
import com.movtery.zalithlauncher.path.createOkHttpClient
import com.movtery.zalithlauncher.utils.GSON
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.logging.Logger.lWarning
import com.movtery.zalithlauncher.utils.network.fetchStringFromUrl
import com.movtery.zalithlauncher.utils.string.decodeBase64
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class SkinFileDownloader {
    private val mClient = createOkHttpClient()

    /**
     * 尝试下载yggdrasil皮肤
     */
    @Throws(Exception::class)
    suspend fun yggdrasil(
        url: String,
        skinFile: File,
        uuid: String,
        changeSkinModel: (SkinModelType) -> Unit
    ) {
        val profileJson = fetchStringFromUrl("${url.removeSuffix("/")}/session/minecraft/profile/$uuid")
        val profileObject = GSON.fromJson(profileJson, JsonObject::class.java)
        val properties = profileObject.get("properties").asJsonArray
        val rawValue = properties.get(0).asJsonObject.get("value").asString

        val value = decodeBase64(rawValue)

        val valueObject = GSON.fromJson(value, JsonObject::class.java)
        val skinObject = valueObject.get("textures").asJsonObject.get("SKIN").asJsonObject
        val skinUrl = skinObject.get("url").asString

        val skinModelType = runCatching {
            skinObject.takeIf {
                it.has("metadata")
            }?.get("metadata")?.let {
                //仅在玩家模型为细臂时，才会存在metadata字段，否则为粗臂
                //Wiki：https://zh.minecraft.wiki/w/Mojang_API#%E8%8E%B7%E5%8F%96%E7%8E%A9%E5%AE%B6%E7%9A%84%E7%9A%AE%E8%82%A4%E5%92%8C%E6%8A%AB%E9%A3%8E
                SkinModelType.ALEX
            } ?: SkinModelType.STEVE
        }.getOrElse {
            lWarning("Can not get skin model type.")
            SkinModelType.NONE
        }

        downloadSkin(skinUrl, skinFile)
        changeSkinModel(skinModelType)
    }

    private fun downloadSkin(url: String, skinFile: File) {
        skinFile.parentFile?.apply {
            if (!exists()) mkdirs()
        }

        val request = Request.Builder()
            .url(url)
            .build()

        mClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("Unexpected code $response")
            }

            try {
                response.body.byteStream().use { inputStream ->
                    FileOutputStream(skinFile).use { outputStream ->
                        val buffer = ByteArray(4096)
                        var bytesRead: Int
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                        }
                    }
                }
            } catch (e: Exception) {
                Logger.lError("Failed to download skin file", e)
            }
        }
    }
}
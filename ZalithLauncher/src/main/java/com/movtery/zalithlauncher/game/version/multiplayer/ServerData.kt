package com.movtery.zalithlauncher.game.version.multiplayer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.github.steveice10.opennbt.tag.builtin.ByteTag
import com.github.steveice10.opennbt.tag.builtin.CompoundTag
import com.github.steveice10.opennbt.tag.builtin.StringTag
import com.movtery.zalithlauncher.utils.logging.Logger.lWarning
import com.movtery.zalithlauncher.utils.network.ServerAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Base64

/**
 * Minecraft 服务器主要信息数据类
 * @param name 由玩家定义的服务器名称
 * @param originIp 玩家填写的原始服务器ip地址
 * @param texturePackStatus 服务器的纹理包启用状态
 * @param acceptedCodeOfConduct 是否已接受服务器代码条款
 * @param icon 服务器保存在本地的图标
 */
data class ServerData(
    var name: String,
    var originIp: String,
    val texturePackStatus: TexturePackStatus = TexturePackStatus.PROMPT,
    val acceptedCodeOfConduct: Boolean? = null,
    val icon: ByteArray? = null
) {
    enum class TexturePackStatus(
        val storageCode: Int?
    ) {
        ENABLED(1),
        DISABLED(0),
        /** 提示用户启用纹理包 */
        PROMPT(null)
    }

    sealed interface Operation {
        data object Loading : Operation
        /** 服务器加载成功 */
        data class Loaded(val result: ServerPingResult) : Operation
        /** 无法连接至服务器 */
        data object Failed : Operation
    }

    var uiIcon by mutableStateOf<Any?>(icon)
        private set

    var operation by mutableStateOf<Operation>(Operation.Loading)
        private set

    suspend fun load() {
        withContext(Dispatchers.Main) {
            operation = Operation.Loading
        }

        runCatching {
            val ip = ServerAddress.parse(originIp)

            val resolvedAddress = ip.resolve()
            val result = pingServer(resolvedAddress)

            withContext(Dispatchers.Main) {
                uiIcon = result.status.favicon ?: icon
                operation = Operation.Loaded(result)
            }
        }.onFailure {
            lWarning("Unable to load/connect to server: $originIp", it)
            withContext(Dispatchers.Main) {
                operation = Operation.Failed
            }
        }
    }

    fun save(): CompoundTag {
        return CompoundTag("").apply {
            put(StringTag("name", this@ServerData.name))
            put(StringTag("ip", this@ServerData.originIp))

            this@ServerData.icon?.let { bytes ->
                Base64.getEncoder().encodeToString(bytes)
            }?.let { iconString ->
                put(StringTag("icon", iconString))
            }

            this@ServerData.texturePackStatus.storageCode?.let { acceptTextures ->
                put(ByteTag("acceptTextures", acceptTextures.toByte()))
            }

            if (this@ServerData.acceptedCodeOfConduct == true) {
                put(ByteTag("acceptedCodeOfConduct", 1))
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ServerData

        if (acceptedCodeOfConduct != other.acceptedCodeOfConduct) return false
        if (name != other.name) return false
        if (originIp != other.originIp) return false
        if (texturePackStatus != other.texturePackStatus) return false
        if (!icon.contentEquals(other.icon)) return false
        if (uiIcon != other.uiIcon) return false
        if (operation != other.operation) return false

        return true
    }

    override fun hashCode(): Int {
        var result = acceptedCodeOfConduct?.hashCode() ?: 0
        result = 31 * result + name.hashCode()
        result = 31 * result + originIp.hashCode()
        result = 31 * result + texturePackStatus.hashCode()
        result = 31 * result + (icon?.contentHashCode() ?: 0)
        result = 31 * result + (uiIcon?.hashCode() ?: 0)
        result = 31 * result + operation.hashCode()
        return result
    }
}
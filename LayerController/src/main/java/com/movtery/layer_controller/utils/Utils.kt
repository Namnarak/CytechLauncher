package com.movtery.layer_controller.utils

import androidx.compose.ui.graphics.Color
import com.movtery.layer_controller.layout.ControlLayout
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import java.io.File
import java.util.Base64
import java.util.UUID

internal val layoutJson = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    serializersModule = SerializersModule {
        contextual(Color::class, ColorSerializer)
    }
}

internal fun randomUUID(length: Int = 12): String =
    UUID.randomUUID()
        .toString()
        .replace("-", "")
        .take(length)

internal fun getAButtonUUID() = randomUUID(18)

/**
 * 生成一个随机的文件名
 * @param characters 字符数
 */
fun newRandomFileName(characters: Int = 8): String {
    val uuid = UUID.randomUUID()
    val bytes = ByteArray(16).apply {
        System.arraycopy(
            longToBytes(uuid.mostSignificantBits), 0, this, 0, 8
        )
        System.arraycopy(
            longToBytes(uuid.leastSignificantBits), 0, this, 8, 8
        )
    }

    val encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    return encoded.take(characters)
}

private fun longToBytes(long: Long): ByteArray {
    val buffer = ByteArray(8)
    for (i in 0 until 8) {
        buffer[i] = (long shr (8 * (7 - i))).toByte()
    }
    return buffer
}

fun ControlLayout.saveToFile(file: File) {
    val jsonString = layoutJson.encodeToString(this)
    file.writeText(jsonString)
}
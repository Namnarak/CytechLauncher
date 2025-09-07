package com.movtery.layer_controller.data.lang

import com.movtery.layer_controller.data.lang.LocalizedString.Companion.check
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Locale

/**
 * 多语言集合，可根据当前系统语言匹配指定的字符串
 * @param default 默认字符串，如果未找到匹配项，则使用它
 */
@Serializable
data class TranslatableString(
    @SerialName("default")
    val default: String,
    @SerialName("matchQueue")
    val matchQueue: List<LocalizedString>
) {
    fun translate(locale: Locale = Locale.getDefault()): String {
        matchQueue.forEach { ls ->
            val value = ls.check(locale)
            if (value != null) return value
        }
        return default
    }

    companion object {
        public val Empty = TranslatableString("", emptyList())

        fun create(default: String, vararg matchQueue: LocalizedString): TranslatableString {
            return TranslatableString(default = default, matchQueue = matchQueue.toList())
        }
    }
}
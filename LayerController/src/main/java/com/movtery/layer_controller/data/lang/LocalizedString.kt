package com.movtery.layer_controller.data.lang

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Locale

/**
 * 本地化显示字符串
 * @param languageTag 语言标签
 */
@Serializable
data class LocalizedString(
    @SerialName("language_tag")
    val languageTag: String,
    @SerialName("value")
    val value: String
) {
    companion object {
        public val Empty = LocalizedString(languageTag = "", value = "")

        /**
         * 尝试检查语言是否匹配
         */
        fun LocalizedString.check(
            locale: Locale = Locale.getDefault()
        ): String? = value.takeIf {
            locale.toLanguageTag() == languageTag
        }
    }
}
package com.movtery.layer_controller.utils.lang

import com.google.gson.annotations.SerializedName
import java.util.Locale

/**
 * 本地化显示字符串
 * @param languageTag 语言标签
 */
class LocalizedString(
    @SerializedName("language_tag")
    val languageTag: String,
    @SerializedName("value")
    val value: String
) {
    companion object {
        /**
         * 尝试检查语言是否匹配
         */
        fun LocalizedString.check(
            locale: Locale = Locale.getDefault()
        ): String? =
            value.takeIf { locale.toLanguageTag() == languageTag }
    }
}
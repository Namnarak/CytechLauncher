package com.movtery.layer_controller.observable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.movtery.layer_controller.utils.lang.LocalizedString
import java.util.Locale

class ObservableLocalizedString(val string: LocalizedString): Packable<LocalizedString> {
    var languageTag by mutableStateOf(string.languageTag)
    var value by mutableStateOf(string.value)

    override fun pack(): LocalizedString {
        return LocalizedString(
            languageTag = languageTag,
            value = value
        )
    }

    companion object {
        /**
         * 尝试检查语言是否匹配
         */
        fun ObservableLocalizedString.check(
            locale: Locale = Locale.getDefault()
        ): String? = value.takeIf {
            locale.toLanguageTag() == languageTag
        }
    }
}
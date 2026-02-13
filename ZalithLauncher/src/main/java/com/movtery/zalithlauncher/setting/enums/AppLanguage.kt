package com.movtery.zalithlauncher.setting.enums

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.movtery.zalithlauncher.R

enum class AppLanguage(
    val tag: String,
    @param:StringRes val textRes: Int
) {
    //按语言代码字母顺序排序
    FOLLOW_SYSTEM("", R.string.generic_follow_system),
    ENGLISH("en", R.string.language_english),
    GERMAN("de", R.string.language_german),
    HUNGARIAN("hu", R.string.language_hungarian),
    INDONESIAN("id", R.string.language_indonesian),
    ITALIAN("it", R.string.language_italian),
    JAPANESE("ja", R.string.language_japanese),
    PORTUGUESE("pt", R.string.language_portuguese),
    BRAZILIAN_PORTUGUESE("pt-BR", R.string.language_brazilian_portuguese),
    RUSSIAN("ru", R.string.language_russian),
    SPANISH("es", R.string.language_spanish),
    SIMPLIFIED_CHINESE("zh-CN", R.string.language_simplified_chinese),
    TRADITIONAL_CHINESE("zh-TW", R.string.language_traditional_chinese),
    ARABIC("ar", R.string.language_arabic),
    TURKISH("tr", R.string.language_turkish),
    UYGHUR("ug", R.string.language_uyghur),
    VIETNAMESE("vi", R.string.language_vietnamese),
}

fun applyLanguage(language: AppLanguage) {
    val appLocale = if (language != AppLanguage.FOLLOW_SYSTEM) {
        LocaleListCompat.forLanguageTags(language.tag)
    } else {
        LocaleListCompat.getEmptyLocaleList()
    }
    AppCompatDelegate.setApplicationLocales(appLocale)
}

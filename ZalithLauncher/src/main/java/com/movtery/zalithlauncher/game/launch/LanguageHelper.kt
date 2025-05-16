package com.movtery.zalithlauncher.game.launch

import com.movtery.zalithlauncher.utils.getSystemLanguage
import com.movtery.zalithlauncher.utils.string.isLowerOrEqualTo
import java.util.regex.Pattern

private val SNAPSHOT_REGEX: Pattern = Pattern.compile("^\\d+[a-zA-Z]\\d+[a-zA-Z]$")

private fun isOlderVersionRelease(versionName: String): Boolean {
    return versionName.isLowerOrEqualTo("1.10.2")
}

private fun isOlderVersionSnapshot(versionName: String): Boolean {
    return versionName.isLowerOrEqualTo("16w32a")
}

private fun getOlderLanguage(lang: String): String {
    val underscoreIndex = lang.indexOf('_')
    return if (underscoreIndex != -1) {
        //只将下划线后面的字符转换为大写
        val builder = StringBuilder(lang.substring(0, underscoreIndex + 1))
        builder.append(lang.substring(underscoreIndex + 1).uppercase())
        builder.toString()
    } else lang
}

private fun getLanguage(versionId: String): String {
    val lang = getSystemLanguage()
    return when {
        versionId.contains('.') -> {
            if (isOlderVersionRelease(versionId)) getOlderLanguage(lang) // 1.10 -
            else lang
        }
        SNAPSHOT_REGEX.matcher(versionId).matches() -> { // 快照版本 "24w09a" "16w20a"
            if (isOlderVersionSnapshot(versionId)) getOlderLanguage(lang)
            else lang
        }
        else -> lang
    }
}

fun MCOptions.loadLanguage(versionId: String) {
    if (!containsKey("lang")) {
        val lang = getLanguage(versionId)
        set("lang", lang)
    }
}
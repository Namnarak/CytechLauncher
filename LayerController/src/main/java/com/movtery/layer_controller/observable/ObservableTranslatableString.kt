package com.movtery.layer_controller.observable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.movtery.layer_controller.data.lang.EmptyLocalizedString
import com.movtery.layer_controller.data.lang.LocalizedString
import com.movtery.layer_controller.data.lang.TranslatableString
import java.util.Locale

class ObservableTranslatableString(private val text: TranslatableString): Packable<TranslatableString> {
    var default by mutableStateOf(text.default)
    var matchQueue = mutableStateListOf<ObservableLocalizedString>()
        .apply { addAll(getMatchQueues()) }
        private set

    private fun getMatchQueues() = text.matchQueue.map { ObservableLocalizedString(it) }

    /**
     * 重置状态
     */
    fun reset() {
        default = text.default
        matchQueue.clear()
        matchQueue.addAll(getMatchQueues())
    }

    fun translate(locale: Locale = Locale.getDefault()): String {
        matchQueue.forEach { ls ->
            val value = ls.check(locale)
            if (value != null) return value
        }
        return default
    }

    /**
     * 移除可翻译的字符串
     */
    fun deleteLocalizedString(string: ObservableLocalizedString) {
        matchQueue.removeIf {
            string.languageTag == it.languageTag && string.value == it.value
        }
    }

    /**
     * 添加可翻译的字符串
     */
    fun addLocalizedString(string: LocalizedString = EmptyLocalizedString) {
        if (matchQueue.any { it.languageTag == string.languageTag && it.value == string.value }) return
        matchQueue.add(ObservableLocalizedString(string))
    }

    override fun pack(): TranslatableString {
        return TranslatableString(
            default = default,
            matchQueue = matchQueue.mapNotNull { it.pack() }
        )
    }
}
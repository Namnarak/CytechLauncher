package com.movtery.layer_controller.observable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.movtery.layer_controller.layout.ControlLayout

class ObservableControlInfo(private val info: ControlLayout.Info) : Packable<ControlLayout.Info> {
    val name = ObservableTranslatableString(info.name)
    val author = ObservableTranslatableString(info.author)
    val description = ObservableTranslatableString(info.description)

    var versionCode by mutableIntStateOf(info.versionCode)
    var versionName by mutableStateOf(info.versionName)

    /**
     * 重置版本名称
     */
    fun resetVersionName() {
        versionName = info.versionName
    }

    override fun pack(): ControlLayout.Info {
        return ControlLayout.Info(
            name = name.pack(),
            author = author.pack(),
            description = description.pack(),
            versionCode = versionCode,
            versionName = versionName
        )
    }
}
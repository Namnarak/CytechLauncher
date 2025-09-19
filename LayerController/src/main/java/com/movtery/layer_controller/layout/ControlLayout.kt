package com.movtery.layer_controller.layout

import com.movtery.layer_controller.EDITOR_VERSION
import com.movtery.layer_controller.data.ButtonStyle
import com.movtery.layer_controller.data.lang.EmptyTranslatableString
import com.movtery.layer_controller.data.lang.TranslatableString
import com.movtery.layer_controller.layout.ControlLayout.Info
import com.movtery.layer_controller.updateLayoutToNew
import com.movtery.layer_controller.utils.layoutJson
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.io.IOException

/**
 * 描述一个控制布局结构
 * @param info 控制布局基本信息
 * @param layers 控制层级列表
 * @param editorVersion 使用编辑器版本
 */
@Serializable
data class ControlLayout(
    @SerialName("info")
    val info: Info,
    @SerialName("layers")
    val layers: List<ControlLayer>,
    @SerialName("styles")
    val styles: List<ButtonStyle>,
    @SerialName("editorVersion")
    val editorVersion: Int
) {
    @Serializable
    data class Info(
        @SerialName("name")
        val name: TranslatableString,
        @SerialName("author")
        val author: TranslatableString,
        @SerialName("description")
        val description: TranslatableString,
        @SerialName("versionCode")
        val versionCode: Int,
        @SerialName("versionName")
        val versionName: String
    )
}

public val EmptyLayoutInfo = Info(
    name = EmptyTranslatableString,
    author = EmptyTranslatableString,
    description = EmptyTranslatableString,
    versionCode = 0,
    versionName = ""
)

public val EmptyControlLayout = ControlLayout(
    editorVersion = EDITOR_VERSION,
    info = EmptyLayoutInfo,
    layers = emptyList(),
    styles = emptyList()
)

/**
 * 从文件加载控制布局配置（检查版本号，大于编辑器版本则抛出`IllegalArgumentException`）
 */
public fun loadLayoutFromFile(file: File): ControlLayout {
    val jsonString = file.readText()
    return loadLayoutFromString(jsonString)
}

public fun loadLayoutFromString(jsonString: String): ControlLayout {
    val jsonObject = layoutJson.decodeFromString<JsonObject>(jsonString)
    if (jsonObject["editorVersion"] == null) throw IOException("The file does not contain the key \"editorVersion\".")
    val version = jsonObject["editorVersion"]!!.jsonPrimitive.int
    if (version <= EDITOR_VERSION) {
        var layout = layoutJson.decodeFromString<ControlLayout>(jsonString)
        if (version < EDITOR_VERSION) layout = updateLayoutToNew(layout)
        return layout
    } else {
        throw IllegalArgumentException("Control layout versions are not supported!")
    }
}

/**
 * 从文件加载控制布局配置（不检查版本号）
 */
public fun loadLayoutFromFileUncheck(file: File): ControlLayout {
    val jsonString = file.readText()
    return layoutJson.decodeFromString<ControlLayout>(jsonString)
}
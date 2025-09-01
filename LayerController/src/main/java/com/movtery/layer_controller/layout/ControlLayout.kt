package com.movtery.layer_controller.layout

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import com.movtery.layer_controller.EDITOR_VERSION
import com.movtery.layer_controller.data.ButtonStyle
import com.movtery.layer_controller.utils.CheckNotNull
import com.movtery.layer_controller.utils.lang.TranslatableString
import com.movtery.layer_controller.utils.layoutGson
import java.io.File

/**
 * 描述一个控制布局结构
 * @param info 控制布局基本信息
 * @param layers 控制层级列表
 * @param editorVersion 使用编辑器版本
 */
data class ControlLayout(
    @SerializedName("info")
    val info: Info = Info.Empty,
    @SerializedName("layers")
    val layers: List<ControlLayer> = emptyList(),
    @SerializedName("styles")
    val styles: List<ButtonStyle> = emptyList(),
    @SerializedName("editorVersion")
    val editorVersion: Int
) : CheckNotNull {
    data class Info(
        @SerializedName("name")
        val name: TranslatableString,
        @SerializedName("author")
        val author: TranslatableString,
        @SerializedName("description")
        val description: TranslatableString,
        @SerializedName("versionCode")
        val versionCode: Int,
        @SerializedName("versionName")
        val versionName: String
    ) : CheckNotNull {
        companion object {
            public val Empty = Info(
                name = TranslatableString.Empty,
                author = TranslatableString.Empty,
                description = TranslatableString.Empty,
                versionCode = 0,
                versionName = ""
            )
        }

        override fun checkNotNull() {
            val key = when {
                name == null -> "name"
                author == null -> "author"
                description == null -> "description"
                versionCode == null -> "versionCode"
                versionName == null -> "versionName"
                else -> null
            }
            if (key != null) throw NullPointerException("The key \"$key\" should not be null.")
        }
    }

    override fun checkNotNull() {
        val key = when {
            info == null -> "info"
            layers == null -> "layers"
            styles == null -> "styles"
            editorVersion == null -> "editorVersion"
            else -> {
                info.checkNotNull()
                null
            }
        }
        if (key != null) throw NullPointerException("The key \"$key\" should not be null.")
    }

    companion object {
        public val Empty = ControlLayout(editorVersion = EDITOR_VERSION)

        /**
         * 从文件加载控制布局配置（检查版本号，大于编辑器版本则抛出`IllegalArgumentException`）
         */
        public fun loadFromFile(file: File): ControlLayout {
            val jsonString = file.readText()
            val jsonObject = layoutGson.fromJson(jsonString, JsonObject::class.java)
            if (jsonObject.has("editorVersion") && jsonObject.get("editorVersion").asInt <= EDITOR_VERSION) {
                return layoutGson.fromJson(jsonObject, ControlLayout::class.java).also {
                    it.checkNotNull()
                }
            } else {
                throw IllegalArgumentException("Control layout versions are not supported!")
            }
        }

        /**
         * 从文件加载控制布局配置（不检查版本号）
         */
        public fun loadFromFileUncheck(file: File): ControlLayout {
            val jsonString = file.readText()
           return layoutGson.fromJson(jsonString, ControlLayout::class.java).also {
               it.checkNotNull()
           }
        }
    }
}
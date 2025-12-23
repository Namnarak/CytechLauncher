/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.upgrade

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 远端返回的最新版本的启动器的信息，用于与本地启动器版本进行检查并更新
 * @param code 最新启动器的版本号
 * @param version 最新启动器的版本名称
 * @param createdAt 发布时间
 * @param files 可下载安装包文件
 * @param defaultBody 默认更新日志，当 [bodies] 中没有匹配的语言日志时使用
 * @param bodies 针对不同语言的更新日志列表
 */
@Serializable
data class RemoteData(
    @SerialName("code")
    val code: Int,
    @SerialName("version")
    val version: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("files")
    val files: List<RemoteFile>,
    @SerialName("defaultBody")
    val defaultBody: RemoteBody,
    @SerialName("bodies")
    val bodies: List<RemoteBody>
) {
    /**
     * 最新版本的启动器的安装包文件
     * @param fileName 可直接展示的文件名称
     * @param uri 可直接在浏览器下载的链接
     * @param arch 该安装包的架构
     * @param size 该安装包文件的大小 (bytes)
     */
    @Serializable
    data class RemoteFile(
        @SerialName("file_name")
        val fileName: String,
        @SerialName("uri")
        val uri: String,
        @SerialName("arch")
        val arch: Arch,
        @SerialName("size")
        val size: Long = 0L
    ) {
        @Serializable
        enum class Arch {
            @SerialName("all")
            ALL,
            @SerialName("arm")
            ARM,
            @SerialName("arm64")
            ARM64,
            @SerialName("x86")
            X86,
            @SerialName("x86_64")
            X86_64
        }
    }

    /**
     * 最新版本的启动器的更新日志，按语言区分
     * @param language 语言标识
     * @param chunks 内容分区，每块包含标题和多行文本
     */
    @Serializable
    data class RemoteBody(
        @SerialName("language")
        val language: String,
        @SerialName("chunks")
        val chunks: List<TextChunk>
    ) {
        /**
         * @param title 这个区块的标题
         * @param texts 多行描述
         */
        @Serializable
        data class TextChunk(
            @SerialName("title")
            val title: String,
            @SerialName("texts")
            val texts: List<Text>
        ) {
            /**
             * 更新日志的单行描述
             * @param text 描述的具体文字内容
             * @param indentation 缩进次数
             * @param links 末尾追加的所有链接
             */
            @Serializable
            data class Text(
                @SerialName("text")
                val text: String,
                @SerialName("indentation")
                val indentation: Int = 0,
                @SerialName("links")
                val links: List<Link> = emptyList()
            ) {
                /**
                 * 内嵌链接，用于在单行描述的末尾追加链接
                 * @param text 链接的可显示文本
                 * @param link 实际的链接内容
                 */
                @Serializable
                data class Link(
                    @SerialName("text")
                    val text: String,
                    @SerialName("link")
                    val link: String
                )
            }
        }
    }
}

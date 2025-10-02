package com.movtery.zalithlauncher.game.download.assets.platform

/**
 * 平台的搜索结果单项数据实现
 */
interface PlatformSearchData {
    /**
     * 所属平台
     */
    fun platform(): Platform

    /**
     * 项目Id
     */
    fun platformId(): String

    /**
     * 在平台上的标题
     */
    fun platformTitle(): String

    /**
     * 在平台上的描述
     */
    fun platformDescription(): String

    /**
     * 在平台上的主要作者
     */
    fun platformAuthor(): String

    /**
     * 图标链接
     */
    fun platformIconUrl(): String?

    /**
     * 在平台上的下载数量
     */
    fun platformDownloadCount(): Long

    /**
     * 在平台上的收藏数量（Modrinth）
     */
    fun platformFollows(): Long?

    /**
     * 在平台上标注的模组加载器信息
     */
    fun platformModLoaders(): List<PlatformDisplayLabel>?

    /**
     * 在平台上标注的类别信息
     */
    fun platformCategories(classes: PlatformClasses): List<PlatformFilterCode>?
}
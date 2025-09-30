package com.movtery.zalithlauncher.game.download.assets.platform

/**
 * 平台项目实现
 */
interface PlatformProject {
    /**
     * 所属平台
     */
    fun platform(): Platform

    /**
     * 该项目在平台上的Id
     */
    fun platformId(): String

    /**
     * 该项目的类型
     */
    fun platformClasses(defaultClasses: PlatformClasses): PlatformClasses

    /**
     * 该项目的别名
     */
    fun platformSlug(): String

    /**
     * 该项目的图标链接
     */
    fun platformIconUrl(): String?

    /**
     * 该项目在平台上的标题名称
     */
    fun platformTitle(): String

    /**
     * 该项目在平台上的描述
     */
    fun platformSummary(): String?

    /**
     * 该项目在平台上的主要作者
     */
    fun platformAuthor(): String?

    /**
     * 该项目在平台上的总下载量
     */
    fun platformDownloadCount(): Long

    /**
     * 该项目的所有相关链接
     */
    fun platformUrls(defaultClasses: PlatformClasses): Urls

    /**
     * 该项目在平台上上传的所有截图
     */
    fun platformScreenshots(): List<Screenshot>

    /**
     * 资源项目各类外链
     * @param projectUrl 平台项目链接
     * @param sourceUrl 源代码仓库链接
     * @param issuesUrl 议题链接
     * @param wikiUrl wiki链接
     */
    class Urls(
        val projectUrl: String? = null,
        val sourceUrl: String? = null,
        val issuesUrl: String? = null,
        val wikiUrl: String? = null
    )

    /**
     * 屏幕截图
     * @param imageUrl 图片链接
     * @param title 截图标题
     * @param description 截图描述
     */
    class Screenshot(
        val imageUrl: String,
        val title: String? = null,
        val description: String? = null
    )
}

/**
 * 所有的链接是否都为null
 */
fun PlatformProject.Urls.isAllNull(): Boolean {
    return projectUrl == null &&
            sourceUrl == null &&
            issuesUrl == null &&
            wikiUrl   == null
}
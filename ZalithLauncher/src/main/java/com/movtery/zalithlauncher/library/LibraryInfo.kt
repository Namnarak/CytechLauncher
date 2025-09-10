package com.movtery.zalithlauncher.library

/**
 * 依赖库信息
 * @param name 库显示名称
 * @param copyrightInfo 库版权信息
 * @param license 库协议信息
 * @param webUrl 库项目链接
 */
data class LibraryInfo(
    val name: String,
    val copyrightInfo: String?,
    val license: License,
    val webUrl: String
)
package com.movtery.zalithlauncher.game.version.mod

import android.os.Parcelable
import com.movtery.zalithlauncher.game.download.assets.platform.ModLoaderDisplayLabel
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import kotlinx.parcelize.Parcelize

/**
 * 模组在平台上对应的文件
 * @param id 文件ID
 * @param platform 所属平台
 * @param datePublished 发布日期
 */
@Parcelize
class ModFile(
    val id: String,
    val projectId: String,
    val platform: Platform,
    val loaders: Array<ModLoaderDisplayLabel>,
    val datePublished: String
): Parcelable
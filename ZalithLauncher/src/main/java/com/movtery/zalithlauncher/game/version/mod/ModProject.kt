package com.movtery.zalithlauncher.game.version.mod

import android.os.Parcelable
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import kotlinx.parcelize.Parcelize

/**
 * 展示到模组管理页面的项目信息
 */
@Parcelize
class ModProject(
    val id: String,
    val platform: Platform,
    val iconUrl: String? = null,
    val title: String,
    val slug: String
): Parcelable
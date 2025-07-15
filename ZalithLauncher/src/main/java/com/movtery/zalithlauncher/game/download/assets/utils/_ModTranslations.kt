package com.movtery.zalithlauncher.game.download.assets.utils

import com.movtery.zalithlauncher.utils.isChinese

/**
 * 获取 mcmod 模组翻译标题，若当前环境非中文环境，则返回原始模组名称
 */
fun ModTranslations.McMod?.getMcmodTitle(originTitle: String): String {
    return this?.displayName?.takeIf { isChinese() } ?: originTitle
}
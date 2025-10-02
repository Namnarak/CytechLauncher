package com.movtery.zalithlauncher.game.download.assets.utils

import android.content.Context
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformProject
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeProject
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthSingleProject
import com.movtery.zalithlauncher.utils.isChinese
import com.movtery.zalithlauncher.utils.string.containsChinese
import com.movtery.zalithlauncher.utils.string.tokenize

/**
 * 根据平台获取模组翻译信息
 */
fun PlatformProject.getMcMod(
    classes: PlatformClasses
): ModTranslations.McMod? {
    val translations = classes.getTranslations()
    return when (this) {
        is ModrinthSingleProject -> translations.getModBySlugId(slug)
        is CurseForgeProject -> translations.getModBySlugId(data.slug)
        else -> error("Unknown project type: $this")
    }
}

/**
 * 获取 mcmod 模组翻译标题，若当前环境非中文环境，则返回原始模组名称
 */
fun ModTranslations.McMod?.getMcmodTitle(originTitle: String, context: Context? = null): String {
    return this?.displayName?.takeIf { isChinese(context) } ?: originTitle
}

/**
 * 修改自源代码：[HMCL Github](https://github.com/HMCL-dev/HMCL/blob/57018be/HMCL/src/main/java/org/jackhuang/hmcl/game/LocalizedRemoteModRepository.java#L45-L63)
 * 原项目版权归原作者所有，遵循GPL v3协议
 * @return `Boolean` 是否包含中文, `String` 英文混合关键词 (不包含中文时，原样返回)
 */
fun String.localizedModSearchKeywords(
    classes: PlatformClasses
): Pair<Boolean, Set<String>?> {
    if (!this.containsChinese()) return false to null
    val englishSearchFiltersSet: MutableSet<String> = HashSet(16)

    for ((count, mod) in classes.getTranslations()
        .searchMod(this).withIndex()
    ) {
        for (englishWord in tokenize(mod.subname.ifBlank { mod.name })) {
            if (englishSearchFiltersSet.contains(englishWord)) continue
            englishSearchFiltersSet.add(englishWord)
        }
        if (count >= 3) break
    }

    return true to englishSearchFiltersSet
}
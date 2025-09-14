package com.movtery.zalithlauncher.game.account.wardrobe

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.yggdrasil.PlayerProfile

/**
 * 空披风，可用来表示不选择、重置披风
 */
val EmptyCape = PlayerProfile.Cape("", "", "", "")

/**
 * 翻译披风名称
 */
@Composable
fun PlayerProfile.Cape.capeTranslatedName(): String {
    if (this == EmptyCape || id.isEmpty()) return stringResource(R.string.cape_name_none)

    val localeRes = when (alias) {
        "Migrator" -> R.string.cape_name_migrator
        "MapMaker" -> R.string.cape_name_mapmaker
        "Moderator" -> R.string.cape_name_moderator
        "Translator-Chinese" -> R.string.cape_name_translator_chinese
        "Translator" -> R.string.cape_name_translator
        "Cobalt" -> R.string.cape_name_cobalt
        "Vanilla" -> R.string.cape_name_vanilla
        "Minecon2011" -> R.string.cape_name_minecon2011
        "Minecon2012" -> R.string.cape_name_minecon2012
        "Minecon2013" -> R.string.cape_name_minecon2013
        "Minecon2015" -> R.string.cape_name_minecon2015
        "Minecon2016" -> R.string.cape_name_minecon2016
        "Cherry Blossom" -> R.string.cape_name_cherry_blossom
        "15th Anniversary" -> R.string.cape_name_15_th_anniversary
        "Purple Heart" -> R.string.cape_name_purple_heart
        "Follower's" -> R.string.cape_name_follower_s
        "MCC 15th Year" -> R.string.cape_name_mcc_15_th_year
        "Minecraft Experience" -> R.string.cape_name_minecraft_experience
        "Mojang Office" -> R.string.cape_name_mojang_office
        "Home" -> R.string.cape_name_home
        "Menace" -> R.string.cape_name_menace
        "Yearn" -> R.string.cape_name_yearn
        "Common" -> R.string.cape_name_common
        "Pan" -> R.string.cape_name_pan
        "Founder's" -> R.string.cape_name_founder_s
        else -> null
    }

    return localeRes?.let { stringResource(it) } ?: alias
}
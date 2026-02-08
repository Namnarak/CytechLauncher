package com.movtery.zalithlauncher.game.version.saves

import com.movtery.zalithlauncher.R

/**
 * @param levelCode 在 level.dat 中存储的值
 */
enum class GameMode(val levelCode: Int, val nameRes: Int) {
    /** 生存模式 */
    SURVIVAL(0, R.string.saves_manage_gamemode_survival),
    /** 创造模式 */
    CREATIVE(1, R.string.saves_manage_gamemode_creative),
    /** 冒险模式 */
    ADVENTURE(2, R.string.saves_manage_gamemode_adventure),
    /** 旁观模式 */
    SPECTATOR(3, R.string.saves_manage_gamemode_spectator)
}
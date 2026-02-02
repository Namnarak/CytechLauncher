package com.movtery.zalithlauncher.game.version.saves

import com.movtery.zalithlauncher.R

/**
 * @param levelCode 在 level.dat 中存储的值
 */
enum class Difficulty(val levelCode: Int, val nameRes: Int) {
    /** 和平 */
    PEACEFUL(0, R.string.saves_manage_difficulty_peaceful),
    /** 简单 */
    EASY(1, R.string.saves_manage_difficulty_easy),
    /** 普通 */
    NORMAL(2, R.string.saves_manage_difficulty_normal),
    /** 困难 */
    HARD(3, R.string.saves_manage_difficulty_hard)
}
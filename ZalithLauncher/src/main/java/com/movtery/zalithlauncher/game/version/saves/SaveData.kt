package com.movtery.zalithlauncher.game.version.saves

import java.io.File

/**
 * 存档解析后的信息类
 */
data class SaveData(
    /** 存档文件夹 */
    val saveFile: File,
// 性能、速度考虑，不再计算存档的大小
//    /** 提前计算好的存档大小 */
//    val saveSize: Long,
    /** 该存档是否有效 */
    val isValid: Boolean,
    /** 存档真正的名字 */
    val levelName: String? = null,
    /** 游戏的版本名称 */
    val levelMCVersion: String? = null,
    /** 上次保存此存档的时间戳 */
    val lastPlayed: Long? = null,
    /** 存档游戏模式 */
    val gameMode: GameMode? = null,
    /** 存档难度等级 */
    val difficulty: Difficulty? = null,
    /** 难度是否被锁定 */
    val difficultyLocked: Boolean? = null,
    /** 是否为极限模式 */
    val hardcoreMode: Boolean? = null,
    /** 存档是否启用命令(作弊) */
    val allowCommands: Boolean? = null,
    /** 世界种子 */
    val worldSeed: Long? = null
)
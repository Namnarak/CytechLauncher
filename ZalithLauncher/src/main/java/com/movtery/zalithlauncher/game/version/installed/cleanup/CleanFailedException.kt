package com.movtery.zalithlauncher.game.version.installed.cleanup

import java.io.File
import java.io.IOException

/**
 * 文件清理失败异常
 * @param files 无法清理的文件
 */
class CleanFailedException(
    val files: List<File>
) : IOException()
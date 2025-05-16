package com.movtery.zalithlauncher.game.download.game.fabric

import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.game.download.game.GameLibDownloader
import com.movtery.zalithlauncher.game.version.download.BaseMinecraftDownloader
import java.io.File

const val FABRIC_LIKE_COMPLETER_ID = "Completer.FabricLike"

fun getFabricLikeCompleterTask(
    downloader: BaseMinecraftDownloader,
    tempMinecraftDir: File,
    tempVersionJson: File,
): Task {
    return Task.runTask(
        id = FABRIC_LIKE_COMPLETER_ID,
        task = { task ->
            val libDownloader = GameLibDownloader(
                downloader = downloader,
                gameJson = tempVersionJson.readText()
            )
            //提交下载计划
            libDownloader.schedule(task, File(tempMinecraftDir, "libraries"))
            //补全游戏库
            libDownloader.download(task)
        }
    )
}
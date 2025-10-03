package com.movtery.zalithlauncher.game.version.mod.update

/**
 * 等待用户确认模组更新时，如果用户选择取消，则抛出[ModUpdateCancelledException]异常，终止任务
 */
class ModUpdateCancelledException : RuntimeException("Update cancelled by user")

/**
 * 当所有模组都是最新版本，无需更新时抛出[NoModUpdatesAvailableException]
 */
class NoModUpdatesAvailableException : RuntimeException("All mods are already up to date")
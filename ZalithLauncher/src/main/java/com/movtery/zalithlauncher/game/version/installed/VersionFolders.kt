package com.movtery.zalithlauncher.game.version.installed

enum class VersionFolders(val folderName: String) {
    /** 无目标文件夹 */
    NONE(""),
    /** 模组文件夹 */
    MOD("mods"),
    /** 资源包文件夹 */
    RESOURCE_PACK("resourcepacks"),
    /** 存档文件夹 */
    SAVES("saves"),
    /** 光影包文件夹 */
    SHADERS("shaderpacks")
}
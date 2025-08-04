package com.movtery.zalithlauncher.game.version.mod.reader

import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.version.mod.LocalMod
import com.movtery.zalithlauncher.game.version.mod.LocalMod.Companion.isDisabled
import com.movtery.zalithlauncher.game.version.mod.ModMetadataReader
import com.movtery.zalithlauncher.game.version.mod.meta.PackMcMeta
import com.movtery.zalithlauncher.utils.GSON
import java.io.File
import java.io.InputStreamReader
import java.util.zip.ZipFile

/**
 * [Reference HMCL](https://github.com/HMCL-dev/HMCL/blob/4650287/HMCLCore/src/main/java/org/jackhuang/hmcl/mod/modinfo/PackMcMeta.java)
 */
object PackMcMetadataReader : ModMetadataReader {
    override fun fromLocal(modFile: File): LocalMod {
        if (!modFile.exists()) throw IllegalArgumentException("File not found: ${modFile.path}")

        val fileSize = modFile.length()
        val rawName = getRawFileName(modFile)

        ZipFile(modFile).use { zip ->
            val entry = zip.getEntry("pack.mcmeta") ?:
            throw IllegalArgumentException("pack.mcmeta not found in resource pack")

            zip.getInputStream(entry).use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    val meta = GSON.fromJson(reader, PackMcMeta::class.java)

                    return LocalMod(
                        modFile = modFile,
                        fileSize = fileSize,
                        id = rawName,
                        loader = ModLoader.PACK,
                        name = rawName,
                        description = meta.pack.description.toPlainText(),
                        version = "",
                        authors = emptyList(),
                        icon = null,
                        notMod = false
                    )
                }
            }
        }
    }

    /**
     * 获取原始文件名，移除 .disabled 后缀
     */
    private fun getRawFileName(file: File): String {
        val fileName = file.name
        return if (file.isDisabled()) fileName.removeSuffix(".disabled")
        else fileName
    }
}

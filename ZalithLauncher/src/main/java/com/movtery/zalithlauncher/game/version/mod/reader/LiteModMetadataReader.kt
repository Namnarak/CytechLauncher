package com.movtery.zalithlauncher.game.version.mod.reader

import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.version.mod.LocalMod
import com.movtery.zalithlauncher.game.version.mod.ModMetadataReader
import com.movtery.zalithlauncher.game.version.mod.meta.LiteModMetadata
import com.movtery.zalithlauncher.utils.GSON
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.zip.ZipFile

/**
 * [Reference HMCL](https://github.com/HMCL-dev/HMCL/blob/242df8a/HMCLCore/src/main/java/org/jackhuang/hmcl/mod/modinfo/LiteModMetadata.java)
 */
object LiteModMetadataReader : ModMetadataReader {
    override fun fromLocal(modFile: File): LocalMod {
        ZipFile(modFile).use { zip ->
            val litemodEntry = zip.getEntry("litemod.json")
                ?: throw IOException("File $modFile is not a LiteLoader mod.")

            zip.getInputStream(litemodEntry).use { stream ->
                val metadata = InputStreamReader(stream, StandardCharsets.UTF_8).use { r ->
                    GSON.fromJson(r, LiteModMetadata::class.java)
                } ?: throw IOException("Mod $modFile `litemod.json` is malformed.")

                return LocalMod(
                    modFile = modFile,
                    fileSize = FileUtils.sizeOf(modFile),
                    id = metadata.name,
                    loader = ModLoader.LITE_LOADER,
                    name = metadata.name,
                    description = metadata.description,
                    version = metadata.version,
                    authors = parseAuthors(metadata.author),
                    icon = null //LiteLoader 通常没有图标
                )
            }
        }
    }

    private fun parseAuthors(author: String?): List<String> {
        return author?.split(',', ';', '&')?.map { it.trim() }?.filterNot { it.isBlank() }
            ?: emptyList()
    }
}
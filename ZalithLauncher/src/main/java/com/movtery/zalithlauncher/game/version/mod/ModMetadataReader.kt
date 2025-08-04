package com.movtery.zalithlauncher.game.version.mod

import com.movtery.zalithlauncher.game.version.mod.LocalMod.Companion.isDisabled
import com.movtery.zalithlauncher.game.version.mod.reader.FabricModMetadataReader
import com.movtery.zalithlauncher.game.version.mod.reader.ForgeNewModMetadataReader
import com.movtery.zalithlauncher.game.version.mod.reader.ForgeOldModMetadataReader
import com.movtery.zalithlauncher.game.version.mod.reader.LiteModMetadataReader
import com.movtery.zalithlauncher.game.version.mod.reader.PackMcMetadataReader
import com.movtery.zalithlauncher.game.version.mod.reader.QuiltModMetadataReader
import com.movtery.zalithlauncher.utils.logging.Logger.lWarning
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File

interface ModMetadataReader {
    fun fromLocal(modFile: File): LocalMod

    companion object {
        private val NORMAL_READERS = arrayOf(
            ForgeOldModMetadataReader,
            ForgeNewModMetadataReader,
            FabricModMetadataReader,
            QuiltModMetadataReader,
            PackMcMetadataReader
        )

        private val READERS = mapOf<String, Array<ModMetadataReader>>(
            "zip" to NORMAL_READERS,
            "jar" to NORMAL_READERS,
            "litemod" to arrayOf(
                LiteModMetadataReader
            )
        )

        suspend fun readAllMods(
            modsDir: File
        ) = withContext(Dispatchers.IO) {
            try {
                val mods = modsDir.listFiles()?.filter { !it.isDirectory }?.map { file ->
                    ensureActive()
                    try {
                        val extension = if (file.isDisabled()) {
                            File(file.nameWithoutExtension).extension
                        } else {
                            file.extension
                        }
                        READERS[extension]?.forEach { reader ->
                            try {
                                return@map reader.fromLocal(file)
                            } catch (_: Exception) {
                                //继续使用下一个解析器
                            }
                        } ?: lWarning("No matching file suffix, file = $file")
                        throw IllegalArgumentException("File $file is not a mod file.")
                    } catch (e: Exception) {
                        lWarning("Failed to read mod: $file", e)
                        return@map LocalMod.createNotMod(file)
                    }
                } ?: emptyList()
                return@withContext mods.sortedBy { it.name }
            } catch (_: CancellationException) {
                return@withContext emptyList()
            }
        }
    }
}
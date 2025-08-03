package com.movtery.zalithlauncher.game.version.mod.reader

import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.version.mod.LocalMod
import com.movtery.zalithlauncher.game.version.mod.ModMetadataReader
import com.movtery.zalithlauncher.game.version.mod.meta.QuiltModMetadata
import com.movtery.zalithlauncher.utils.GSON
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.util.zip.ZipFile

/**
 * [Reference HMCL](https://github.com/HMCL-dev/HMCL/blob/4650287/HMCLCore/src/main/java/org/jackhuang/hmcl/mod/modinfo/QuiltModMetadata.java)
 */
object QuiltModMetadataReader : ModMetadataReader {
    override fun fromLocal(modFile: File): LocalMod {
        ZipFile(modFile).use { zip ->
            val quiltModEntry = zip.getEntry("quilt.mod.json")
                ?: throw IOException("File $modFile is not a Quilt mod.")

            zip.getInputStream(quiltModEntry).bufferedReader().use { reader ->
                val metadata = GSON.fromJson(
                    reader.readText(),
                    QuiltModMetadata::class.java
                ) ?: throw JsonParseException("Json object cannot be null.")

                if (metadata.schemaVersion != 1) {
                    throw IOException("File $modFile is not a supported Quilt mod (schema version ${metadata.schemaVersion}).")
                }

                val quiltLoader = metadata.quiltLoader
                val contributors = parseContributors(quiltLoader.metadata.contributors)
                val icon = zip.tryGetIcon(quiltLoader.metadata.icon)

                return LocalMod(
                    modFile = modFile,
                    fileSize = FileUtils.sizeOf(modFile),
                    id = quiltLoader.id,
                    loader = ModLoader.QUILT,
                    name = quiltLoader.metadata.name,
                    description = quiltLoader.metadata.description,
                    version = quiltLoader.version,
                    authors = contributors,
                    icon = icon
                )
            }
        }
    }

    private fun parseContributors(contributors: JsonObject?): List<String> {
        if (contributors == null) return emptyList()

        return contributors.entrySet().map { (name, role) ->
            val roleText = role.asJsonPrimitive?.asString?.takeIf { it.isNotBlank() }
            if (roleText != null) "$name ($roleText)" else name
        }
    }
}
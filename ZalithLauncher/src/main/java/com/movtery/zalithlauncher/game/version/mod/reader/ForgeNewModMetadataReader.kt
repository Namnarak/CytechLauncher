package com.movtery.zalithlauncher.game.version.mod.reader

import com.moandjiezana.toml.Toml
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.version.mod.LocalMod
import com.movtery.zalithlauncher.game.version.mod.ModMetadataReader
import com.movtery.zalithlauncher.game.version.mod.meta.ForgeNewModMetadata
import com.movtery.zalithlauncher.utils.logging.Logger.lWarning
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.util.jar.Attributes
import java.util.jar.Manifest
import java.util.zip.ZipFile

/**
 * [Reference HMCL](https://github.com/HMCL-dev/HMCL/blob/4650287/HMCLCore/src/main/java/org/jackhuang/hmcl/mod/modinfo/ForgeNewModMetadata.java)
 */
object ForgeNewModMetadataReader : ModMetadataReader {
    private const val ACC_FORGE = 0x01
    private const val ACC_NEO_FORGED = 0x02

    override fun fromLocal(modFile: File): LocalMod {
        ZipFile(modFile).use { zip ->
            //尝试 Forge
            runCatching {
                readFromToml(
                    zip,
                    "META-INF/mods.toml",
                    ACC_FORGE or ACC_NEO_FORGED,
                    ModLoader.FORGE,
                    modFile
                )
            }.onSuccess { return it }

            //尝试 NeoForge
            runCatching {
                readFromToml(
                    zip,
                    "META-INF/neoforge.mods.toml",
                    ACC_NEO_FORGED,
                    ModLoader.NEOFORGE,
                    modFile
                )
            }.onSuccess { return it }

            throw IOException("File $modFile is not a Forge 1.13+ or NeoForge mod.")
        }
    }

    private fun readFromToml(
        zip: ZipFile,
        tomlPath: String,
        loaderACC: Int,
        defaultLoader: ModLoader,
        modFile: File
    ): LocalMod {
        val tomlEntry = zip.getEntry(tomlPath) ?: throw IOException("TOML file $tomlPath not found")

        zip.getInputStream(tomlEntry).bufferedReader().use { reader ->
            val toml = Toml().read(reader.readText())
            val metadata = toml.to(ForgeNewModMetadata::class.java)
                ?: throw IOException("Failed to parse TOML metadata")

            if (metadata.mods.isEmpty()) {
                throw IOException("Mod $modFile `$tomlPath` is malformed")
            }

            val mod = metadata.mods[0]
            val jarVersion = readVersion(zip, modFile)
            val resolvedVersion = mod.version.replace("\${file.jarVersion}", jarVersion ?: "")
            val loaderType = analyzeLoader(toml, mod.modId, loaderACC, defaultLoader)
            val icon = zip.tryGetIcon(metadata.logoFile)

            return LocalMod(
                modFile = modFile,
                fileSize = FileUtils.sizeOf(modFile),
                id = mod.modId,
                loader = loaderType,
                name = mod.displayName,
                description = mod.description,
                version = resolvedVersion,
                authors = parseAuthors(mod.authors),
                icon = icon
            )
        }
    }

    private fun readVersion(zip: ZipFile, modFile: File): String? {
        val manifestEntry = zip.getEntry("META-INF/MANIFEST.MF") ?: return null

        return try {
            zip.getInputStream(manifestEntry).use { stream ->
                Manifest(stream).mainAttributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION)
            }
        } catch (e: Exception) {
            lWarning("Failed to parse MANIFEST.MF in file $modFile", e)
            null
        }
    }

    private fun parseAuthors(authors: String?): List<String> {
        return authors?.split(',', ';', '&')?.map {
            it.trim()
        }?.filter {
            it.isNotBlank()
        } ?: emptyList()
    }

    /**
     * 分析模组加载器类型
     */
    private fun analyzeLoader(
        toml: Toml,
        modID: String,
        loaderACC: Int,
        defaultLoader: ModLoader
    ): ModLoader {
        @Suppress("UNCHECKED_CAST")
        val dependencies = toml.getList<Map<String, Any>>("dependencies.$modID")
            ?: toml.getList("dependencies") //兼容旧格式

        fun checkLoaderACC(current: Int, target: Int, res: ModLoader): ModLoader {
            return if (target and current != 0) res else throw IOException("Mismatched loader")
        }

        dependencies?.forEach { dependency ->
            when (dependency["modId"] as? String) {
                "forge" -> return checkLoaderACC(loaderACC, ACC_FORGE, ModLoader.FORGE)
                "neoforge" -> return checkLoaderACC(loaderACC, ACC_NEO_FORGED, ModLoader.NEOFORGE)
            }
        }

        return defaultLoader
    }
}
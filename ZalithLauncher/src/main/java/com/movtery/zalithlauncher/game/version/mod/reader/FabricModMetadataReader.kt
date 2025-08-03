package com.movtery.zalithlauncher.game.version.mod.reader

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.annotations.JsonAdapter
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.version.mod.LocalMod
import com.movtery.zalithlauncher.game.version.mod.ModMetadataReader
import com.movtery.zalithlauncher.utils.GSON
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.lang.reflect.Type
import java.util.zip.ZipFile

/**
 * [Reference HMCL](https://github.com/HMCL-dev/HMCL/blob/4650287/HMCLCore/src/main/java/org/jackhuang/hmcl/mod/modinfo/FabricModMetadata.java)
 */
object FabricModMetadataReader : ModMetadataReader {
    override fun fromLocal(modFile: File): LocalMod {
        ZipFile(modFile).use { zip ->
            val fabricModEntry = zip.getEntry("fabric.mod.json")
                ?: throw IOException("File $modFile is not a Fabric mod.")

            zip.getInputStream(fabricModEntry).bufferedReader().use { reader ->
                val metadata = GSON.fromJson(
                    reader.readText(),
                    FabricModMetadata::class.java
                ) ?: throw JsonParseException("Json object cannot be null.")

                val authors = parseAuthors(metadata.authors)
                val icon = zip.tryGetIcon(metadata.icon)

                return LocalMod(
                    modFile = modFile,
                    fileSize = FileUtils.sizeOf(modFile),
                    id = metadata.id,
                    loader = ModLoader.FABRIC,
                    name = metadata.name,
                    description = metadata.description ?: "",
                    version = metadata.version,
                    authors = authors,
                    icon = icon
                )
            }
        }
    }

    private fun parseAuthors(authors: List<FabricModAuthor>?): List<String> {
        return authors?.mapNotNull { it.name } ?: emptyList()
    }

    data class FabricModMetadata(
        val id: String,
        val name: String,
        val version: String,
        val description: String? = null,
        val icon: String? = null,
        val authors: List<FabricModAuthor>? = null,
        val contact: Map<String, String>? = null
    )

    @JsonAdapter(FabricModAuthorSerializer::class)
    data class FabricModAuthor(val name: String? = null)

    class FabricModAuthorSerializer : JsonSerializer<FabricModAuthor>,
        JsonDeserializer<FabricModAuthor> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): FabricModAuthor {
            return if (json.isJsonPrimitive) {
                FabricModAuthor(json.asString)
            } else {
                FabricModAuthor(json.asJsonObject.getAsJsonPrimitive("name")?.asString)
            }
        }

        override fun serialize(
            src: FabricModAuthor?,
            typeOfSrc: Type?,
            context: JsonSerializationContext?
        ): JsonElement {
            return src?.name?.let { JsonPrimitive(it) } ?: JsonNull.INSTANCE
        }
    }
}
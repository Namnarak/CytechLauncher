package com.movtery.zalithlauncher.game.addons.modloader.forgelike.neoforge.models

import com.movtery.zalithlauncher.game.addons.modloader.forgelike.neoforge.NeoForgeVersion
import kotlinx.serialization.Serializable

@Serializable
data class BMCLAPIMaven(
    val files: List<File>,
    val name: String = "",
    val type: String = "",
    @Transient
    val isLegacy: Boolean = false
): NeoForgeMergeableMaven<BMCLAPIMaven> {
    @Serializable
    data class File(
        val contentLength: Int? = null,
        val contentType: String? = null,
        val lastModifiedTime: Double? = null,
        val name: String,
        val type: String
    )

    override fun plus(maven: BMCLAPIMaven): List<NeoForgeVersion> {
        return this.files.mapNotNull { file ->
            val versionId = file.name
            if (file.type != "DIRECTORY" || versionId.contains("maven", true)) return@mapNotNull null
            if (isVersionInvalid(versionId)) return@mapNotNull null
            NeoForgeVersion(versionId, this.isLegacy)
        } + maven.files.mapNotNull { file ->
            val versionId = file.name
            if (file.type != "DIRECTORY" || versionId.contains("maven", true)) return@mapNotNull null
            if (isVersionInvalid(versionId)) return@mapNotNull null
            NeoForgeVersion(versionId, maven.isLegacy)
        }
    }
}
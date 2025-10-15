package com.movtery.zalithlauncher.game.addons.modloader.forgelike.neoforge.models

import com.movtery.zalithlauncher.game.addons.modloader.forgelike.neoforge.NeoForgeVersion
import kotlinx.serialization.Serializable

/**
 * NeoForged Maven
 */
@Serializable
data class NeoForgedMaven(
    val isSnapshot: Boolean,
    val versions: List<String>,
    @Transient
    val isLegacy: Boolean = false
): NeoForgeMergeableMaven<NeoForgedMaven> {

    override fun plus(maven: NeoForgedMaven): List<NeoForgeVersion> {
        return this.versions.mapNotNull { versionId ->
            if (isVersionInvalid(versionId)) return@mapNotNull null
            NeoForgeVersion(versionId, this.isLegacy)
        } + maven.versions.mapNotNull { versionId ->
            if (isVersionInvalid(versionId)) return@mapNotNull null
            NeoForgeVersion(versionId, maven.isLegacy)
        }
    }
}
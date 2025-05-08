package com.movtery.zalithlauncher.game.addons.modloader.forgelike

class Version private constructor(
    val major: Int,
    val minor: Int,
    val build: Int,
    val revision: Int
) : Comparable<Version> {
    companion object {
        fun parse(versionString: String): Version {
            val parts = versionString.split('.', '-').mapNotNull { it.toIntOrNull() }
            return Version(
                parts.getOrElse(0) { 0 },
                parts.getOrElse(1) { 0 },
                parts.getOrElse(2) { 0 },
                parts.getOrElse(3) { 0 }
            )
        }
    }

    override fun compareTo(other: Version): Int {
        return compareValuesBy(
            this, other,
            { it.major },
            { it.minor },
            { it.build },
            { it.revision }
        )
    }

    override fun toString(): String {
        return "$major.$minor.$build.$revision"
    }
}
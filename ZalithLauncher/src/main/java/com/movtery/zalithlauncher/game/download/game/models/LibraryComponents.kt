package com.movtery.zalithlauncher.game.download.game.models

data class LibraryComponents(
    val groupId: String,
    val artifactId: String,
    val version: String,
    val classifier: String? = null,
    val extension: String = "jar"
) {
    val descriptor: String by lazy {
        // groupId:artifactId:version:classifier@extension
        "$groupId:$artifactId:$version".let {
            if (classifier.isNullOrEmpty()) it
            else "$it:$classifier"
        }.let {
            if (extension != "jar") "$it@$extension"
            else it
        }
    }

    override fun toString(): String {
        return descriptor
    }
}
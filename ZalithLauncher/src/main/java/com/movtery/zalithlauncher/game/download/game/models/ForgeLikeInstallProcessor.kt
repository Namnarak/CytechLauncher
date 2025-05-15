package com.movtery.zalithlauncher.game.download.game.models

class ForgeLikeInstallProcessor(
    private val sides: List<String>?,
    private val jar: String,
    private val classpath: List<String>?,
    private val args: List<String>?,
    private val outputs: Map<String, String>?
) {
    fun isSide(side: String): Boolean {
        return sides == null || sides.contains(side)
    }

    fun getJar(): LibraryComponents {
        return fromDescriptor(this.jar)
    }

    fun getClasspath(): List<LibraryComponents> {
        return classpath?.map { fromDescriptor(it) } ?: emptyList()
    }

    fun getArgs(): List<String> {
        return args ?: emptyList()
    }

    fun getOutputs(): Map<String, String> {
        return outputs ?: emptyMap()
    }
}
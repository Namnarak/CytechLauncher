package com.movtery.zalithlauncher.game.renderer.renderers

import android.content.Context
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.renderer.RendererInterface

class GL4ESRenderer(private val context: Context) : RendererInterface {
    override fun getRendererId(): String = "opengles2"

    override fun getUniqueIdentifier(): String = "8b52d82d-8f6d-4d3a-a767-dc93f8b72fc7"

    override fun getRendererName(): String = context.getString(R.string.renderer_name_gl4es)

    override fun getRendererEnv(): Lazy<Map<String, String>> = lazy { emptyMap() }

    override fun getDlopenLibrary(): Lazy<List<String>> = lazy { emptyList() }

    override fun getRendererLibrary(): String = "libgl4es_114.so"
}
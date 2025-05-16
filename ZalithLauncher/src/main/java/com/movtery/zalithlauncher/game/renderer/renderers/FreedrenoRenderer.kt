package com.movtery.zalithlauncher.game.renderer.renderers

import android.content.Context
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.renderer.RendererInterface

class FreedrenoRenderer(private val context: Context) : RendererInterface {
    override fun getRendererId(): String = "gallium_freedreno"

    override fun getUniqueIdentifier(): String = "1ad7249f-5784-4f00-bc72-174b3578ee46"

    override fun getRendererName(): String = context.getString(R.string.renderer_name_freedreno)

    override fun getMaxMCVersion(): String = "1.21.4"

    override fun getRendererEnv(): Lazy<Map<String, String>> = lazy { emptyMap() }

    override fun getDlopenLibrary(): Lazy<List<String>> = lazy { emptyList() }

    override fun getRendererLibrary(): String = "libOSMesa_8.so"
}
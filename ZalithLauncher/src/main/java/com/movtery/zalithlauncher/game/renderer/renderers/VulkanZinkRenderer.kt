package com.movtery.zalithlauncher.game.renderer.renderers

import android.content.Context
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.renderer.RendererInterface

class VulkanZinkRenderer(private val context: Context) : RendererInterface {
    override fun getRendererId(): String = "vulkan_zink"

    override fun getUniqueIdentifier(): String = "0fa435e2-46df-45c9-906c-b29606aaef00"

    override fun getRendererName(): String = context.getString(R.string.renderer_name_vulkan_zink)

    override fun getRendererEnv(): Lazy<Map<String, String>> = lazy {
        mapOf(
            "MESA_GL_VERSION_OVERRIDE" to "4.6",
            "MESA_GLSL_VERSION_OVERRIDE" to "460"
        )
    }

    override fun getDlopenLibrary(): Lazy<List<String>> = lazy { emptyList() }

    override fun getRendererLibrary(): String = "libOSMesa_8.so"
}
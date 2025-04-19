package com.movtery.zalithlauncher.ui.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.Surface
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.lifecycleScope
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ZLApplication
import com.movtery.zalithlauncher.bridge.Logger
import com.movtery.zalithlauncher.bridge.ZLBridge
import com.movtery.zalithlauncher.game.keycodes.LwjglGlfwKeycode
import com.movtery.zalithlauncher.game.launch.GameLauncher
import com.movtery.zalithlauncher.game.launch.JarInfo
import com.movtery.zalithlauncher.game.launch.JvmLauncher
import com.movtery.zalithlauncher.game.launch.Launcher
import com.movtery.zalithlauncher.game.launch.handler.GameHandler
import com.movtery.zalithlauncher.game.launch.handler.AbstractHandler
import com.movtery.zalithlauncher.game.launch.handler.HandlerType
import com.movtery.zalithlauncher.game.launch.handler.JVMHandler
import com.movtery.zalithlauncher.game.multirt.RuntimesManager
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.getGameManifest
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.scaleFactor
import com.movtery.zalithlauncher.state.ColorThemeState
import com.movtery.zalithlauncher.state.LocalColorThemeState
import com.movtery.zalithlauncher.ui.base.BaseComponentActivity
import com.movtery.zalithlauncher.ui.theme.ZalithLauncherTheme
import com.movtery.zalithlauncher.utils.getDisplayFriendlyRes
import com.movtery.zalithlauncher.utils.getParcelableSafely
import org.lwjgl.glfw.CallbackBridge
import java.io.File
import java.io.IOException

class VMActivity : BaseComponentActivity(), SurfaceTextureListener {
    companion object {
        const val INTENT_RUN_GAME = "BUNDLE_RUN_GAME"
        const val INTENT_RUN_JAR = "INTENT_RUN_JAR"
        const val INTENT_VERSION = "INTENT_VERSION"
        const val INTENT_JAR_INFO = "INTENT_JAR_INFO"
        private var isRunning = false
    }

    private var mTextureView: TextureView? = null

    private lateinit var launcher: Launcher
    private lateinit var handler: AbstractHandler

    private var isRenderingStarted: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = intent.extras ?: throw IllegalStateException("Unknown VM launch state!")

        launcher = if (bundle.getBoolean(INTENT_RUN_GAME, false)) {
            val version: Version = bundle.getParcelableSafely(INTENT_VERSION, Version::class.java)
                ?: throw IllegalStateException("No launch version has been set.")
            val gameManifest = getGameManifest(version)
            CallbackBridge.nativeSetUseInputStackQueue(gameManifest.arguments != null)
            handler = GameHandler()
            GameLauncher(this, version, gameManifest.javaVersion?.majorVersion ?: 8)
        } else if (bundle.getBoolean(INTENT_RUN_JAR, false)) {
            val jarInfo: JarInfo = bundle.getParcelableSafely(INTENT_JAR_INFO, JarInfo::class.java)
                ?: throw IllegalStateException("No launch jar info has been set.")
            handler = JVMHandler()
            JvmLauncher(this, jarInfo) { finish() }
        } else {
            throw IllegalStateException("Unknown VM launch mode, or the launch mode was not set at all!")
        }

        window?.apply {
            setBackgroundDrawable(Color.BLACK.toDrawable())
            if (AllSettings.sustainedPerformance.getValue()) {
                setSustainedPerformanceMode(true)
            }
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) // 防止系统息屏
        }

        val logFile = File(PathManager.DIR_FILES_EXTERNAL, "${launcher.getLogName()}.log")
        if (!logFile.exists() && !logFile.createNewFile()) throw IOException("Failed to create a new log file")
        Logger.begin(logFile.absolutePath)

        setContent {
            val colorThemeState = remember { ColorThemeState() }

            CompositionLocalProvider(
                LocalColorThemeState provides colorThemeState
            ) {
                ZalithLauncherTheme {
                    Screen(content = handler.getComposableLayout())
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        handler.onResume()
        CallbackBridge.nativeSetWindowAttrib(LwjglGlfwKeycode.GLFW_HOVERED, 1)
    }

    override fun onPause() {
        super.onPause()
        handler.onPause()
        CallbackBridge.nativeSetWindowAttrib(LwjglGlfwKeycode.GLFW_HOVERED, 0)
    }

    override fun onStart() {
        super.onStart()
        CallbackBridge.nativeSetWindowAttrib(LwjglGlfwKeycode.GLFW_HOVERED, 1)
    }

    override fun onStop() {
        super.onStop()
        CallbackBridge.nativeSetWindowAttrib(LwjglGlfwKeycode.GLFW_HOVERED, 0)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        refreshDisplayMetrics()
        refreshSize()
    }

    override fun onPostResume() {
        super.onPostResume()
        refreshSize()
    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (handler.shouldIgnoreKeyEvent(event)) {
            return super.dispatchKeyEvent(event)
        }
        return true
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        if (isRunning) {
            ZLBridge.setupBridgeWindow(Surface(surface))
            return
        }
        isRunning = true

        handler.mIsSurfaceDestroyed = false
        refreshSize()
        handler.execute(Surface(surface), launcher, lifecycleScope)
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        refreshSize()
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        handler.mIsSurfaceDestroyed = true
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        if (!isRenderingStarted) {
            isRenderingStarted = true
            refreshSize()
            handler.onGraphicOutput()
        }
    }

    @Composable
    private fun Screen(
        content: @Composable () -> Unit = {}
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize().align(Alignment.Center),
                factory = { context ->
                    TextureView(context).apply {
                        isOpaque = true
                        alpha = 1.0f

                        surfaceTextureListener = this@VMActivity
                    }.also { view ->
                        mTextureView = view
                    }
                }
            )

            content()
        }
    }

    private fun refreshSize() {
        val width = getDisplayPixels(ZLApplication.DISPLAY_METRICS.widthPixels)
        val height = getDisplayPixels(ZLApplication.DISPLAY_METRICS.heightPixels)
        if (width < 1 || height < 1) {
            Log.e("VMActivity", "Impossible resolution : $width x $height")
            return
        }
        CallbackBridge.windowWidth = width
        CallbackBridge.windowHeight = height
        mTextureView?.apply {
            surfaceTexture?.setDefaultBufferSize(CallbackBridge.windowWidth, CallbackBridge.windowHeight)
        }
        CallbackBridge.sendUpdateWindowSize(CallbackBridge.windowWidth, CallbackBridge.windowHeight)
    }

    private fun getDisplayPixels(pixels: Int): Int {
        return when (handler.type) {
            HandlerType.GAME -> getDisplayFriendlyRes(pixels, scaleFactor)
            HandlerType.JVM -> getDisplayFriendlyRes(pixels, 0.8f)
        }
    }
}

/**
 * 让VMActivity进入运行游戏模式
 * @param version 指定版本
 */
fun runGame(context: Context, version: Version) {
    val intent = Intent(context, VMActivity::class.java).apply {
        putExtra(VMActivity.INTENT_RUN_GAME, true)
        putExtra(VMActivity.INTENT_VERSION, version)
        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
    context.startActivity(intent)
}

/**
 * 让VMActivity进入运行Jar模式
 * @param jarFile 指定 jar 文件
 * @param jreName 指定使用的 Java 环境，null 则为自动选择
 * @param customArgs 指定 jvm 参数
 */
fun runJar(
    context: Context,
    jarFile: File,
    jreName: String? = null,
    customArgs: String? = null
) {
    RuntimesManager.getExactJreName(8) ?: run {
        Toast.makeText(context, R.string.multirt_no_java_8, Toast.LENGTH_SHORT).show()
        return
    }

    val intent = Intent(context, VMActivity::class.java).apply {
        putExtra(VMActivity.INTENT_RUN_JAR, true)
        putExtra(VMActivity.INTENT_JAR_INFO, JarInfo(jarFile.absolutePath, jreName, customArgs))
        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
    context.startActivity(intent)
}
package com.movtery.zalithlauncher.game.launch.handler

import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import android.view.KeyEvent
import android.view.Surface
import androidx.compose.runtime.Composable
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withSave
import androidx.lifecycle.LifecycleCoroutineScope
import com.movtery.zalithlauncher.ZLApplication
import com.movtery.zalithlauncher.bridge.ZLBridge
import com.movtery.zalithlauncher.game.launch.Launcher
import com.movtery.zalithlauncher.ui.screens.game.JVMScreen
import com.movtery.zalithlauncher.utils.string.StringUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class JVMHandler : AbstractHandler(HandlerType.JVM) {
    private val mCanvasWidth = (ZLApplication.DISPLAY_METRICS.widthPixels * 0.8).toInt()
    private val mCanvasHeight = (ZLApplication.DISPLAY_METRICS.heightPixels * 0.8).toInt()

    override fun execute(surface: Surface, launcher: Launcher, scope: LifecycleCoroutineScope) {
        scope.launch(Dispatchers.Default) {
            var canvas: Canvas?
            val rgbArrayBitmap = createBitmap(mCanvasWidth, mCanvasHeight)
            val paint = Paint()

            try {
                while (!mIsSurfaceDestroyed && surface.isValid) {
                    canvas = surface.lockCanvas(null)
                    canvas?.drawRGB(0, 0, 0)

                    ZLBridge.renderAWTScreenFrame()?.let { rgbArray ->
                        canvas?.withSave {
                            rgbArrayBitmap.setPixels(
                                rgbArray,
                                0,
                                mCanvasWidth,
                                0,
                                0,
                                mCanvasWidth,
                                mCanvasHeight
                            )
                            this.drawBitmap(rgbArrayBitmap, 0f, 0f, paint)
                        }
                    }

                    canvas?.let { surface.unlockCanvasAndPost(it) }
                }
            } catch (throwable: Throwable) {
                Log.e("JVMHandler", StringUtils.throwableToString(throwable))
            } finally {
                rgbArrayBitmap.recycle()
                surface.release()
            }
        }
        super.execute(surface, launcher, scope)
    }

    override fun onPause() {
    }

    override fun onResume() {
    }

    override fun onGraphicOutput() {
    }

    override fun shouldIgnoreKeyEvent(event: KeyEvent): Boolean {
        return true
    }

    @Composable
    override fun getComposableLayout() = @Composable {
        JVMScreen()
    }
}
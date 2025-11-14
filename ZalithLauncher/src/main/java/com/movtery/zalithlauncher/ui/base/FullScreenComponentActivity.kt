/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.ui.base

import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.View
import android.view.View.OnSystemUiVisibilityChangeListener
import android.view.WindowManager
import androidx.annotation.CallSuper

abstract class FullScreenComponentActivity : AbstractComponentActivity() {
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFullscreen()
    }

    @CallSuper
    override fun onPostResume() {
        super.onPostResume()
        setFullscreen()
        ignoreNotch()
    }

    /**
     * 是否忽略刘海屏
     */
    abstract fun shouldIgnoreNotch(): Boolean

    private fun setFullscreen() {
        val decorView = window.decorView
        val visibilityChangeListener =
            OnSystemUiVisibilityChangeListener { visibility: Int ->
                val multiWindowMode = isInMultiWindowMode
                // When in multi-window mode, asking for fullscreen makes no sense (cause the launcher runs in a window)
                // So, ignore the fullscreen setting when activity is in multi window mode
                if (!multiWindowMode) {
                    if ((visibility and View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                or View.SYSTEM_UI_FLAG_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
                    }
                } else {
                    decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                }
            }
        decorView.setOnSystemUiVisibilityChangeListener(visibilityChangeListener)
        visibilityChangeListener.onSystemUiVisibilityChange(decorView.systemUiVisibility)
    }

    protected fun ignoreNotch() {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                if (shouldIgnoreNotch()) {
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                } else {
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
                }
            window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            )
        }
    }
}
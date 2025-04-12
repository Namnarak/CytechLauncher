package com.movtery.zalithlauncher.components

import com.movtery.zalithlauncher.R

enum class Components(val component: String, val displayName: String, val summary: Int, val privateDirectory: Boolean) {
    LWJGL3("lwjgl3", "LWJGL 3", R.string.unpack_screen_lwjgl, true)
}
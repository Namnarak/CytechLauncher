package com.movtery.zalithlauncher.components

import com.movtery.zalithlauncher.R

enum class Components(val component: String, val displayName: String, val summary: Int) {
    AUTH_LIBS("auth_libs", "authlib-injector", R.string.unpack_screen_authlib_injector),
    LWJGL3("lwjgl3", "LWJGL 3", R.string.unpack_screen_lwjgl),
    LAUNCHER("launcher", "Launcher Components", R.string.unpack_screen_launcher)
}
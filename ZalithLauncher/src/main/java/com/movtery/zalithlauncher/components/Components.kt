package com.movtery.zalithlauncher.components

import com.movtery.zalithlauncher.R

enum class Components(val component: String, val displayName: String, val summary: Int) {
    AUTH_LIBS("auth_libs", "authlib-injector", R.string.unpack_screen_authlib_injector),
    CACIOCAVALLO("caciocavallo", "caciocavallo", R.string.unpack_screen_cacio),
    CACIOCAVALLO17("caciocavallo17", "caciocavallo 17", R.string.unpack_screen_cacio),
    LWJGL3("lwjgl3", "LWJGL 3", R.string.unpack_screen_lwjgl),
    LAUNCHER("launcher", "Launcher Components", R.string.unpack_screen_launcher)
}
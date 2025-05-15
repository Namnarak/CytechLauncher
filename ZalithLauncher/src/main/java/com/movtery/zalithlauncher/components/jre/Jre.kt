package com.movtery.zalithlauncher.components.jre

import com.movtery.zalithlauncher.R

enum class Jre(val jreName: String, val jrePath: String, val summary: Int, val majorVersion: Int) {
    JRE_8("Internal-8", "runtimes/jre-8", R.string.unpack_screen_jre8, 8),
    JRE_17("Internal-17", "runtimes/jre-17", R.string.unpack_screen_jre17, 17),
    JRE_21("Internal-21", "runtimes/jre-21", R.string.unpack_screen_jre21, 21)
}
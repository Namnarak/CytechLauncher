package com.movtery.zalithlauncher.game.download.jvm_server

class JvmCrashException(val code: Int) : RuntimeException("During installation, the JVM exited with exception code $code.")
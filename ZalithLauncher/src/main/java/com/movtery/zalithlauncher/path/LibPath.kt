package com.movtery.zalithlauncher.path

import com.movtery.zalithlauncher.path.PathManager.Companion.DIR_COMPONENTS
import java.io.File

class LibPath {
    companion object {
        private val LAUNCHER_COMPONENTS = File(DIR_COMPONENTS, "launcher")
        private val AUTH_LIBS_DIR = File(DIR_COMPONENTS, "auth_libs")

        @JvmField val CACIO_8 = File(DIR_COMPONENTS, "caciocavallo")
        @JvmField val CACIO_17 = File(DIR_COMPONENTS, "caciocavallo17")

        @JvmField val MIO_LIB_PATCHER = File(LAUNCHER_COMPONENTS, "MioLibPatcher.jar")

        @JvmField val AUTHLIB_INJECTOR = File(AUTH_LIBS_DIR, "authlib-injector.jar")
        @JvmField val NIDE_8_AUTH = File(AUTH_LIBS_DIR, "nide8auth.jar")
    }
}
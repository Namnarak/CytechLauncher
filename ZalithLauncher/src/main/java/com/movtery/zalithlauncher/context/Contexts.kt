package com.movtery.zalithlauncher.context

import android.content.Context
import android.content.ContextWrapper
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.Settings

class Contexts {
    companion object {
        fun refresh(context: Context) {
            PathManager.refreshPaths(context)
            Settings.refreshSettings()
        }

        fun getWrapper(context: Context): ContextWrapper {
            refresh(context)
            return ContextWrapper(context)
        }
    }
}
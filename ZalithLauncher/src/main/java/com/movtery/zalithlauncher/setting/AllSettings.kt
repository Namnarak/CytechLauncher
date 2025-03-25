package com.movtery.zalithlauncher.setting

import android.os.Build
import com.movtery.zalithlauncher.setting.unit.IntSettingUnit
import com.movtery.zalithlauncher.setting.unit.StringSettingUnit

class AllSettings {
    companion object {
        //Launcher
        val launcherColorTheme = StringSettingUnit(
            "launcherColorTheme",
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                "DYNAMIC"
            } else {
                "ROSEWOOD_EMBER"
            }
        )

        val launcherAnimateSpeed = IntSettingUnit("launcherAnimateSpeed", 5)
    }
}
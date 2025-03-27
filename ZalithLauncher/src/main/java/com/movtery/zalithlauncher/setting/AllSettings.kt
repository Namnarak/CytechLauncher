package com.movtery.zalithlauncher.setting

import android.os.Build
import com.movtery.zalithlauncher.setting.unit.BooleanSettingUnit
import com.movtery.zalithlauncher.setting.unit.IntSettingUnit
import com.movtery.zalithlauncher.setting.unit.StringSettingUnit

class AllSettings {
    companion object {
        //Launcher
        /**
         * 颜色主题色
         * Android 12+ 默认动态主题色
         */
        val launcherColorTheme = StringSettingUnit(
            "launcherColorTheme",
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                "DYNAMIC"
            } else {
                "ROSEWOOD_EMBER"
            }
        )

        /**
         * 启动器部分屏幕全屏
         */
        val launcherFullScreen = BooleanSettingUnit("launcherFullScreen", true)

        /**
         * 动画倍速
         */
        val launcherAnimateSpeed = IntSettingUnit("launcherAnimateSpeed", 5)

        //Other
        val currentAccount = StringSettingUnit("currentAccount", "")
    }
}
package com.movtery.zalithlauncher.setting

import android.os.Build
import com.movtery.zalithlauncher.game.path.GamePathManager
import com.movtery.zalithlauncher.setting.unit.BooleanSettingUnit
import com.movtery.zalithlauncher.setting.unit.IntSettingUnit
import com.movtery.zalithlauncher.setting.unit.StringSettingUnit
import com.movtery.zalithlauncher.ui.theme.ColorThemeType

class AllSettings {
    companion object {
        //Renderer
        /**
         * 全局渲染器
         */
        val renderer = StringSettingUnit("renderer", "")

        //Game
        /**
         * 版本隔离
         */
        val versionIsolation = BooleanSettingUnit("versionIsolation", true)

        //Launcher
        /**
         * 颜色主题色
         * Android 12+ 默认动态主题色
         */
        val launcherColorTheme = StringSettingUnit(
            "launcherColorTheme",
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) "DYNAMIC"
            else ColorThemeType.EMBERMIRE.name
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
        /**
         * 当前选择的账号
         */
        val currentAccount = StringSettingUnit("currentAccount", "")

        /**
         * 当前选择的游戏目录id
         */
        val currentGamePathId = StringSettingUnit("currentGamePathId", GamePathManager.DEFAULT_ID)

        /**
         * 启动器任务菜单是否展开
         */
        val launcherTaskMenuExpanded = BooleanSettingUnit("launcherTaskMenuExpanded", true)
    }
}
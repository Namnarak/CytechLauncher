package com.movtery.zalithlauncher.setting

import android.os.Build
import com.movtery.zalithlauncher.game.path.GamePathManager
import com.movtery.zalithlauncher.info.InfoDistributor
import com.movtery.zalithlauncher.setting.unit.BooleanSettingUnit
import com.movtery.zalithlauncher.setting.unit.IntSettingUnit
import com.movtery.zalithlauncher.setting.unit.StringSettingUnit
import com.movtery.zalithlauncher.ui.theme.ColorThemeType
import com.movtery.zalithlauncher.utils.animation.TransitionAnimationType

class AllSettings {
    companion object {
        //Renderer
        /**
         * 全局渲染器
         */
        val renderer = StringSettingUnit("renderer", "")

        /**
         * 分辨率
         */
        val resolutionRatio = IntSettingUnit("resolutionRatio", 100)

        //Game
        /**
         * 版本隔离
         */
        val versionIsolation = BooleanSettingUnit("versionIsolation", true)

        /**
         * 版本自定义信息
         */
        val versionCustomInfo = StringSettingUnit("", "${InfoDistributor.LAUNCHER_IDENTIFIER}[zl_version]")

        /**
         * 启动器的Java环境
         */
        val javaRuntime = StringSettingUnit("javaRuntime", "")

        /**
         * 游戏内存分配大小
         */
        val ramAllocation = IntSettingUnit("ramAllocation", -1)

        /**
         * 自定义Jvm启动参数
         */
        val jvmArgs = StringSettingUnit("jvmArgs", "")

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

        /**
         * 启动器页面切换动画类型
         */
        val launcherSwapAnimateType = StringSettingUnit("launcherSwapAnimateType", TransitionAnimationType.BOUNCE.name)

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

        /**
         * 启动屏幕最终用户协议上次更新日期
         */
        val splashEulaDate = StringSettingUnit("splashEulaDate", "")
    }
}
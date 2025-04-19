package com.movtery.zalithlauncher.setting

import android.os.Build
import com.movtery.zalithlauncher.game.path.GamePathManager
import com.movtery.zalithlauncher.info.InfoDistributor
import com.movtery.zalithlauncher.setting.enums.GestureActionType
import com.movtery.zalithlauncher.setting.unit.BooleanSettingUnit
import com.movtery.zalithlauncher.setting.unit.IntSettingUnit
import com.movtery.zalithlauncher.setting.unit.StringSettingUnit
import com.movtery.zalithlauncher.ui.control.mouse.ControlMode
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
         * Vulkan 驱动器
         */
        val vulkanDriver = StringSettingUnit("vulkanDriver", "default turnip")

        /**
         * 分辨率
         */
        val resolutionRatio = IntSettingUnit("resolutionRatio", 100)

        /**
         * 游戏页面全屏化
         */
        val gameFullScreen = BooleanSettingUnit("gameFullScreen", true)

        /**
         * 持续性能模式
         */
        val sustainedPerformance = BooleanSettingUnit("sustainedPerformance", false)

        /**
         * 使用系统的 Vulkan 驱动
         */
        val zinkPreferSystemDriver = BooleanSettingUnit("zinkPreferSystemDriver", false)

        /**
         * Zink 垂直同步
         */
        val vsyncInZink = BooleanSettingUnit("vsyncInZink", false)

        /**
         * 强制在高性能核心运行
         */
        val bigCoreAffinity = BooleanSettingUnit("bigCoreAffinity", false)

        /**
         * 启用着色器日志输出
         */
        val dumpShaders = BooleanSettingUnit("dumpShaders", false)

        //Game
        /**
         * 版本隔离
         */
        val versionIsolation = BooleanSettingUnit("versionIsolation", true)

        /**
         * 不检查游戏完整性
         */
        val skipGameIntegrityCheck = BooleanSettingUnit("skipGameIntegrityCheck", false)

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

        //Control
        /**
         * 虚拟鼠标大小（Dp）
         */
        val mouseSize = IntSettingUnit("mouseSize", 24)

        /**
         * 虚拟鼠标速度
         */
        val mouseSpeed = IntSettingUnit("mouseSpeed", 100)

        /**
         * 虚拟鼠标控制模式
         */
        val mouseControlMode = StringSettingUnit("mouseControlMode", ControlMode.SLIDE.name)

        /**
         * 手势控制
         */
        val gestureControl = BooleanSettingUnit("gestureControl", false)

        /**
         * 手势控制点击时触发的鼠标按钮
         */
        val gestureTapMouseAction = StringSettingUnit("gestureTapMouseAction", GestureActionType.MOUSE_RIGHT.name)

        /**
         * 手势控制长按时触发的鼠标按钮
         */
        val gestureLongPressMouseAction = StringSettingUnit("gestureLongPressMouseAction", GestureActionType.MOUSE_LEFT.name)

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
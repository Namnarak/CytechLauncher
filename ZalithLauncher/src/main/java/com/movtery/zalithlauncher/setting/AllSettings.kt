package com.movtery.zalithlauncher.setting

import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.movtery.zalithlauncher.game.path.GamePathManager
import com.movtery.zalithlauncher.info.InfoDistributor
import com.movtery.zalithlauncher.setting.enums.GestureActionType
import com.movtery.zalithlauncher.setting.enums.MirrorSourceType
import com.movtery.zalithlauncher.setting.enums.MouseControlMode
import com.movtery.zalithlauncher.setting.unit.AbstractSettingUnit
import com.movtery.zalithlauncher.setting.unit.BooleanSettingUnit
import com.movtery.zalithlauncher.setting.unit.IntSettingUnit
import com.movtery.zalithlauncher.setting.unit.StringSettingUnit
import com.movtery.zalithlauncher.setting.unit.enumSettingUnit
import com.movtery.zalithlauncher.ui.theme.ColorThemeType
import com.movtery.zalithlauncher.utils.animation.TransitionAnimationType

class AllSettings {
    companion object {
        private val refreshableList = mutableListOf<AbstractSettingUnit<*>>()

        /**
         * 重载全部设置项
         */
        fun reloadAll() {
            refreshableList.forEach { unit ->
                unit.init()
            }
        }

        //Renderer
        /**
         * 全局渲染器
         */
        val renderer = StringSettingUnit("renderer", "")
            .also { refreshableList.add(it) }

        /**
         * Vulkan 驱动器
         */
        val vulkanDriver = StringSettingUnit("vulkanDriver", "default turnip")
            .also { refreshableList.add(it) }

        /**
         * 分辨率
         */
        val resolutionRatio = IntSettingUnit("resolutionRatio", 100)
            .also { refreshableList.add(it) }

        /**
         * 游戏页面全屏化
         */
        val gameFullScreen = BooleanSettingUnit("gameFullScreen", true)
            .also { refreshableList.add(it) }

        /**
         * 持续性能模式
         */
        val sustainedPerformance = BooleanSettingUnit("sustainedPerformance", false)
            .also { refreshableList.add(it) }

        /**
         * 使用系统的 Vulkan 驱动
         */
        val zinkPreferSystemDriver = BooleanSettingUnit("zinkPreferSystemDriver", false)
            .also { refreshableList.add(it) }

        /**
         * Zink 垂直同步
         */
        val vsyncInZink = BooleanSettingUnit("vsyncInZink", false)
            .also { refreshableList.add(it) }

        /**
         * 强制在高性能核心运行
         */
        val bigCoreAffinity = BooleanSettingUnit("bigCoreAffinity", false)
            .also { refreshableList.add(it) }

        /**
         * 启用着色器日志输出
         */
        val dumpShaders = BooleanSettingUnit("dumpShaders", false)
            .also { refreshableList.add(it) }

        //Game
        /**
         * 版本隔离
         */
        val versionIsolation = BooleanSettingUnit("versionIsolation", true)
            .also { refreshableList.add(it) }

        /**
         * 不检查游戏完整性
         */
        val skipGameIntegrityCheck = BooleanSettingUnit("skipGameIntegrityCheck", false)
            .also { refreshableList.add(it) }

        /**
         * 版本自定义信息
         */
        val versionCustomInfo = StringSettingUnit("versionCustomInfo", "${InfoDistributor.LAUNCHER_IDENTIFIER}[zl_version]")
            .also { refreshableList.add(it) }

        /**
         * 启动器的Java环境
         */
        val javaRuntime = StringSettingUnit("javaRuntime", "")
            .also { refreshableList.add(it) }

        /**
         * 自动选择Java环境
         */
        val autoPickJavaRuntime = BooleanSettingUnit("autoPickJavaRuntime", true)
            .also { refreshableList.add(it) }

        /**
         * 游戏内存分配大小
         */
        val ramAllocation = IntSettingUnit("ramAllocation", -1)
            .also { refreshableList.add(it) }

        /**
         * 自定义Jvm启动参数
         */
        val jvmArgs = StringSettingUnit("jvmArgs", "")
            .also { refreshableList.add(it) }

        /**
         * 启动游戏时自动展示日志，直到游戏开始渲染
         */
        val showLogAutomatic = BooleanSettingUnit("showLogAutomatic", false)
            .also { refreshableList.add(it) }

        /**
         * 日志字体大小
         */
        val logTextSize = IntSettingUnit("logTextSize", 15)
            .also { refreshableList.add(it) }

        /**
         * 日志缓冲区刷新时间
         */
        val logBufferFlushInterval = IntSettingUnit("logBufferFlushInterval", 200)
            .also { refreshableList.add(it) }

        //Control
        /**
         * 实体鼠标控制
         */
        val physicalMouseMode = BooleanSettingUnit("physicalMouseMode", true)
            .also { refreshableList.add(it) }

        /**
         * 虚拟鼠标大小（Dp）
         */
        val mouseSize = IntSettingUnit("mouseSize", 24)
            .also { refreshableList.add(it) }

        /**
         * 虚拟鼠标灵敏度
         */
        val cursorSensitivity = IntSettingUnit("cursorSensitivity", 100)
            .also { refreshableList.add(it) }

        /**
         * 被抓获指针移动灵敏度
         */
        val mouseCaptureSensitivity = IntSettingUnit("mouseCaptureSensitivity", 100)
            .also { refreshableList.add(it) }

        /**
         * 虚拟鼠标控制模式
         */
        val mouseControlMode = enumSettingUnit("mouseControlMode", MouseControlMode.SLIDE)
            .also { refreshableList.add(it) }

        /**
         * 鼠标控制长按延迟
         */
        val mouseLongPressDelay = IntSettingUnit("mouseLongPressDelay", 300)
            .also { refreshableList.add(it) }

        /**
         * 手势控制
         */
        val gestureControl = BooleanSettingUnit("gestureControl", false)
            .also { refreshableList.add(it) }

        /**
         * 手势控制点击时触发的鼠标按钮
         */
        val gestureTapMouseAction = enumSettingUnit("gestureTapMouseAction", GestureActionType.MOUSE_RIGHT)
            .also { refreshableList.add(it) }

        /**
         * 手势控制长按时触发的鼠标按钮
         */
        val gestureLongPressMouseAction = enumSettingUnit("gestureLongPressMouseAction", GestureActionType.MOUSE_LEFT)
            .also { refreshableList.add(it) }

        /**
         * 手势控制长按延迟
         */
        val gestureLongPressDelay = IntSettingUnit("gestureLongPressDelay", 300)
            .also { refreshableList.add(it) }

        //Launcher
        /**
         * 颜色主题色
         * Android 12+ 默认动态主题色
         */
        val launcherColorTheme = enumSettingUnit(
            "launcherColorTheme",
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ColorThemeType.DYNAMIC
            else ColorThemeType.EMBERMIRE
        ).also { refreshableList.add(it) }

        /**
         * 自定义颜色主题色
         */
        val launcherCustomColor = IntSettingUnit("launcherCustomColor", Color.Blue.toArgb())
            .also { refreshableList.add(it) }

        /**
         * 启动器部分屏幕全屏
         */
        val launcherFullScreen = BooleanSettingUnit("launcherFullScreen", true)
            .also { refreshableList.add(it) }

        /**
         * 动画倍速
         */
        val launcherAnimateSpeed = IntSettingUnit("launcherAnimateSpeed", 5)
            .also { refreshableList.add(it) }

        /**
         * 动画幅度
         */
        val launcherAnimateExtent = IntSettingUnit("launcherAnimateExtent", 5)
            .also { refreshableList.add(it) }

        /**
         * 启动器页面切换动画类型
         */
        val launcherSwapAnimateType = enumSettingUnit("launcherSwapAnimateType", TransitionAnimationType.JELLY_BOUNCE)
            .also { refreshableList.add(it) }

        /**
         * 启动器日志保留天数
         */
        val launcherLogRetentionDays = IntSettingUnit("launcherLogRetentionDays", 7)
            .also { refreshableList.add(it) }

        /**
         * 下载版本附加内容镜像源类型
         */
        val fetchModLoaderSource = enumSettingUnit("fetchModLoaderSource", MirrorSourceType.OFFICIAL_FIRST)
            .also { refreshableList.add(it) }

        /**
         * 文件下载镜像源类型
         */
        val fileDownloadSource = enumSettingUnit("fileDownloadSource", MirrorSourceType.OFFICIAL_FIRST)
            .also { refreshableList.add(it) }

        //Other
        /**
         * 当前选择的账号
         */
        val currentAccount = StringSettingUnit("currentAccount", "")
            .also { refreshableList.add(it) }

        /**
         * 当前选择的游戏目录id
         */
        val currentGamePathId = StringSettingUnit("currentGamePathId", GamePathManager.DEFAULT_ID)
            .also { refreshableList.add(it) }

        /**
         * 启动器任务菜单是否展开
         */
        val launcherTaskMenuExpanded = BooleanSettingUnit("launcherTaskMenuExpanded", true)
            .also { refreshableList.add(it) }

        /**
         * 在游戏菜单悬浮窗上显示帧率
         */
        val showFPS = BooleanSettingUnit("showFPS", true)
            .also { refreshableList.add(it) }
    }
}
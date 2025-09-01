package com.movtery.zalithlauncher.ui.control.control

import com.movtery.layer_controller.event.ClickEvent
import com.movtery.zalithlauncher.game.keycodes.ControlEventKeycode
import org.lwjgl.glfw.CallbackBridge

/**
 * 点击按键时，处理LWJGL按键事件
 */
fun lwjglEvent(
    clickEvent: ClickEvent,
    isPressed: Boolean
) {
    if (clickEvent.type != ClickEvent.Type.Key) return
    val keycode: Int = ControlEventKeycode.getKeycodeFromEvent(clickEvent)?.toInt() ?: return
    if (clickEvent.key.startsWith("GLFW_MOUSE_", false)) {
        CallbackBridge.sendMouseButton(keycode, isPressed)
    } else {
        CallbackBridge.sendKeyPress(keycode, CallbackBridge.getCurrentMods(), isPressed)
        CallbackBridge.setModifiers(keycode, isPressed)
    }
}

//启动器点击事件

/** 切换输入法 */
const val LAUNCHER_EVENT_SWITCH_IME = "launcher.event.switch_ime"
/** 切换菜单 */
const val LAUNCHER_EVENT_SWITCH_MENU = "launcher.event.switch_menu"
/** 控制虚拟鼠标滚轮上-长按一直触发 */
const val LAUNCHER_EVENT_SCROLL_UP = "launcher.event.scroll_up"
/** 控制虚拟鼠标滚轮上-单次点击 */
const val LAUNCHER_EVENT_SCROLL_UP_SINGLE = "launcher.event.scroll_up.single"
/** 控制虚拟鼠标滚轮下-长按一直触发 */
const val LAUNCHER_EVENT_SCROLL_DOWN = "launcher.event.scroll_down"
/** 控制虚拟鼠标滚轮下-单次点击 */
const val LAUNCHER_EVENT_SCROLL_DOWN_SINGLE = "launcher.event.scroll_down.single"
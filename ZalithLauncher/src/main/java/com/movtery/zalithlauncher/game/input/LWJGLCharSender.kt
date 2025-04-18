package com.movtery.zalithlauncher.game.input

import com.movtery.zalithlauncher.game.keycodes.LwjglGlfwKeycode
import org.lwjgl.glfw.CallbackBridge

object LWJGLCharSender : CharacterSenderStrategy {
    override fun sendBackspace() {
        CallbackBridge.sendKeycode(LwjglGlfwKeycode.GLFW_KEY_BACKSPACE.toInt(), '\u0008', 0, 0, true)
        CallbackBridge.sendKeycode(LwjglGlfwKeycode.GLFW_KEY_BACKSPACE.toInt(), '\u0008', 0, 0, false)
    }

    override fun sendEnter() {
        CallbackBridge.sendKeyPress(LwjglGlfwKeycode.GLFW_KEY_ENTER.toInt())
    }

    override fun sendChar(character: Char) {
        CallbackBridge.sendChar(character, 0)
    }
}
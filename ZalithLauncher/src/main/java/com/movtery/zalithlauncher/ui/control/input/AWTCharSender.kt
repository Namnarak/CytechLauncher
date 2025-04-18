package com.movtery.zalithlauncher.ui.control.input

import com.movtery.zalithlauncher.bridge.AWTInputEvent
import com.movtery.zalithlauncher.bridge.ZLBridge

object AWTCharSender : CharacterSenderStrategy {
    override fun sendChar(character: Char) {
        ZLBridge.sendChar(character)
    }

    override fun sendBackspace() {
        ZLBridge.sendKey(' ', AWTInputEvent.VK_BACK_SPACE)
    }

    override fun sendEnter() {
        ZLBridge.sendKey(' ', AWTInputEvent.VK_ENTER)
    }
}
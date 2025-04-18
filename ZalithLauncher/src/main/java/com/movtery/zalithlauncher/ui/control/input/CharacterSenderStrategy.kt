package com.movtery.zalithlauncher.ui.control.input

/**
 * Simple interface for sending chars through whatever bridge will be necessary
 * [Modified from PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher/blob/v3_openjdk/app_pojavlauncher/src/main/java/net/kdt/pojavlaunch/customcontrols/keyboard/CharacterSenderStrategy.java)
 */
interface CharacterSenderStrategy {
    /** Called when there is a character to delete, may be called multiple times in a row  */
    fun sendBackspace()

    /** Called when we want to send enter specifically  */
    fun sendEnter()

    /** Called when there is a character to send, may be called multiple times in a row  */
    fun sendChar(character: Char)
}
package com.movtery.zalithlauncher.ui.control.gamepad

import android.os.Parcelable
import com.movtery.zalithlauncher.viewmodel.GamepadViewModel
import kotlinx.parcelize.Parcelize

/**
 * 手柄与键盘映射
 * @param key 手柄键值
 * @param targetsInGame 目标键盘映射（游戏内）
 * @param targetsInMenu 目标键盘映射（菜单内）
 */
@Parcelize
class GamepadMapping(
    val key: Int,
    val dpadDirection: GamepadViewModel.DpadDirection?,
    val targetsInGame: Set<String> = emptySet(),
    val targetsInMenu: Set<String> = emptySet()
): Parcelable
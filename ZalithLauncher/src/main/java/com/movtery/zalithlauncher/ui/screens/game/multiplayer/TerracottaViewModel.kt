/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.ui.screens.game.multiplayer

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movtery.zalithlauncher.terracotta.Terracotta

class TerracottaViewModel: ViewModel() {
    var operation by mutableStateOf<TerracottaOperation>(TerracottaOperation.None)

    /**
     * 打开陶瓦联机菜单
     */
    fun openMenu(context: Activity) {
        if (operation !is TerracottaOperation.None) return

//        if (!Terracotta.initialized) initialize(context)
        operation = TerracottaOperation.ShowMenu
    }

    /**
     * 初始化陶瓦联机
     */
    private fun initialize(context: Activity) {
        Terracotta.initialize(context, viewModelScope)
        Terracotta.setWaiting(context, true)
    }

    //TODO 主要逻辑实现在 ViewModel
}

@Composable
fun rememberTerracottaViewModel(
    keyTag: String
): TerracottaViewModel {
    return viewModel(
        key = keyTag
    ) {
        TerracottaViewModel()
    }
}
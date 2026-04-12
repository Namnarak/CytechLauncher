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

package com.movtery.zalithlauncher.ui.control.input

import android.text.InputType
import android.view.inputmethod.EditorInfo
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun HidableInputLayout(
    view: TouchCharInput?,
    onSend: (String) -> Unit,
    onBackspace: () -> Unit,
    onClose: () -> Unit,
    onViewChanged: (TouchCharInput?) -> Unit,
    keyboardController: SoftwareKeyboardController? = LocalSoftwareKeyboardController.current,
    inputFocus: FocusRequester = remember { FocusRequester() },
) {
    AndroidView(
        modifier = Modifier
            .alpha(0f)
            .size(1.dp)
            .focusRequester(inputFocus),
        factory = { context ->
            TouchCharInput(context).apply {
                imeOptions = EditorInfo.IME_FLAG_NO_FULLSCREEN or
                        EditorInfo.IME_FLAG_NO_EXTRACT_UI or
                        EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING or
                        EditorInfo.IME_ACTION_DONE
                inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or
                        InputType.TYPE_TEXT_VARIATION_FILTER or
                        InputType.TYPE_TEXT_FLAG_MULTI_LINE

                setEms(10)
            }.also { view0 ->
                val view = view0.also {
                    it.setListener(
                        object : InputListener {
                            override fun onSend(char: Char) {
                                onSend(char.toString())
                            }
                            override fun onBackspace() {
                                onBackspace()
                            }
                        }
                    )
                }
                onViewChanged(view)
            }
        }
    )

    LaunchedEffect(view) {
        if (view == null) return@LaunchedEffect
        inputFocus.requestFocus()
        keyboardController?.show()
        view.enableKeyboard()
    }

    DisposableEffect(Unit) {
        onDispose {
            keyboardController?.hide()
            onViewChanged(null)
        }
    }

    OnKeyboardClosed {
        keyboardController?.hide()
        onClose()
    }
}
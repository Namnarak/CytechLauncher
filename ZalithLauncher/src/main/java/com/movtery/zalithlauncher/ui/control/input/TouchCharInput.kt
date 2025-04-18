package com.movtery.zalithlauncher.ui.control.input

import android.text.InputType
import android.view.inputmethod.EditorInfo
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.movtery.zalithlauncher.ui.control.input.view.TouchCharInput

@Composable
fun TouchCharInput(
    characterSender: CharacterSenderStrategy,
    onViewReady: (TouchCharInput) -> Unit
) {
    AndroidView(
        modifier = Modifier.size(1.dp),
        factory = { context ->
            TouchCharInput(context).apply {
                setCharacterSender(characterSender)

                imeOptions = EditorInfo.IME_FLAG_NO_FULLSCREEN or
                        EditorInfo.IME_FLAG_NO_EXTRACT_UI or
                        EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING or
                        EditorInfo.IME_ACTION_DONE

                inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or
                        InputType.TYPE_TEXT_VARIATION_FILTER or
                        InputType.TYPE_TEXT_FLAG_MULTI_LINE

                setEms(10)

                onViewReady(this)
            }
        }
    )
}
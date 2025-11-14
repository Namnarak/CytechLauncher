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

package com.movtery.zalithlauncher.ui.screens.content.versions.elements

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import coil3.compose.AsyncImage
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.SimpleEditDialog
import com.movtery.zalithlauncher.ui.components.SimpleTaskDialog
import com.movtery.zalithlauncher.ui.screens.content.elements.isFilenameInvalid
import com.movtery.zalithlauncher.utils.string.getMessageOrToString
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import kotlinx.coroutines.Dispatchers
import org.apache.commons.io.FileUtils
import java.io.File

/** 加载状态 */
sealed interface LoadingState {
    data object None : LoadingState
    /** 正在加载 */
    data object Loading : LoadingState
}

sealed interface DeleteAllOperation {
    data object None : DeleteAllOperation
    /** 警告用户是否真的要批量删除资源 */
    data class Warning(val files: List<File>): DeleteAllOperation
    /** 开始删除所有选中的资源 */
    data class Delete(val files: List<File>): DeleteAllOperation
    /** 已成功删除所有选中的资源 */
    data object Success : DeleteAllOperation
}

@Composable
fun DeleteAllOperation(
    operation: DeleteAllOperation,
    changeOperation: (DeleteAllOperation) -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    onRefresh: () -> Unit
) {
    when (operation) {
        is DeleteAllOperation.None -> {}
        is DeleteAllOperation.Warning -> {
            SimpleAlertDialog(
                title = stringResource(R.string.manage_delete_all),
                text = {
                    Text(text = stringResource(R.string.manage_delete_all_message))
                },
                confirmText = stringResource(R.string.generic_delete),
                onCancel = {
                    changeOperation(DeleteAllOperation.None)
                },
                onConfirm = {
                    changeOperation(DeleteAllOperation.Delete(operation.files))
                }
            )
        }
        is DeleteAllOperation.Delete -> {
            val context = LocalContext.current
            SimpleTaskDialog(
                title = stringResource(R.string.manage_delete_all),
                task = {
                    //开始删除所有文件
                    operation.files.forEach { file ->
                        FileUtils.delete(file)
                    }

                    changeOperation(DeleteAllOperation.Success)
                    onRefresh()
                },
                context = Dispatchers.IO,
                onDismiss = {},
                onError = { th ->
                    val message = context.getString(R.string.manage_delete_all_error)
                    submitError(
                        ErrorViewModel.ThrowableMessage(
                            title = context.getString(R.string.generic_error),
                            message = message + "\r\n" + th.getMessageOrToString()
                        )
                    )
                }
            )
        }
        is DeleteAllOperation.Success -> {
            SimpleAlertDialog(
                title = stringResource(R.string.manage_delete_all),
                text = stringResource(R.string.manage_delete_all_success)
            ) {
                changeOperation(DeleteAllOperation.None)
            }
        }
    }
}

/**
 * Minecraft 的 `§` 颜色占位符，参考 [Minecraft Wiki](https://zh.minecraft.wiki/w/%E6%A0%BC%E5%BC%8F%E5%8C%96%E4%BB%A3%E7%A0%81#%E9%A2%9C%E8%89%B2%E4%BB%A3%E7%A0%81)
 */
val MINECRAFT_COLOR_FORMAT = mapOf(
    '0' to (Color(0xFF000000) to Color(0xFF000000)),
    '1' to (Color(0xFF0000AA) to Color(0xFF00002A)),
    '2' to (Color(0xFF00AA00) to Color(0xFF002A00)),
    '3' to (Color(0xFF00AAAA) to Color(0xFF002A2A)),
    '4' to (Color(0xFFAA0000) to Color(0xFF2A0000)),
    '5' to (Color(0xFFAA00AA) to Color(0xFF2A002A)),
    '6' to (Color(0xFFFFAA00) to Color(0xFF2A2A00)), //仅 JE，BE 为 #402A00
    '7' to (Color(0xFFAAAAAA) to Color(0xFF2A2A2A)),
    '8' to (Color(0xFF555555) to Color(0xFF151515)),
    '9' to (Color(0xFF5555FF) to Color(0xFF15153F)),
    'a' to (Color(0xFF55FF55) to Color(0xFF153F15)),
    'b' to (Color(0xFF55FFFF) to Color(0xFF153F3F)),
    'c' to (Color(0xFFFF5555) to Color(0xFF3F1515)),
    'd' to (Color(0xFFFF55FF) to Color(0xFF3F153F)),
    'e' to (Color(0xFFFFFF55) to Color(0xFF3F3F15)),
    'f' to (Color(0xFFFFFFFF) to Color(0xFF3F3F3F)),
//    以下仅 BE
//    'g' to (Color(0xFFDDD605) to Color(0xFF373501)),
//    'h' to (Color(0xFFE3D4D1) to Color(0xFF383534)),
//    'i' to (Color(0xFFCECACA) to Color(0xFF333232)),
//    'j' to (Color(0xFF443A3B) to Color(0xFF110E0E)),
//    'm' to (Color(0xFF971607) to Color(0xFF250501)),
//    'n' to (Color(0xFFB4684D) to Color(0xFF2D1A13)),
//    'p' to (Color(0xFFDEB12D) to Color(0xFF372C0B)),
//    'q' to (Color(0xFF47A036) to Color(0xFF04280D)),
//    's' to (Color(0xFF2CBAA8) to Color(0xFF0B2E2A)),
//    't' to (Color(0xFF21497B) to Color(0xFF08121E)),
//    'u' to (Color(0xFF9A5CC6) to Color(0xFF261731)),
//    'v' to (Color(0xFFEB7114) to Color(0xFF3B1D05))
)

/**
 * Minecraft 颜色占位符、样式占位符格式化后的 Text
 * 像 Minecraft 一样，渲染两层文本，底层作为背景层，顶层作为前景层
 * 若输入字符串内不存在 `§`，则使用普通的 Text
 */
@Composable
fun MinecraftColorTextNormal(
    modifier: Modifier = Modifier,
    inputText: String,
    style: TextStyle,
    maxLines: Int = Int.MAX_VALUE
) {
    if (inputText.contains("§")) {
        MinecraftColorText(
            modifier = modifier,
            inputText = inputText,
            fontSize = style.fontSize,
            maxLines = maxLines
        )
    } else {
        Text(
            modifier = modifier,
            text = inputText,
            style = style,
            maxLines = maxLines
        )
    }
}

/**
 * Minecraft 颜色占位符、样式占位符格式化后的 Text
 * 像 Minecraft 一样，渲染两层文本，底层作为背景层，顶层作为前景层
 */
@Composable
fun MinecraftColorText(
    modifier: Modifier = Modifier,
    inputText: String,
    fontSize: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE
) {
    val segments = remember(inputText) { parseSegments(inputText) }
    val density = LocalDensity.current

    //计算出合适的偏移量
    val offsetFactor = 1f / 16f
    val offsetDp = with(density) { (fontSize.toPx() * offsetFactor).toDp() }

    Row(modifier = modifier) {
        segments.forEach { (text, style) ->
            Box {
                //背景层
                Text(
                    text = text,
                    style = style.toTextStyle().copy(color = style.background),
                    fontSize = fontSize,
                    maxLines = maxLines,
                    modifier = Modifier.offset(x = offsetDp, y = offsetDp)
                )
                //前景层
                Text(
                    text = text,
                    style = style.toTextStyle(),
                    fontSize = fontSize,
                    maxLines = maxLines,
                )
            }
        }
    }
}

private fun parseSegments(input: String): List<Pair<String, TextStyleState>> {
    val segments = mutableListOf<Pair<String, TextStyleState>>()
    var currentStyle = TextStyleState()
    var index = 0
    var buffer = StringBuilder()

    while (index < input.length) {
        //判断是否是格式代码
        if (input[index] == '§' && index + 1 < input.length) {
            if (buffer.isNotEmpty()) {
                segments.add(buffer.toString() to currentStyle)
                buffer = StringBuilder()
            }

            val code = input[index + 1].lowercaseChar()
            currentStyle = when (code) {
                in MINECRAFT_COLOR_FORMAT -> {
                    val colors = MINECRAFT_COLOR_FORMAT[code]!!
                    currentStyle.copy(color = colors.first, background = colors.second)
                }
                'r' -> TextStyleState() //重置样式
                'l' -> currentStyle.copy(bold = true)
                'o' -> currentStyle.copy(italic = true)
                'n' -> currentStyle.copy(underline = true)
                'm' -> currentStyle.copy(strikethrough = true)
                else -> currentStyle //忽略未知或不支持的格式代码（如k）
            }

            index += 2
        } else {
            buffer.append(input[index])
            index++
        }
    }

    if (buffer.isNotEmpty()) {
        segments.add(buffer.toString() to currentStyle)
    }

    return segments
}

/**
 * @param color 前景颜色
 * @param background 背景颜色，默认为深灰色
 * @param bold 加粗
 * @param italic 斜体
 * @param underline 下划线
 * @param strikethrough 删除线
 */
private data class TextStyleState(
    val color: Color = Color.White,
    val background: Color = Color(0xFF3F3F3F),
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
    val strikethrough: Boolean = false
) {
    fun toTextStyle(): TextStyle = TextStyle(
        color = color,
        fontWeight = if (bold) FontWeight.Bold else null,
        fontStyle = if (italic) FontStyle.Italic else null,
        textDecoration = TextDecoration.combine(
            listOfNotNull(
                if (underline) TextDecoration.Underline else null,
                if (strikethrough) TextDecoration.LineThrough else null
            )
        )
    )
}

@Composable
fun FileNameInputDialog(
    initValue: String,
    existsCheck: @Composable (String) -> String?,
    title: String,
    label: String,
    onDismissRequest: () -> Unit = {},
    onConfirm: (vale: String) -> Unit = {}
) {
    var value by remember { mutableStateOf(initValue) }
    var errorMessage by remember { mutableStateOf("") }

    val isError = value.isEmpty() || isFilenameInvalid(value) { message ->
        errorMessage = message
    } || existsCheck(value).also { if (it != null) errorMessage = it } != null

    SimpleEditDialog(
        title = title,
        value = value,
        onValueChange = { value = it },
        isError = isError,
        label = {
            Text(text = label)
        },
        supportingText = {
            when {
                value.isEmpty() -> Text(text = stringResource(R.string.generic_cannot_empty))
                isError -> Text(text = errorMessage)
            }
        },
        singleLine = true,
        onDismissRequest = onDismissRequest,
        onConfirm = {
            if (!isError) {
                onConfirm(value)
            }
        }
    )
}

@Composable
fun ByteArrayIcon(
    modifier: Modifier = Modifier,
    triggerRefresh: Any? = null,
    defaultIcon: Int = R.drawable.ic_unknown_pack,
    icon: ByteArray?,
    colorFilter: ColorFilter? = null
) {
    val context = LocalContext.current

    val model = remember(triggerRefresh, context) {
        icon ?: defaultIcon
    }

    AsyncImage(
        modifier = modifier,
        model = model,
        contentDescription = null,
        alignment = Alignment.Center,
        contentScale = ContentScale.Fit,
        colorFilter = colorFilter
    )
}
package com.movtery.zalithlauncher.ui.control.mouse

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.gif.GifDecoder
import coil3.request.ImageRequest
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.utils.file.child
import java.io.File

/**
 * 鼠标指针图片文件
 */
val mousePointerFile: File = PathManager.DIR_MOUSE_POINTER.child("default_pointer.image")

/**
 * 虚拟指针模拟层
 * @param controlMode       控制模式：SLIDE（滑动控制）、CLICK（点击控制）
 * @param onTap             点击回调，参数是触摸点在控件内的绝对坐标
 * @param onLongPress       长按开始回调
 * @param onLongPressEnd    长按结束回调
 * @param onPointerMove     指针移动回调，参数在 SLIDE 模式下是指针位置，CLICK 模式下是手指当前位置
 * @param mouseSize         指针大小
 * @param mouseSpeed        指针移动速度（滑动模式生效）
 */
@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
fun VirtualPointerLayout(
    modifier: Modifier = Modifier,
    controlMode: ControlMode = AllSettings.mouseControlMode.toControlMode(),
    onTap: (Offset) -> Unit = {},
    onLongPress: () -> Unit = {},
    onLongPressEnd: () -> Unit = {},
    onPointerMove: (Offset) -> Unit = {},
    mouseSize: Dp = AllSettings.mouseSize.getValue().dp,
    mouseSpeed: Int = AllSettings.mouseSpeed.getValue(),
) {
    var screenWidth by remember { mutableFloatStateOf(0f) }
    var screenHeight by remember { mutableFloatStateOf(0f) }
    var pointerPosition by remember { mutableStateOf(Offset(0f, 0f)) }

    val speedFactor = mouseSpeed / 100f

    Box(
        modifier = modifier
            .onSizeChanged { size ->
                screenWidth = size.width.toFloat()
                screenHeight = size.height.toFloat()
                pointerPosition = Offset(screenWidth / 2, screenHeight / 2)
                onPointerMove(pointerPosition)
            }
    ) {
        MousePointer(
            modifier = Modifier.offset(
                x = with(LocalDensity.current) { pointerPosition.x.toDp() },
                y = with(LocalDensity.current) { pointerPosition.y.toDp() }
            ),
            mouseSize = mouseSize,
            mouseFile = mousePointerFile
        )

        TouchpadLayout(
            modifier = Modifier.fillMaxSize(),
            controlMode = controlMode,
            onTap = { fingerPos ->
                onTap(
                    if (controlMode == ControlMode.CLICK) {
                        //当前手指的绝对坐标
                        pointerPosition = fingerPos
                        fingerPos
                    } else {
                        pointerPosition
                    }
                )
            },
            onLongPress = onLongPress,
            onLongPressEnd = onLongPressEnd,
            onPointerMove = { offset ->
                pointerPosition =  if (controlMode == ControlMode.SLIDE) {
                    Offset(
                        x = (pointerPosition.x + offset.x * speedFactor)
                            .coerceIn(0f, screenWidth),
                        y = (pointerPosition.y + offset.y * speedFactor)
                            .coerceIn(0f, screenHeight)
                    )
                } else {
                    //当前手指的绝对坐标
                    offset
                }
                onPointerMove(pointerPosition)
            },
            inputChange = arrayOf(speedFactor, controlMode)
        )
    }
}

/**
 * @param triggerRefresh 触发强制重组更新，用于刷新鼠标指针
 */
@Composable
fun MousePointer(
    modifier: Modifier = Modifier,
    mouseSize: Dp = AllSettings.mouseSize.getValue().dp,
    mouseFile: File?,
    centerIcon: Boolean = false,
    triggerRefresh: Boolean = false
) {
    val context = LocalContext.current

    val imageAlignment = if (centerIcon) Alignment.Center else Alignment.TopStart
    val imageModifier = modifier.size(mouseSize)

    if (mouseFile != null) {
        val imageLoader = remember {
            ImageLoader.Builder(context)
                .components { add(GifDecoder.Factory()) }
                .build()
        }

        val imageModel = remember(mouseFile, triggerRefresh) {
            ImageRequest.Builder(context)
                .data(mouseFile)
                .diskCacheKey("${mouseFile.path}_${triggerRefresh.hashCode()}")
                .build()
        }

        AsyncImage(
            model = imageModel,
            imageLoader = imageLoader,
            contentDescription = null,
            alignment = imageAlignment,
            contentScale = ContentScale.Fit,
            modifier = imageModifier
        )
    } else {
        Image(
            painter = painterResource(R.drawable.ic_mouse_pointer),
            contentDescription = null,
            alignment = imageAlignment,
            contentScale = ContentScale.Fit,
            modifier = imageModifier
        )
    }
}
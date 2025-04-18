package com.movtery.zalithlauncher.ui.screens.content

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.context.copyLocalFile
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.state.MutableStates
import com.movtery.zalithlauncher.state.ObjectStates
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.IconTextButton
import com.movtery.zalithlauncher.ui.control.mouse.MousePointer
import com.movtery.zalithlauncher.ui.control.mouse.VirtualPointerLayout
import com.movtery.zalithlauncher.ui.control.mouse.getDefaultMousePointer
import com.movtery.zalithlauncher.ui.control.mouse.mousePointerFile
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.utils.string.StringUtils.Companion.getMessageOrToString
import kotlinx.coroutines.Dispatchers
import org.apache.commons.io.FileUtils

const val MOUSE_POINTER_SCREEN_TAG = "MousePointerScreen"

@Composable
fun MousePointerScreen() {
    BaseScreen(
        screenTag = MOUSE_POINTER_SCREEN_TAG,
        currentTag = MutableStates.mainScreenTag
    ) { isVisible ->
        val context = LocalContext.current

        var mousePainter = getDefaultMousePointer()
        var refreshMousePointer by remember { mutableStateOf(false) }

        if (refreshMousePointer) {
            mousePainter = getDefaultMousePointer()
            refreshMousePointer = false
        }

        val filePicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { result ->
            if (result != null) {
                TaskSystem.submitTask(
                    Task.runTask(
                        dispatcher = Dispatchers.IO,
                        task = {
                            context.copyLocalFile(result, mousePointerFile)
                            refreshMousePointer = true
                        },
                        onError = { th ->
                            FileUtils.deleteQuietly(mousePointerFile)
                            ObjectStates.updateThrowable(
                                ObjectStates.ThrowableMessage(
                                    title = context.getString(R.string.error_import_image),
                                    message = th.getMessageOrToString()
                                )
                            )
                        }
                    )
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            MouseTestLayout(
                isVisible = isVisible,
                mousePainter = mousePainter,
                modifier = Modifier
                    .weight(7f)
                    .fillMaxHeight()
            )
            RightMenu(
                isVisible = isVisible,
                mousePainter = mousePainter,
                modifier = Modifier
                    .weight(3f)
                    .fillMaxHeight()
                    .padding(top = 12.dp, bottom = 12.dp, end = 12.dp),
                importMouseFile = { filePicker.launch(arrayOf("image/*")) },
                resetMouseFile = {
                    FileUtils.deleteQuietly(mousePointerFile)
                    refreshMousePointer = true
                }
            )
        }
    }
}

@Composable
private fun MouseTestLayout(
    isVisible: Boolean,
    mousePainter: Painter,
    modifier: Modifier = Modifier
) {
    val yOffset by swapAnimateDpAsState(
        targetValue = (-40).dp,
        swapIn = isVisible
    )

    VirtualPointerLayout(
        modifier = modifier
            .offset {
                IntOffset(
                    x = 0,
                    y = yOffset.roundToPx()
                )
            },
        mousePainter = mousePainter
    )
}

@Composable
private fun RightMenu(
    isVisible: Boolean,
    mousePainter: Painter,
    modifier: Modifier = Modifier,
    importMouseFile: () -> Unit = {},
    resetMouseFile: () -> Unit = {}
) {
    val xOffset by swapAnimateDpAsState(
        targetValue = 40.dp,
        swapIn = isVisible
    )

    Surface(
        modifier = modifier.offset {
            IntOffset(
                x = xOffset.roundToPx(),
                y = 0
            )
        },
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.inversePrimary,
        shadowElevation = 4.dp
    ) {
        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val (mousePointer, reset) = createRefs()

            Box(
                modifier = Modifier
                    .constrainAs(mousePointer) {
                        top.linkTo(parent.top)
                        bottom.linkTo(anchor = parent.bottom, margin = 34.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .clip(shape = MaterialTheme.shapes.large)
                    .clickable(onClick = importMouseFile)
            ) {
                MousePointer(
                    modifier = Modifier.padding(all = 12.dp),
                    mouseSize = 54.dp,
                    mousePainter = mousePainter,
                    centerIcon = true
                )
            }

            IconTextButton(
                modifier = Modifier
                    .constrainAs(reset) {
                        top.linkTo(anchor = mousePointer.bottom, margin = 8.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                onClick = resetMouseFile,
                imageVector = Icons.Default.RestartAlt,
                contentDescription = stringResource(R.string.generic_reset),
                text = stringResource(R.string.generic_reset)
            )
        }
    }
}
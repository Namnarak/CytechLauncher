package com.movtery.zalithlauncher.ui.screens.content.settings

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.state.MutableStates
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsBackground
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.animation.getAnimateTweenBounce

const val CONTROL_MANAGE_SCREEN_TAG = "ControlManageScreen"

@Composable
fun ControlManageScreen() {
    BaseScreen(
        screenTag = CONTROL_MANAGE_SCREEN_TAG,
        currentTag = MutableStates.settingsScreenTag
    ) { isVisible ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(state = rememberScrollState())
                .padding(all = 12.dp)
        ) {
            val yOffset by animateDpAsState(
                targetValue = if (isVisible) 0.dp else (-40).dp,
                animationSpec = if (isVisible) getAnimateTweenBounce() else getAnimateTween()
            )

            SettingsBackground(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = 0,
                            y = yOffset.roundToPx()
                        )
                    }
            ) {

            }
        }
    }
}
package com.movtery.zalithlauncher.ui.screens.content.download.game

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.state.MutableStates
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.screens.content.DOWNLOAD_SCREEN_TAG
import com.movtery.zalithlauncher.ui.screens.content.download.DOWNLOAD_GAME_SCREEN_TAG
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState

const val DOWNLOAD_GAME_WITH_ADDON_SCREEN_TAG = "DownloadGameWithAddonScreen"

@Composable
fun DownloadGameWithAddonScreen(
    gameVersion: String
) {
    BaseScreen(
        Triple(DOWNLOAD_SCREEN_TAG, MutableStates.mainScreenTag, true),
        Triple(DOWNLOAD_GAME_SCREEN_TAG, MutableStates.downloadScreenTag, false),
        Triple(DOWNLOAD_GAME_WITH_ADDON_SCREEN_TAG, DownloadGameScreenStates.screenTag, true),
    ) { isVisible ->
        val yOffset by swapAnimateDpAsState(
            targetValue = (-40).dp,
            swapIn = isVisible
        )

        Card(
            modifier = Modifier
                .padding(all = 12.dp)
                .fillMaxSize()
                .offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
            shape = MaterialTheme.shapes.extraLarge,
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
        ) {

        }
    }
}
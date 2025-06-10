package com.movtery.zalithlauncher.ui.screens.content.download

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import kotlinx.serialization.Serializable

@Serializable
data object DownloadShadersScreenKey: NestedNavKey {
    override fun isLastScreen(): Boolean = true
}

@Composable
fun DownloadShadersScreen() {
    Box(modifier = Modifier.fillMaxSize()) { Text(modifier = Modifier.align(Alignment.Center) , text = "TODO") }
}
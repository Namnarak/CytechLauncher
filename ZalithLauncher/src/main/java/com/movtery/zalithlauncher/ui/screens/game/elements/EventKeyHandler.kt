package com.movtery.zalithlauncher.ui.screens.game.elements

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateMap
import kotlin.collections.component1
import kotlin.collections.component2

@Composable
fun HandleEventKey(
    keys: SnapshotStateMap<String, Int>,
    handle: (key: String, pressed: Boolean) -> Unit
) {
    LaunchedEffect(keys) {
        snapshotFlow { keys.toMap() }
            .collect { pressedKeys ->
                val keysToRemove = pressedKeys.mapNotNull { (key, count) ->
                    val pressed = when (count) {
                        1 -> true
                        0 -> false
                        else -> return@mapNotNull null
                    }
                    handle(key, pressed)
                    key.takeIf { count == 0 }
                }
                if (keysToRemove.isNotEmpty()) {
                    val snapshotMap = keys.toMap()
                    keysToRemove.forEach { key ->
                        if (snapshotMap[key] == 0) {
                            keys.remove(key)
                        }
                    }
                }
            }
    }
}
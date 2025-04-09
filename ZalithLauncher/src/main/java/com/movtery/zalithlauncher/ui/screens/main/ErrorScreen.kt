package com.movtery.zalithlauncher.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.info.InfoDistributor
import com.movtery.zalithlauncher.ui.components.DownShadow
import com.movtery.zalithlauncher.ui.components.ScalingActionButton
import com.movtery.zalithlauncher.utils.string.StringUtils

@Composable
fun ErrorScreen(
    throwable: Throwable,
    canRestart: Boolean = true,
    onRestartClick: () -> Unit = {},
    onExitClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .zIndex(10f)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            ErrorContent(
                throwable = throwable,
                canRestart = canRestart,
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.background),
                onRestartClick = onRestartClick,
                onExitClick = onExitClick
            )

            DownShadow(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart),
                height = 4.dp
            )
        }
    }
}

@Composable
private fun TopBar(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = stringResource(R.string.crash_launcher_title, InfoDistributor.LAUNCHER_NAME)
            )
        }
    }
}

@Composable
private fun ErrorContent(
    throwable: Throwable,
    canRestart: Boolean,
    modifier: Modifier = Modifier,
    onRestartClick: () -> Unit = {},
    onExitClick: () -> Unit = {}
) {
    Row(modifier = modifier) {
        Surface(
            modifier = Modifier
                .weight(7f)
                .padding(start = 12.dp, top = 12.dp, bottom = 12.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.shapes.extraLarge,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(state = rememberScrollState())
                    .padding(all = 12.dp)
            ) {
                Text(
                    text = stringResource(R.string.crash_launcher_message),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = StringUtils.throwableToString(throwable),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Surface(
            modifier = Modifier
                .weight(3f)
                .padding(all = 12.dp),
            color = MaterialTheme.colorScheme.inversePrimary,
            shape = MaterialTheme.shapes.extraLarge,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(all = 12.dp).fillMaxSize(),
                verticalArrangement = Arrangement.Bottom
            ) {
                if (canRestart) {
                    ScalingActionButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onRestartClick
                    ) {
                        Text(text = stringResource(R.string.crash_restart))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                ScalingActionButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onExitClick
                ) {
                    Text(text = stringResource(R.string.crash_exit))
                }
            }
        }
    }
}
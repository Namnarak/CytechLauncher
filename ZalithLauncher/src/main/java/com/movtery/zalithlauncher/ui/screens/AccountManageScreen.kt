package com.movtery.zalithlauncher.ui.screens

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountType
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.getAccountTypeName
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.state.LocalMainScreenTag
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.screens.elements.PlayerFace
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.animation.getAnimateTweenBounce
import java.util.regex.Pattern

const val ACCOUNT_MANAGE_SCREEN_TAG = "AccountManageScreen"

private val localNamePattern = Pattern.compile("[^a-zA-Z0-9_]")

/**
 * 离线账号登陆
 */
private fun localLogin(userName: String) {
    val account = Account().apply {
        this.username = userName
        this.accountType = AccountType.LOCAL.tag
    }
    runCatching {
        account.save()
        Log.i("LocalLogin", "Saved local account: $userName")
        AccountsManager.reloadAccounts()
    }.onFailure { e ->
        Log.e("LocalLogin", "Failed to save local account: $userName", e)
    }
}

@Composable
fun AccountManageScreen() {
    BaseScreen(
        screenTag = ACCOUNT_MANAGE_SCREEN_TAG,
        tagProvider = LocalMainScreenTag
    ) { isVisible ->
        Box {
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                ServerTypeTab(
                    isVisible = isVisible,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(all = 12.dp)
                        .weight(2.5f)
                )
                AccountsLayout(
                    isVisible = isVisible,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(top = 12.dp, end = 12.dp, bottom = 12.dp)
                        .weight(7.5f)
                )
            }
            if (!isVisible) { //禁止触摸
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0f)
                        .clickable { }
                )
            }
        }
    }
}

@Composable
fun ServerTypeTab(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val xOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else (-40).dp,
        animationSpec = if (isVisible) getAnimateTweenBounce() else getAnimateTween()
    )

    var localLoginDialog by rememberSaveable { mutableStateOf(false) }

    if (localLoginDialog) {
        var showAlert by rememberSaveable { mutableStateOf(false) }
        var userName by rememberSaveable { mutableStateOf<String?>(null) }

        if (showAlert) {
            SimpleAlertDialog(
                title = stringResource(R.string.account_supporting_username_invalid_title),
                text = stringResource(R.string.account_supporting_username_invalid_local_message),
                onConfirm = {
                    showAlert = false
                    localLoginDialog = false
                    localLogin(userName = userName!!)
                },
                onDismiss = {
                    showAlert = false
                }
            )
        }

        LocalLoginDialog(
            onDismissRequest = {
                localLoginDialog = false
            },
            onConfirm = { username, userNameInvalid ->
                userName = username
                if (userNameInvalid) {
                    showAlert = true
                    return@LocalLoginDialog
                }
                localLogin(userName = username)
                localLoginDialog = false
            }
        )
    }

    Surface(
        modifier = modifier
            .offset {
                IntOffset(
                    x = xOffset.roundToPx(),
                    y = 0
                )
            }.fillMaxHeight()
        ,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.inversePrimary,
        shadowElevation = 4.dp
    ) {
        Column {
            Column(
                modifier = modifier
                    .clip(shape = MaterialTheme.shapes.large)
                    .verticalScroll(state = rememberScrollState())
                    .weight(1f)
            ) {
                LoginItem(
                    modifier = Modifier.fillMaxWidth(),
                    serverName = stringResource(R.string.account_type_microsoft)
                ) {

                }
                LoginItem(
                    modifier = Modifier.fillMaxWidth(),
                    serverName = stringResource(R.string.account_type_local)
                ) {
                    localLoginDialog = true
                }
            }

            Button(
                modifier = Modifier
                    .padding(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 8.dp)
                    .fillMaxWidth(),
                onClick = {}
            ) {
                Text(
                    text = stringResource(R.string.account_add_new_server)
                )
            }
        }
    }
}

@Composable
fun AccountsLayout(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val yOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else (-40).dp,
        animationSpec = if (isVisible) getAnimateTweenBounce() else getAnimateTween()
    )

    val accounts by AccountsManager.accountsFlow.collectAsState()

    Surface(
        modifier = modifier.offset {
            IntOffset(
                x = 0,
                y = yOffset.roundToPx()
            )
        },
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.extraLarge,
        shadowElevation = 4.dp
    ) {
        var currentAccount by rememberSaveable { mutableStateOf(AllSettings.currentAccount.getValue()) }
        var deleteAccount by rememberSaveable { mutableStateOf<Account?>(null) }

        //删除账号前弹出Dialog提醒
        if (deleteAccount != null) {
            val account = deleteAccount!!
            SimpleAlertDialog(
                title = stringResource(R.string.account_delete_title),
                text = stringResource(R.string.account_delete_message, account.username),
                onConfirm = {
                    AccountsManager.deleteAccount(account)
                    deleteAccount = null
                },
                onDismiss = {
                    deleteAccount = null
                }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape = MaterialTheme.shapes.extraLarge)
                .padding(start = 12.dp, end = 12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(accounts.size) { index ->
                val account = accounts[index]
                AccountItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (index != accounts.size - 1) 12.dp else 0.dp),
                    currentAccount = currentAccount,
                    account = account,
                    onSelected = { uniqueUUID ->
                        AllSettings.currentAccount.put(uniqueUUID).save()
                        currentAccount = uniqueUUID
                    },
                    onRefreshClick = {},
                    onDeleteClick = { deleteAccount = account }
                )
            }
        }
    }
}

@Composable
fun AccountItem(
    modifier: Modifier = Modifier,
    currentAccount: String,
    account: Account,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    onSelected: (uniqueUUID: String) -> Unit = {},
    onRefreshClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    val selected = currentAccount == account.uniqueUUID

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.inversePrimary,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.large,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (selected) return@clickable
                    onSelected(account.uniqueUUID)
                }
                .clip(shape = MaterialTheme.shapes.large)
                .padding(all = 8.dp)
        ) {
            RadioButton(
                selected = selected,
                onClick = {
                    if (selected) return@RadioButton
                    onSelected(account.uniqueUUID)
                }
            )
            PlayerFace(
                modifier = Modifier.align(Alignment.CenterVertically),
                account = account,
                avatarSize = 46
            )
            Spacer(modifier = Modifier.width(18.dp))
            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f)
            ) {
                val context = LocalContext.current
                Text(text = account.username)
                Text(
                    text = getAccountTypeName(context, account),
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Row {
                IconButton(
                    onClick = onRefreshClick,
                    enabled = account.accountType != AccountType.LOCAL.tag
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.ic_refresh),
                        contentDescription = stringResource(R.string.generic_refresh)
                    )
                }
                IconButton(
                    onClick = onDeleteClick
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = stringResource(R.string.generic_delete)
                    )
                }
            }
        }
    }
}

@Composable
fun LocalLoginDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (userName: String, userNameInvalid: Boolean) -> Unit
) {
    var username by rememberSaveable { mutableStateOf("") }
    var isUserNameInvalid by rememberSaveable { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.account_type_local),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.size(16.dp))
                TextField(
                    value = username,
                    onValueChange = { newValue ->
                        username = newValue.trim()
                    },
                    isError = isUserNameInvalid,
                    supportingText = {
                        val errorText = when {
                            username.isEmpty() -> stringResource(R.string.account_supporting_username_invalid_empty)
                            username.length <= 2 -> stringResource(R.string.account_supporting_username_invalid_short)
                            username.length > 16 -> stringResource(R.string.account_supporting_username_invalid_long)
                            localNamePattern.matcher(username).find() -> stringResource(R.string.account_supporting_username_invalid_illegal_characters)
                            else -> ""
                        }.also {
                            isUserNameInvalid = it.isNotEmpty()
                        }
                        if (isUserNameInvalid) {
                            Text(text = errorText)
                        }
                    },
                    label = {
                        Text(text = stringResource(R.string.account_label_username))
                    }
                )
                Spacer(modifier = Modifier.size(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = onDismissRequest
                    ) {
                        Text(text = stringResource(R.string.generic_cancel))
                    }
                    Spacer(
                        modifier = Modifier.width(16.dp)
                    )
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (username.isNotEmpty()) {
                                onConfirm(username, isUserNameInvalid)
                            }
                        }
                    ) {
                        Text(text = stringResource(R.string.generic_confirm))
                    }
                }
            }
        }
    }
}

@Composable
fun LoginItem(
    modifier: Modifier = Modifier,
    serverName: String,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(start = 4.dp, top = 12.dp, bottom = 12.dp, end = 4.dp)
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(R.drawable.ic_add),
            contentDescription = serverName,
            tint = contentColor
        )
        Spacer(
            modifier = Modifier.width(8.dp)
        )
        Text(
            text = serverName,
            color = contentColor
        )
    }
}

@Composable
fun ServerItem(
    modifier: Modifier = Modifier,
    serverName: String,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    onClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(start = 4.dp)
    ) {
        Text(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
            text = serverName,
            color = contentColor
        )
        Spacer(
            modifier = Modifier.width(8.dp)
        )
        IconButton(
            onClick = onDeleteClick
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(R.drawable.ic_delete),
                contentDescription = serverName,
                tint = contentColor
            )
        }
    }
}
package com.movtery.zalithlauncher.ui.screens

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.isMicrosoftLogging
import com.movtery.zalithlauncher.game.account.localLogin
import com.movtery.zalithlauncher.game.account.microsoftLogin
import com.movtery.zalithlauncher.game.account.otherserver.OtherLoginApi
import com.movtery.zalithlauncher.game.account.otherserver.OtherLoginHelper
import com.movtery.zalithlauncher.game.account.otherserver.models.Servers
import com.movtery.zalithlauncher.game.account.otherserver.models.Servers.Server
import com.movtery.zalithlauncher.game.account.saveAccount
import com.movtery.zalithlauncher.game.account.tryGetFullServerUrl
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.state.LocalMainScreenTag
import com.movtery.zalithlauncher.state.ObjectStates
import com.movtery.zalithlauncher.task.ProgressAwareTask
import com.movtery.zalithlauncher.task.TaskSystem
import com.movtery.zalithlauncher.ui.activities.MainActivity
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.SimpleEditDialog
import com.movtery.zalithlauncher.ui.components.SimpleListDialog
import com.movtery.zalithlauncher.ui.screens.elements.AccountItem
import com.movtery.zalithlauncher.ui.screens.elements.AccountOperation
import com.movtery.zalithlauncher.ui.screens.elements.LoginItem
import com.movtery.zalithlauncher.ui.screens.elements.MicrosoftLoginOperation
import com.movtery.zalithlauncher.ui.screens.elements.OtherLoginOperation
import com.movtery.zalithlauncher.ui.screens.elements.OtherServerLoginDialog
import com.movtery.zalithlauncher.ui.screens.elements.ServerItem
import com.movtery.zalithlauncher.ui.screens.elements.ServerOperation
import com.movtery.zalithlauncher.utils.GSON
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.animation.getAnimateTweenBounce
import com.movtery.zalithlauncher.utils.network.NetWorkUtils
import com.movtery.zalithlauncher.utils.string.StringUtils
import com.movtery.zalithlauncher.utils.string.StringUtils.Companion.getMessageOrToString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONObject
import java.io.File
import java.util.regex.Pattern
import kotlin.coroutines.CoroutineContext

const val ACCOUNT_MANAGE_SCREEN_TAG = "AccountManageScreen"

private val localNamePattern = Pattern.compile("[^a-zA-Z0-9_]")

private val otherServerConfig = MutableStateFlow(Servers().apply { server = ArrayList() })
private val otherServerConfigFile = File(PathManager.DIR_GAME, "other_servers.json")

private fun refreshOtherServer() {
    val config: String = otherServerConfigFile.takeIf { it.exists() }?.readText() ?: return
    otherServerConfig.value = GSON.fromJson(config, Servers::class.java)
}

@Composable
private fun AddOtherServer(
    serverUrl: String,
    onThrowable: (Throwable) -> Unit = {}
) {
    val context = LocalContext.current

    TaskSystem.submitTask(object : ProgressAwareTask<Unit>() {
        override suspend fun performMainTask(coroutineContext: CoroutineContext) {
            updateProgress(-1f, context.getString(R.string.account_other_login_getting_full_url))
            val fullServerUrl = tryGetFullServerUrl(serverUrl)
            if (isCanceled()) return
            updateProgress(0.5f, context.getString(R.string.account_other_login_getting_server_info))
            OtherLoginApi.getServeInfo(fullServerUrl)?.let { data ->
                val server = Server()
                JSONObject(data).optJSONObject("meta")?.let { meta ->
                    server.serverName = meta.optString("serverName")
                    server.baseUrl = fullServerUrl
                    server.register = meta.optJSONObject("links")?.optString("register") ?: ""
                    if (otherServerConfig.value.server.any { it.baseUrl == server.baseUrl }) {
                        //确保服务器不重复
                        return
                    }
                    otherServerConfig.update { currentConfig ->
                        currentConfig.server.add(server)
                        currentConfig.copy()
                    }
                    updateProgress(0.8f, context.getString(R.string.account_other_login_saving_server))
                    otherServerConfigFile.writeText(
                        GSON.toJson(otherServerConfig.value, Servers::class.java)
                    )
                    updateProgress(1f, context.getString(R.string.generic_done))
                }
            }
        }
    }.onThrowable {
        onThrowable(it)
        Log.e("AddOtherServer", "Failed to add other server\n${StringUtils.throwableToString(it)}")
    })
}

@Composable
fun AccountManageScreen() {
    BaseScreen(
        screenTag = ACCOUNT_MANAGE_SCREEN_TAG,
        tagProvider = LocalMainScreenTag
    ) { isVisible ->
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

        var username by rememberSaveable { mutableStateOf("") }
        var isUserNameInvalid by rememberSaveable { mutableStateOf(false) }

        SimpleEditDialog(
            title = stringResource(R.string.account_type_local),
            value = username,
            onValueChange = { username = it.trim() },
            label = { Text(text = stringResource(R.string.account_label_username)) },
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
            onDismissRequest = { localLoginDialog = false },
            onConfirm = {
                if (username.isNotEmpty()) {
                    if (isUserNameInvalid) {
                        showAlert = true
                        return@SimpleEditDialog
                    }
                    localLogin(userName = username)
                    localLoginDialog = false
                }
            }
        )

        if (showAlert) {
            SimpleAlertDialog(
                title = stringResource(R.string.account_supporting_username_invalid_title),
                text = stringResource(R.string.account_supporting_username_invalid_local_message),
                onConfirm = {
                    showAlert = false
                    localLoginDialog = false
                    localLogin(userName = username)
                },
                onDismiss = {
                    showAlert = false
                }
            )
        }
    }

    LaunchedEffect(true) {
        runCatching {
            refreshOtherServer()
        }.onFailure {
            Log.e("ServerTypeTab", "Failed to refresh other server", it)
        }
    }

    var serverOperation by remember { mutableStateOf<ServerOperation>(ServerOperation.None) }
    when (val operation = serverOperation) {
        is ServerOperation.AddNew -> {
            var serverUrl by rememberSaveable { mutableStateOf("") }
            SimpleEditDialog(
                title = stringResource(R.string.account_add_new_server),
                value = serverUrl,
                onValueChange = { serverUrl = it.trim() },
                label = { Text(text = stringResource(R.string.account_label_server_url)) },
                onDismissRequest = { serverOperation = ServerOperation.None },
                onConfirm = {
                    if (it.isNotEmpty()) {
                        serverOperation = ServerOperation.None
                        serverOperation = ServerOperation.Add(serverUrl)
                    }
                }
            )
        }
        is ServerOperation.Add -> {
            AddOtherServer(
                serverUrl = operation.serverUrl,
                onThrowable = { serverOperation = ServerOperation.OnThrowable(it) }
            )
            serverOperation = ServerOperation.None
        }
        is ServerOperation.Delete -> {
            SimpleAlertDialog(
                title = stringResource(R.string.account_other_login_delete_server_title),
                text = stringResource(R.string.account_other_login_delete_server_message, operation.serverName),
                onDismiss = { serverOperation = ServerOperation.None },
                onConfirm = {
                    otherServerConfig.update { currentConfig ->
                        currentConfig.server.removeAt(operation.serverIndex)
                        val configString = GSON.toJson(currentConfig, Servers::class.java)
                        runCatching {
                            otherServerConfigFile.writeText(configString)
                        }.onFailure {
                            Log.e("ServerTypeTab", "Failed to save other server config", it)
                        }
                        currentConfig.copy()
                    }
                    serverOperation = ServerOperation.None
                }
            )
        }
        is ServerOperation.OnThrowable -> {
            ObjectStates.updateThrowable(
                ObjectStates.ThrowableMessage(
                    title = stringResource(R.string.account_other_login_adding_failure),
                    message = operation.throwable.getMessageOrToString()
                )
            )
            serverOperation = ServerOperation.None
        }
        is ServerOperation.None -> {}
    }

    val context = LocalContext.current
    var otherLoginOperation by remember { mutableStateOf<OtherLoginOperation>(OtherLoginOperation.None) }
    when (val operation = otherLoginOperation) {
        is OtherLoginOperation.OnLogin -> {
            OtherServerLoginDialog(
                server = operation.server,
                onRegisterClick = { url ->
                    NetWorkUtils.openLink(context, url)
                    otherLoginOperation = OtherLoginOperation.None
                },
                onDismissRequest = { otherLoginOperation = OtherLoginOperation.None },
                onConfirm = { email, password ->
                    otherLoginOperation = OtherLoginOperation.None
                    OtherLoginHelper(operation.server, email, password, object : OtherLoginHelper.OnLoginListener {
                        override fun onSuccess(account: Account) {
                            otherLoginOperation = OtherLoginOperation.OnSuccess(account)
                        }
                        override fun onFailed(error: String) {
                            otherLoginOperation = OtherLoginOperation.OnFailed(error)
                        }
                    }).createNewAccount(context) { availableProfiles, selectedFunction ->
                        otherLoginOperation = OtherLoginOperation.SelectRole(availableProfiles, selectedFunction)
                    }
                }
            )
        }
        is OtherLoginOperation.OnSuccess -> { saveAccount(operation.account) }
        is OtherLoginOperation.OnFailed -> {
            ObjectStates.updateThrowable(
                ObjectStates.ThrowableMessage(
                    title = stringResource(R.string.account_logging_in_failed),
                    message = operation.error
                )
            )
            otherLoginOperation = OtherLoginOperation.None
        }
        is OtherLoginOperation.SelectRole -> {
            SimpleListDialog(
                title = stringResource(R.string.account_other_login_select_role),
                itemsProvider = { operation.profiles },
                itemTextProvider = { it.name },
                onItemSelected = { operation.selected(it) },
                onDismissRequest = { otherLoginOperation = OtherLoginOperation.None }
            )
        }
        is OtherLoginOperation.None -> {}
    }

    var microsoftLoginOperation by remember { mutableStateOf<MicrosoftLoginOperation>(MicrosoftLoginOperation.None) }
    val mainActivity = context as? MainActivity
    when (microsoftLoginOperation) {
        is MicrosoftLoginOperation.None -> {}
        is MicrosoftLoginOperation.RunTask -> {
            microsoftLogin(
                context = context,
                updateOperation = { microsoftLoginOperation = it },
                checkWebScreenClosed = {
                    mainActivity?.navController?.currentDestination?.route?.startsWith(WEB_VIEW_SCREEN_TAG) == false
                }
            )
            microsoftLoginOperation = MicrosoftLoginOperation.None
        }
    }

    Surface(
        modifier = modifier
            .offset {
                IntOffset(
                    x = xOffset.roundToPx(),
                    y = 0
                )
            }
            .fillMaxHeight()
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
                    serverName = stringResource(R.string.account_type_microsoft),
                ) {
                    if (!isMicrosoftLogging()) {
                        microsoftLoginOperation = MicrosoftLoginOperation.RunTask
                    }
                }
                LoginItem(
                    modifier = Modifier.fillMaxWidth(),
                    serverName = stringResource(R.string.account_type_local)
                ) {
                    localLoginDialog = true
                }
                val servers by otherServerConfig.collectAsState()
                servers.server.forEachIndexed { index, server ->
                    ServerItem(
                        server = server,
                        onClick = { otherLoginOperation = OtherLoginOperation.OnLogin(server) },
                        onDeleteClick = { serverOperation = ServerOperation.Delete(server.serverName, index) }
                    )
                }
            }

            Button(
                modifier = Modifier
                    .padding(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 8.dp)
                    .fillMaxWidth(),
                onClick = { serverOperation = ServerOperation.AddNew }
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
    val currentAccount by AccountsManager.currentAccountFlow.collectAsState()

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
        val context = LocalContext.current
        var accountOperation by remember { mutableStateOf<AccountOperation>(AccountOperation.None) }
        when (val operation = accountOperation) {
            is AccountOperation.Delete -> {
                //删除账号前弹出Dialog提醒
                SimpleAlertDialog(
                    title = stringResource(R.string.account_delete_title),
                    text = stringResource(R.string.account_delete_message, operation.account.username),
                    onConfirm = {
                        AccountsManager.deleteAccount(operation.account)
                        accountOperation = AccountOperation.None
                    },
                    onDismiss = { accountOperation = AccountOperation.None }
                )
            }
            is AccountOperation.Refresh -> {
                if (NetWorkUtils.isNetworkAvailable(context)) {
                    AccountsManager.performLogin(
                        context = context,
                        account = operation.account,
                        onSuccess = {
                            it.downloadSkin()
                            saveAccount(it)
                        },
                        onFailed = { accountOperation = AccountOperation.OnFailed(it) }
                    )
                }
                accountOperation = AccountOperation.None
            }
            is AccountOperation.OnFailed -> {
                ObjectStates.updateThrowable(
                    ObjectStates.ThrowableMessage(
                        title = stringResource(R.string.account_logging_in_failed),
                        message = operation.error
                    )
                )
                accountOperation = AccountOperation.None
            }
            is AccountOperation.None -> {}
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
                        AccountsManager.setCurrentAccount(uniqueUUID)
                    },
                    onRefreshClick = { accountOperation = AccountOperation.Refresh(account) },
                    onDeleteClick = { accountOperation = AccountOperation.Delete(account) }
                )
            }
        }
    }
}

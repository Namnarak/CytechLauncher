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
import com.movtery.zalithlauncher.game.account.AccountType
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.otherserver.AuthResult
import com.movtery.zalithlauncher.game.account.otherserver.OtherLoginApi
import com.movtery.zalithlauncher.game.account.otherserver.OtherLoginHelper
import com.movtery.zalithlauncher.game.account.otherserver.Servers
import com.movtery.zalithlauncher.game.account.otherserver.Servers.Server
import com.movtery.zalithlauncher.game.account.tryGetFullServerUrl
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.state.LocalMainScreenTag
import com.movtery.zalithlauncher.task.ProgressAwareTask
import com.movtery.zalithlauncher.task.TaskSystem
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.SimpleEditDialog
import com.movtery.zalithlauncher.ui.components.SimpleListDialog
import com.movtery.zalithlauncher.ui.screens.elements.AccountItem
import com.movtery.zalithlauncher.ui.screens.elements.LoginItem
import com.movtery.zalithlauncher.ui.screens.elements.OtherServerLoginDialog
import com.movtery.zalithlauncher.ui.screens.elements.ServerItem
import com.movtery.zalithlauncher.utils.GSON
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.animation.getAnimateTweenBounce
import com.movtery.zalithlauncher.utils.network.NetWorkUtils
import com.movtery.zalithlauncher.utils.string.StringUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONObject
import java.io.File
import java.util.regex.Pattern

const val ACCOUNT_MANAGE_SCREEN_TAG = "AccountManageScreen"

private val localNamePattern = Pattern.compile("[^a-zA-Z0-9_]")

private val otherServerConfig = MutableStateFlow(Servers())
private val otherServerConfigFile = File(PathManager.DIR_GAME, "other_servers.json")

/**
 * 添加认证服务器时的状态
 */
sealed interface ServerOperation {
    data object None : ServerOperation
    data class Add(val serverUrl: String) : ServerOperation
    data class OnThrowable(val throwable: Throwable) : ServerOperation
}

/**
 * 认证服务器登陆时的状态
 */
sealed interface OtherLoginOperation {
    data object None : OtherLoginOperation
    data class OnLogin(val server: Server) : OtherLoginOperation
    data class OnSuccess(val account: Account) : OtherLoginOperation
    data class OnFailed(val error: String) : OtherLoginOperation
    data class SelectRole(
        val profiles: List<AuthResult.AvailableProfiles>,
        val selected: (AuthResult.AvailableProfiles) -> Unit
    ) : OtherLoginOperation
}

/**
 * 离线账号登陆
 */
private fun localLogin(userName: String) {
    val account = Account().apply {
        this.username = userName
        this.accountType = AccountType.LOCAL.tag
    }
    saveAccount(account)
}

private fun saveAccount(account: Account) {
    runCatching {
        account.save()
        Log.i("SaveAccount", "Saved local account: ${account.username}")
        AccountsManager.reloadAccounts()
    }.onFailure { e ->
        Log.e("SaveAccount", "Failed to save local account: ${account.username}", e)
    }
}

private fun refreshOtherServer() {
    val config = otherServerConfigFile.readText()
    otherServerConfig.value = GSON.fromJson(config, Servers::class.java)
}

@Composable
private fun AddOtherServer(
    serverUrl: String,
    onThrowable: (Throwable) -> Unit = {}
) {
    val context = LocalContext.current

    TaskSystem.submitTask(object : ProgressAwareTask<Unit>() {
        override suspend fun performMainTask() {
            updateProgress(0f, context.getString(R.string.account_other_login_getting_full_url))
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

    var yggdrasilServerDialog by rememberSaveable { mutableStateOf(false) }
    var serverOperation by remember { mutableStateOf<ServerOperation>(ServerOperation.None) }

    when (val operation = serverOperation) {
        is ServerOperation.Add -> {
            AddOtherServer(
                serverUrl = operation.serverUrl,
                onThrowable = { serverOperation = ServerOperation.OnThrowable(it) }
            )
            serverOperation = ServerOperation.None
        }
        is ServerOperation.OnThrowable -> {
            SimpleAlertDialog(
                title = stringResource(R.string.account_other_login_adding_failure),
                text = StringUtils.throwableToString(operation.throwable)
            ) {
                serverOperation = ServerOperation.None
            }
        }
        is ServerOperation.None -> {}
    }

    if (yggdrasilServerDialog) {
        var serverUrl by rememberSaveable { mutableStateOf("") }

        SimpleEditDialog(
            title = stringResource(R.string.account_add_new_server),
            value = serverUrl,
            onValueChange = { serverUrl = it.trim() },
            label = { Text(text = stringResource(R.string.account_label_server_url)) },
            onDismissRequest = { yggdrasilServerDialog = false },
            onConfirm = {
                if (it.isNotEmpty()) {
                    yggdrasilServerDialog = false
                    serverOperation = ServerOperation.Add(serverUrl)
                }
            }
        )
    }

    runCatching {
        refreshOtherServer()
    }.onFailure {
        Log.e("ServerTypeTab", "Failed to refresh other server", it)
    }

    var deleteServer by remember { mutableStateOf<Int?>(null) }
    deleteServer?.let { index ->
        SimpleAlertDialog(
            title = stringResource(R.string.account_other_login_delete_server_title),
            text = stringResource(
                R.string.account_other_login_delete_server_message,
                otherServerConfig.value.server[index].serverName
            ),
            onDismiss = { deleteServer = null },
            onConfirm = {
                otherServerConfig.update { currentConfig ->
                    currentConfig.server.removeAt(index)
                    val configString = GSON.toJson(currentConfig, Servers::class.java)
                    runCatching {
                        otherServerConfigFile.writeText(configString)
                    }.onFailure {
                        Log.e("ServerTypeTab", "Failed to save other server config", it)
                    }
                    currentConfig.copy()
                }
                deleteServer = null
            }
        )
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
            SimpleAlertDialog(
                title = stringResource(R.string.account_logging_in_failed),
                text = operation.error
            ) { otherLoginOperation = OtherLoginOperation.None }
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
                    serverName = stringResource(R.string.account_type_microsoft)
                ) {

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
                        onDeleteClick = { deleteServer = index }
                    )
                }
            }

            Button(
                modifier = Modifier
                    .padding(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 8.dp)
                    .fillMaxWidth(),
                onClick = {
                    yggdrasilServerDialog = true
                }
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
                        AccountsManager.setCurrentAccount(uniqueUUID)
                    },
                    onRefreshClick = {},
                    onDeleteClick = { deleteAccount = account }
                )
            }
        }
    }
}

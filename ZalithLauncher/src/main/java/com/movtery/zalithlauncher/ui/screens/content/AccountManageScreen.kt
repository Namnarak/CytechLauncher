/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.ui.screens.content

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.context.COPY_LABEL_ACCOUNT_UUID
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.auth_server.data.AuthServer
import com.movtery.zalithlauncher.game.account.isAuthServerAccount
import com.movtery.zalithlauncher.game.account.isLocalAccount
import com.movtery.zalithlauncher.game.account.isMicrosoftAccount
import com.movtery.zalithlauncher.game.account.isMicrosoftLogging
import com.movtery.zalithlauncher.game.account.wardrobe.EmptyCape
import com.movtery.zalithlauncher.game.account.wardrobe.capeTranslatedName
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.ScalingActionButton
import com.movtery.zalithlauncher.ui.components.ScalingLabel
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.SimpleEditDialog
import com.movtery.zalithlauncher.ui.components.SimpleListDialog
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.AccountItem
import com.movtery.zalithlauncher.ui.screens.content.elements.AccountOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.AccountSkinOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.LocalLoginDialog
import com.movtery.zalithlauncher.ui.screens.content.elements.LocalLoginOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.LoginItem
import com.movtery.zalithlauncher.ui.screens.content.elements.MicrosoftChangeCapeOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.MicrosoftChangeSkinOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.MicrosoftLoginOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.MicrosoftLoginTipDialog
import com.movtery.zalithlauncher.ui.screens.content.elements.OtherLoginOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.OtherServerLoginDialog
import com.movtery.zalithlauncher.ui.screens.content.elements.SelectCapeDialog
import com.movtery.zalithlauncher.ui.screens.content.elements.SelectSkinModelDialog
import com.movtery.zalithlauncher.ui.screens.content.elements.ServerItem
import com.movtery.zalithlauncher.ui.screens.content.elements.ServerOperation
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.utils.copyText
import com.movtery.zalithlauncher.utils.string.getMessageOrToString
import com.movtery.zalithlauncher.viewmodel.AccountManageViewModel
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel

@Composable
fun AccountManageScreen(
    backStackViewModel: ScreenBackStackViewModel,
    backToMainScreen: () -> Unit,
    openLink: (url: String) -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    viewModel: AccountManageViewModel = viewModel()
) {
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val currentAccount by viewModel.currentAccount.collectAsStateWithLifecycle()
    val authServers by viewModel.authServers.collectAsStateWithLifecycle()

    val microsoftLoginOperation by viewModel.microsoftLoginOperation.collectAsStateWithLifecycle()
    val microsoftChangeSkinOperation by viewModel.microsoftChangeSkinOperation.collectAsStateWithLifecycle()
    val microsoftChangeCapeOperation by viewModel.microsoftChangeCapeOperation.collectAsStateWithLifecycle()
    val localLoginOperation by viewModel.localLoginOperation.collectAsStateWithLifecycle()
    val otherLoginOperation by viewModel.otherLoginOperation.collectAsStateWithLifecycle()
    val serverOperation by viewModel.serverOperation.collectAsStateWithLifecycle()
    val accountOperation by viewModel.accountOperation.collectAsStateWithLifecycle()
    val accountSkinOperationMap by viewModel.accountSkinOperationMap.collectAsStateWithLifecycle()

    BaseScreen(
        screenKey = NormalNavKey.AccountManager,
        currentKey = backStackViewModel.mainScreen.currentKey
    ) { isVisible ->
        AccountManageContent(
            isVisible = isVisible,
            accounts = accounts,
            currentAccount = currentAccount,
            authServers = authServers,
            microsoftLoginOperation = microsoftLoginOperation,
            microsoftChangeSkinOperation = microsoftChangeSkinOperation,
            microsoftChangeCapeOperation = microsoftChangeCapeOperation,
            localLoginOperation = localLoginOperation,
            otherLoginOperation = otherLoginOperation,
            serverOperation = serverOperation,
            accountOperation = accountOperation,
            accountSkinOperationMap = accountSkinOperationMap,
            onUpdateMicrosoftLoginOp = viewModel::updateMicrosoftLoginOperation,
            onUpdateLocalLoginOp = viewModel::updateLocalLoginOperation,
            onUpdateOtherLoginOp = viewModel::updateOtherLoginOperation,
            onUpdateServerOp = viewModel::updateServerOperation,
            onUpdateAccountOp = viewModel::updateAccountOperation,
            onUpdateAccountSkinOp = viewModel::updateAccountSkinOperation,
            onUpdateMicrosoftSkinOp = viewModel::updateMicrosoftChangeSkinOperation,
            onUpdateMicrosoftCapeOp = viewModel::updateMicrosoftChangeCapeOperation,
            onPerformMicrosoftLogin = { context, toWeb, backToMain, check ->
                viewModel.performMicrosoftLogin(context, toWeb, backToMain, check, submitError)
            },
            onImportSkinFile = { context, account, uri ->
                viewModel.importSkinFile(context, account, uri, submitError)
            },
            onUploadMicrosoftSkin = { context, account, file, model ->
                viewModel.uploadMicrosoftSkin(context, account, file, model, submitError)
            },
            onFetchMicrosoftCapes = { context, account ->
                viewModel.fetchMicrosoftCapes(context, account, submitError)
            },
            onApplyMicrosoftCape = { context, account, id, name, reset ->
                viewModel.applyMicrosoftCape(context, account, id, name, reset, submitError)
            },
            onCreateLocalAccount = viewModel::createLocalAccount,
            onLoginWithOtherServer = { context, server, email, pass ->
                viewModel.loginWithOtherServer(context, server, email, pass, submitError)
            },
            onAddServer = viewModel::addServer,
            onDeleteServer = viewModel::deleteServer,
            onDeleteAccount = viewModel::deleteAccount,
            onRefreshAccount = viewModel::refreshAccount,
            onSaveLocalSkin = { context, acc, uri, refresh ->
                viewModel.saveLocalSkin(context, acc, uri, refresh, submitError)
            },
            onResetSkin = viewModel::resetSkin,
            onFormatError = { context, th -> viewModel.formatAccountError(context, th) },
            openLink = openLink,
            submitError = submitError,
            backToMainScreen = backToMainScreen,
            navigateToWeb = { url -> backStackViewModel.mainScreen.backStack.navigateToWeb(url) },
            checkIfInWebScreen = { backStackViewModel.mainScreen.currentKey is NormalNavKey.WebScreen }
        )
    }
}

@Composable
private fun AccountManageContent(
    isVisible: Boolean,
    accounts: List<Account>,
    currentAccount: Account?,
    authServers: List<AuthServer>,
    microsoftLoginOperation: MicrosoftLoginOperation,
    microsoftChangeSkinOperation: MicrosoftChangeSkinOperation,
    microsoftChangeCapeOperation: MicrosoftChangeCapeOperation,
    localLoginOperation: LocalLoginOperation,
    otherLoginOperation: OtherLoginOperation,
    serverOperation: ServerOperation,
    accountOperation: AccountOperation,
    accountSkinOperationMap: Map<String, AccountSkinOperation>,
    onUpdateMicrosoftLoginOp: (MicrosoftLoginOperation) -> Unit,
    onUpdateLocalLoginOp: (LocalLoginOperation) -> Unit,
    onUpdateOtherLoginOp: (OtherLoginOperation) -> Unit,
    onUpdateServerOp: (ServerOperation) -> Unit,
    onUpdateAccountOp: (AccountOperation) -> Unit,
    onUpdateAccountSkinOp: (String, AccountSkinOperation) -> Unit,
    onUpdateMicrosoftSkinOp: (MicrosoftChangeSkinOperation) -> Unit,
    onUpdateMicrosoftCapeOp: (MicrosoftChangeCapeOperation) -> Unit,
    onPerformMicrosoftLogin: (Context, (String) -> Unit, () -> Unit, () -> Boolean) -> Unit,
    onImportSkinFile: (Context, Account, Uri) -> Unit,
    onUploadMicrosoftSkin: (Context, Account, java.io.File, com.movtery.zalithlauncher.game.account.wardrobe.SkinModelType) -> Unit,
    onFetchMicrosoftCapes: (Context, Account) -> Unit,
    onApplyMicrosoftCape: (Context, Account, String?, String, Boolean) -> Unit,
    onCreateLocalAccount: (String, String?) -> Unit,
    onLoginWithOtherServer: (Context, AuthServer, String, String) -> Unit,
    onAddServer: (String) -> Unit,
    onDeleteServer: (AuthServer) -> Unit,
    onDeleteAccount: (Account) -> Unit,
    onRefreshAccount: (Context, Account) -> Unit,
    onSaveLocalSkin: (Context, Account, Uri, () -> Unit) -> Unit,
    onResetSkin: (Account, () -> Unit) -> Unit,
    onFormatError: (Context, Throwable) -> String,
    openLink: (url: String) -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    backToMainScreen: () -> Unit,
    navigateToWeb: (url: String) -> Unit,
    checkIfInWebScreen: () -> Boolean
) {
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        ServerTypeMenu(
            isVisible = isVisible,
            modifier = Modifier
                .fillMaxHeight()
                .padding(all = 12.dp)
                .weight(3f),
            authServers = authServers,
            onUpdateMicrosoftLoginOp = onUpdateMicrosoftLoginOp,
            onUpdateLocalLoginOp = onUpdateLocalLoginOp,
            onUpdateOtherLoginOp = onUpdateOtherLoginOp,
            onUpdateServerOp = onUpdateServerOp
        )
        AccountsLayout(
            isVisible = isVisible,
            modifier = Modifier
                .fillMaxHeight()
                .padding(top = 12.dp, end = 12.dp, bottom = 12.dp)
                .weight(7f),
            accounts = accounts,
            currentAccount = currentAccount,
            accountOperation = accountOperation,
            accountSkinOperationMap = accountSkinOperationMap,
            onUpdateAccountOp = onUpdateAccountOp,
            onUpdateAccountSkinOp = onUpdateAccountSkinOp,
            onUpdateMicrosoftSkinOp = onUpdateMicrosoftSkinOp,
            onUpdateMicrosoftCapeOp = onUpdateMicrosoftCapeOp,
            onRefreshAccount = onRefreshAccount,
            onSaveLocalSkin = onSaveLocalSkin,
            onResetSkin = onResetSkin,
            onFormatError = onFormatError,
            submitError = submitError
        )
    }

    //微软账号操作逻辑
    MicrosoftLoginOperation(
        checkIfInWebScreen = checkIfInWebScreen,
        navigateToWeb = navigateToWeb,
        backToMainScreen = backToMainScreen,
        microsoftLoginOperation = microsoftLoginOperation,
        updateOperation = onUpdateMicrosoftLoginOp,
        openLink = openLink,
        onPerformMicrosoftLogin = onPerformMicrosoftLogin
    )

    //微软账号更改皮肤操作逻辑
    MicrosoftChangeSkinOperation(
        operation = microsoftChangeSkinOperation,
        updateOperation = onUpdateMicrosoftSkinOp,
        onImportSkinFile = onImportSkinFile,
        onUploadMicrosoftSkin = onUploadMicrosoftSkin
    )

    //微软账号更改披风操作逻辑
    MicrosoftChangeCapeOperation(
        operation = microsoftChangeCapeOperation,
        updateOperation = onUpdateMicrosoftCapeOp,
        onFetchMicrosoftCapes = onFetchMicrosoftCapes,
        onApplyMicrosoftCape = onApplyMicrosoftCape
    )

    //离线账号操作逻辑
    LocalLoginOperation(
        localLoginOperation = localLoginOperation,
        updateOperation = onUpdateLocalLoginOp,
        openLink = openLink,
        onCreateLocalAccount = onCreateLocalAccount
    )

    //外置账号操作逻辑
    OtherLoginOperation(
        otherLoginOperation = otherLoginOperation,
        updateOperation = onUpdateOtherLoginOp,
        submitError = submitError,
        openLink = openLink,
        onLoginWithOtherServer = onLoginWithOtherServer,
        onFormatError = onFormatError
    )

    //外置服务器操作逻辑
    ServerTypeOperation(
        serverOperation = serverOperation,
        updateServerOperation = onUpdateServerOp,
        submitError = submitError,
        onAddServer = onAddServer,
        onDeleteServer = onDeleteServer
    )
}

@Composable
private fun ServerTypeMenu(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    authServers: List<AuthServer>,
    onUpdateMicrosoftLoginOp: (MicrosoftLoginOperation) -> Unit,
    onUpdateLocalLoginOp: (LocalLoginOperation) -> Unit,
    onUpdateOtherLoginOp: (OtherLoginOperation) -> Unit,
    onUpdateServerOp: (ServerOperation) -> Unit
) {
    val xOffset by swapAnimateDpAsState(
        targetValue = (-40).dp,
        swapIn = isVisible,
        isHorizontal = true
    )

    BackgroundCard(
        modifier = modifier
            .offset { IntOffset(x = xOffset.roundToPx(), y = 0) }
            .fillMaxHeight(),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(state = rememberScrollState())
                .padding(all = 12.dp)
        ) {
            LoginItem(
                modifier = Modifier.fillMaxWidth(),
                serverName = stringResource(R.string.account_type_microsoft),
            ) {
                if (!isMicrosoftLogging()) {
                    onUpdateMicrosoftLoginOp(MicrosoftLoginOperation.Tip)
                }
            }
            LoginItem(
                modifier = Modifier.fillMaxWidth(),
                serverName = stringResource(R.string.account_type_local)
            ) {
                onUpdateLocalLoginOp(LocalLoginOperation.Edit)
            }

            authServers.forEach { server ->
                ServerItem(
                    server = server,
                    onClick = { onUpdateOtherLoginOp(OtherLoginOperation.OnLogin(server)) },
                    onDeleteClick = { onUpdateServerOp(ServerOperation.Delete(server)) }
                )
            }
        }

        ScalingActionButton(
            modifier = Modifier
                .padding(PaddingValues(horizontal = 12.dp, vertical = 8.dp))
                .fillMaxWidth(),
            onClick = { onUpdateServerOp(ServerOperation.AddNew) }
        ) {
            MarqueeText(text = stringResource(R.string.account_add_new_server_button))
        }
    }
}

/**
 * 微软账号登陆操作逻辑
 */
@Composable
private fun MicrosoftLoginOperation(
    checkIfInWebScreen: () -> Boolean,
    navigateToWeb: (url: String) -> Unit,
    backToMainScreen: () -> Unit,
    microsoftLoginOperation: MicrosoftLoginOperation,
    updateOperation: (MicrosoftLoginOperation) -> Unit,
    openLink: (url: String) -> Unit,
    onPerformMicrosoftLogin: (Context, (String) -> Unit, () -> Unit, () -> Boolean) -> Unit
) {
    val context = LocalContext.current

    when (microsoftLoginOperation) {
        is MicrosoftLoginOperation.None -> {}
        is MicrosoftLoginOperation.Tip -> {
            MicrosoftLoginTipDialog(
                onDismissRequest = { updateOperation(MicrosoftLoginOperation.None) },
                onConfirm = { updateOperation(MicrosoftLoginOperation.RunTask) },
                openLink = openLink
            )
        }
        is MicrosoftLoginOperation.RunTask -> {
            onPerformMicrosoftLogin(context, navigateToWeb, backToMainScreen, checkIfInWebScreen)
        }
    }
}

@Composable
private fun MicrosoftChangeSkinOperation(
    operation: MicrosoftChangeSkinOperation,
    updateOperation: (MicrosoftChangeSkinOperation) -> Unit,
    onImportSkinFile: (Context, Account, Uri) -> Unit,
    onUploadMicrosoftSkin: (Context, Account, java.io.File, com.movtery.zalithlauncher.game.account.wardrobe.SkinModelType) -> Unit
) {
    val context = LocalContext.current
    when (operation) {
        is MicrosoftChangeSkinOperation.None -> {}
        is MicrosoftChangeSkinOperation.ImportFile -> {
            onImportSkinFile(context, operation.account, operation.uri)
        }
        is MicrosoftChangeSkinOperation.SelectSkinModel -> {
            SelectSkinModelDialog(
                onDismissRequest = {
                    updateOperation(MicrosoftChangeSkinOperation.None)
                },
                onSelected = { modelType ->
                    updateOperation(
                        MicrosoftChangeSkinOperation.RunTask(
                            account = operation.account,
                            file = operation.file,
                            skinModel = modelType
                        )
                    )
                }
            )
        }
        is MicrosoftChangeSkinOperation.RunTask -> {
            onUploadMicrosoftSkin(context, operation.account, operation.file, operation.skinModel)
        }
    }
}

/**
 * 微软账号更改披风操作逻辑
 */
@Composable
private fun MicrosoftChangeCapeOperation(
    operation: MicrosoftChangeCapeOperation,
    updateOperation: (MicrosoftChangeCapeOperation) -> Unit,
    onFetchMicrosoftCapes: (Context, Account) -> Unit,
    onApplyMicrosoftCape: (Context, Account, String?, String, Boolean) -> Unit
) {
    val context = LocalContext.current
    when (operation) {
        is MicrosoftChangeCapeOperation.None -> {}
        is MicrosoftChangeCapeOperation.FetchProfiles -> {
            onFetchMicrosoftCapes(context, operation.account)
        }
        is MicrosoftChangeCapeOperation.SelectCape -> {
            val account = operation.account
            val profile = operation.profile

            val capes = remember(profile.capes) {
                listOf(EmptyCape) + profile.capes
            }

            SelectCapeDialog(
                capes = capes,
                onSelected = { cape ->
                    updateOperation(MicrosoftChangeCapeOperation.RunTask(account, cape))
                },
                onDismiss = {
                    updateOperation(MicrosoftChangeCapeOperation.None)
                }
            )
        }
        is MicrosoftChangeCapeOperation.RunTask -> {
            val capeId: String? = operation.cape.takeIf { it != EmptyCape }?.id
            onApplyMicrosoftCape(
                context,
                operation.account,
                capeId,
                operation.cape.capeTranslatedName(),
                operation.cape == EmptyCape
            )
        }
    }
}

/**
 * 离线账号登陆操作逻辑
 */
@Composable
private fun LocalLoginOperation(
    localLoginOperation: LocalLoginOperation,
    updateOperation: (LocalLoginOperation) -> Unit,
    openLink: (url: String) -> Unit,
    onCreateLocalAccount: (String, String?) -> Unit
) {
    when (localLoginOperation) {
        is LocalLoginOperation.None -> {}
        is LocalLoginOperation.Edit -> {
            LocalLoginDialog(
                onDismissRequest = { updateOperation(LocalLoginOperation.None) },
                onConfirm = { isUserNameInvalid, userName, userUUID ->
                    val operation = if (isUserNameInvalid) {
                        LocalLoginOperation.Alert(userName, userUUID)
                    } else {
                        LocalLoginOperation.Create(userName, userUUID)
                    }
                    updateOperation(operation)
                },
                openLink = openLink
            )
        }
        is LocalLoginOperation.Create -> {
            onCreateLocalAccount(localLoginOperation.userName, localLoginOperation.userUUID)
        }
        is LocalLoginOperation.Alert -> {
            SimpleAlertDialog(
                title = stringResource(R.string.account_supporting_username_invalid_title),
                text = {
                    Text(text = stringResource(R.string.account_supporting_username_invalid_local_message_hint1))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.account_supporting_username_invalid_local_message_hint2),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = stringResource(R.string.account_supporting_username_invalid_local_message_hint3))
                    Text(text = stringResource(R.string.account_supporting_username_invalid_local_message_hint4))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.account_supporting_username_invalid_local_message_hint5),
                        fontWeight = FontWeight.Bold
                    )
                },
                confirmText = stringResource(R.string.account_supporting_username_invalid_still_use),
                onConfirm = {
                    updateOperation(LocalLoginOperation.Create(localLoginOperation.userName, localLoginOperation.userUUID))
                },
                onCancel = {
                    updateOperation(LocalLoginOperation.None)
                }
            )
        }
    }
}

@Composable
private fun OtherLoginOperation(
    otherLoginOperation: OtherLoginOperation,
    updateOperation: (OtherLoginOperation) -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    openLink: (link: String) -> Unit,
    onLoginWithOtherServer: (Context, AuthServer, String, String) -> Unit,
    onFormatError: (Context, Throwable) -> String
) {
    val context = LocalContext.current
    when (otherLoginOperation) {
        is OtherLoginOperation.None -> {}
        is OtherLoginOperation.OnLogin -> {
            OtherServerLoginDialog(
                server = otherLoginOperation.server,
                onRegisterClick = { url ->
                    openLink(url)
                    updateOperation(OtherLoginOperation.None)
                },
                onDismissRequest = { updateOperation(OtherLoginOperation.None) },
                onConfirm = { email, password ->
                    updateOperation(OtherLoginOperation.None)
                    onLoginWithOtherServer(context, otherLoginOperation.server, email, password)
                }
            )
        }
        is OtherLoginOperation.OnFailed -> {
            val message: String = onFormatError(context, otherLoginOperation.th)

            submitError(
                ErrorViewModel.ThrowableMessage(
                    title = stringResource(R.string.account_logging_in_failed),
                    message = message
                )
            )
            updateOperation(OtherLoginOperation.None)
        }
        is OtherLoginOperation.SelectRole -> {
            SimpleListDialog(
                title = stringResource(R.string.account_other_login_select_role),
                items = otherLoginOperation.profiles,
                itemTextProvider = { it.name },
                onItemSelected = { otherLoginOperation.selected(it) },
                onDismissRequest = { updateOperation(OtherLoginOperation.None) }
            )
        }
    }
}

@Composable
private fun ServerTypeOperation(
    serverOperation: ServerOperation,
    updateServerOperation: (ServerOperation) -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    onAddServer: (String) -> Unit,
    onDeleteServer: (AuthServer) -> Unit
) {
    when (serverOperation) {
        is ServerOperation.AddNew -> {
            var serverUrl by rememberSaveable { mutableStateOf("") }
            SimpleEditDialog(
                title = stringResource(R.string.account_add_new_server),
                value = serverUrl,
                onValueChange = { serverUrl = it.trim() },
                label = { Text(text = stringResource(R.string.account_label_server_url)) },
                singleLine = true,
                onDismissRequest = { updateServerOperation(ServerOperation.None) },
                onConfirm = {
                    if (serverUrl.isNotEmpty()) {
                        updateServerOperation(ServerOperation.Add(serverUrl))
                    }
                }
            )
        }
        is ServerOperation.Add -> {
            onAddServer(serverOperation.serverUrl)
        }
        is ServerOperation.Delete -> {
            val server = serverOperation.server
            SimpleAlertDialog(
                title = stringResource(R.string.account_other_login_delete_server_title),
                text = stringResource(
                    R.string.account_other_login_delete_server_message,
                    server.serverName
                ),
                onDismiss = { updateServerOperation(ServerOperation.None) },
                onConfirm = {
                    onDeleteServer(server)
                }
            )
        }
        is ServerOperation.OnThrowable -> {
            submitError(
                ErrorViewModel.ThrowableMessage(
                    title = stringResource(R.string.account_other_login_adding_failure),
                    message = serverOperation.throwable.getMessageOrToString()
                )
            )
            updateServerOperation(ServerOperation.None)
        }
        is ServerOperation.None -> {}
    }
}

@Composable
private fun AccountsLayout(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    accounts: List<Account>,
    currentAccount: Account?,
    accountOperation: AccountOperation,
    accountSkinOperationMap: Map<String, AccountSkinOperation>,
    onUpdateAccountOp: (AccountOperation) -> Unit,
    onUpdateAccountSkinOp: (String, AccountSkinOperation) -> Unit,
    onUpdateMicrosoftSkinOp: (MicrosoftChangeSkinOperation) -> Unit,
    onUpdateMicrosoftCapeOp: (MicrosoftChangeCapeOperation) -> Unit,
    onRefreshAccount: (Context, Account) -> Unit,
    onSaveLocalSkin: (Context, Account, Uri, () -> Unit) -> Unit,
    onResetSkin: (Account, () -> Unit) -> Unit,
    onFormatError: (Context, Throwable) -> String,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    val yOffset by swapAnimateDpAsState(
        targetValue = (-40).dp,
        swapIn = isVisible
    )

    val context = LocalContext.current

    AccountOperation(
        accountOperation = accountOperation,
        updateAccountOperation = onUpdateAccountOp,
        onFormatError = onFormatError,
        submitError = submitError
    )

    BackgroundCard(
        modifier = modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
        shape = MaterialTheme.shapes.extraLarge
    ) {
        if (accounts.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape = MaterialTheme.shapes.extraLarge),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                items(accounts, key = { it.uniqueUUID }) { account ->
                    var refreshAvatar by remember { mutableStateOf(false) }
                    val accountSkinOperation = accountSkinOperationMap[account.uniqueUUID] ?: AccountSkinOperation.None

                    AccountSkinOperation(
                        account = account,
                        accountSkinOperation = accountSkinOperation,
                        updateOperation = { onUpdateAccountSkinOp(account.uniqueUUID, it) },
                        onRefreshAvatar = { refreshAvatar = !refreshAvatar },
                        onSaveLocalSkin = onSaveLocalSkin,
                        onResetSkin = onResetSkin
                    )

                    val skinPicker = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.OpenDocument()
                    ) { uri ->
                        uri?.let { result ->
                            when {
                                account.isLocalAccount() -> {
                                    onUpdateAccountSkinOp(account.uniqueUUID, AccountSkinOperation.SelectSkinModel(result))
                                }
                                account.isMicrosoftAccount() -> {
                                    onUpdateMicrosoftSkinOp(MicrosoftChangeSkinOperation.ImportFile(account, result))
                                }
                            }
                        }
                    }

                    AccountItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        currentAccount = currentAccount,
                        account = account,
                        refreshKey = refreshAvatar,
                        onSelected = { acc ->
                            AccountsManager.setCurrentAccount(acc)
                        },
                        onChangeSkin = {
                            if (!account.isAuthServerAccount()) {
                                skinPicker.launch(arrayOf("image/png"))
                            }
                        },
                        onChangeCape = {
                            if (account.isMicrosoftAccount()) {
                                onUpdateMicrosoftCapeOp(MicrosoftChangeCapeOperation.FetchProfiles(account))
                            }
                        },
                        onResetSkin = {
                            onUpdateAccountSkinOp(account.uniqueUUID, AccountSkinOperation.PreResetSkin)
                        },
                        onRefreshClick = {
                            onRefreshAccount(context, account)
                        },
                        onCopyUUID = {
                            copyText(
                                label = COPY_LABEL_ACCOUNT_UUID,
                                text = account.profileId,
                                context = context,
                                showToast = false
                            )
                            Toast.makeText(
                                context,
                                context.getString(R.string.account_local_uuid_copied, account.username),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onDeleteClick = { onUpdateAccountOp(AccountOperation.Delete(account)) }
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                ScalingLabel(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(R.string.account_no_account)
                )
            }
        }
    }
}

@Composable
private fun AccountSkinOperation(
    account: Account,
    accountSkinOperation: AccountSkinOperation,
    updateOperation: (AccountSkinOperation) -> Unit,
    onRefreshAvatar: () -> Unit,
    onSaveLocalSkin: (Context, Account, Uri, () -> Unit) -> Unit,
    onResetSkin: (Account, () -> Unit) -> Unit
) {
    val context = LocalContext.current
    when (accountSkinOperation) {
        is AccountSkinOperation.None -> {}
        is AccountSkinOperation.SaveSkin -> {
            onSaveLocalSkin(context, account, accountSkinOperation.uri, onRefreshAvatar)
        }
        is AccountSkinOperation.SelectSkinModel -> {
            SelectSkinModelDialog(
                onDismissRequest = {
                    updateOperation(AccountSkinOperation.None)
                },
                onSelected = { type ->
                    account.skinModelType = type
                    account.profileId = com.movtery.zalithlauncher.game.account.wardrobe.getLocalUUIDWithSkinModel(account.username, type)
                    updateOperation(AccountSkinOperation.SaveSkin(accountSkinOperation.uri))
                }
            )
        }
        is AccountSkinOperation.PreResetSkin -> {
            SimpleAlertDialog(
                title = stringResource(R.string.generic_reset),
                text = stringResource(R.string.account_change_skin_reset_skin_message),
                onDismiss = { updateOperation(AccountSkinOperation.None) },
                onConfirm = { updateOperation(AccountSkinOperation.ResetSkin) }
            )
        }
        is AccountSkinOperation.ResetSkin -> {
            onResetSkin(account, onRefreshAvatar)
        }
    }
}

@Composable
private fun AccountOperation(
    accountOperation: AccountOperation,
    updateAccountOperation: (AccountOperation) -> Unit,
    onFormatError: (Context, Throwable) -> String,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    val context = LocalContext.current
    when (accountOperation) {
        is AccountOperation.Delete -> {
            SimpleAlertDialog(
                title = stringResource(R.string.account_delete_title),
                text = stringResource(R.string.account_delete_message,
                    accountOperation.account.username),
                onConfirm = {
                    AccountsManager.deleteAccount(accountOperation.account)
                    updateAccountOperation(AccountOperation.None)
                },
                onDismiss = { updateAccountOperation(AccountOperation.None) }
            )
        }
        is AccountOperation.OnFailed -> {
            val message: String = onFormatError(context, accountOperation.th)

            submitError(
                ErrorViewModel.ThrowableMessage(
                    title = stringResource(R.string.account_logging_in_failed),
                    message = message
                )
            )
            updateAccountOperation(AccountOperation.None)
        }
        is AccountOperation.None -> {}
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 480)
@Composable
private fun AccountManageContentPreview() {
    MaterialTheme {
        Surface {
            AccountManageContent(
                isVisible = true,
                accounts = emptyList(),
                currentAccount = null,
                authServers = emptyList(),
                microsoftLoginOperation = MicrosoftLoginOperation.None,
                microsoftChangeSkinOperation = MicrosoftChangeSkinOperation.None,
                microsoftChangeCapeOperation = MicrosoftChangeCapeOperation.None,
                localLoginOperation = LocalLoginOperation.None,
                otherLoginOperation = OtherLoginOperation.None,
                serverOperation = ServerOperation.None,
                accountOperation = AccountOperation.None,
                accountSkinOperationMap = emptyMap(),
                onUpdateMicrosoftLoginOp = {},
                onUpdateLocalLoginOp = {},
                onUpdateOtherLoginOp = {},
                onUpdateServerOp = {},
                onUpdateAccountOp = {},
                onUpdateAccountSkinOp = { _, _ -> },
                onUpdateMicrosoftSkinOp = {},
                onUpdateMicrosoftCapeOp = {},
                onPerformMicrosoftLogin = { _, _, _, _ -> },
                onImportSkinFile = { _, _, _ -> },
                onUploadMicrosoftSkin = { _, _, _, _ -> },
                onFetchMicrosoftCapes = { _, _ -> },
                onApplyMicrosoftCape = { _, _, _, _, _ -> },
                onCreateLocalAccount = { _, _ -> },
                onLoginWithOtherServer = { _, _, _, _ -> },
                onAddServer = {},
                onDeleteServer = {},
                onDeleteAccount = {},
                onRefreshAccount = { _, _ -> },
                onSaveLocalSkin = { _, _, _, _ -> },
                onResetSkin = { _, _ -> },
                onFormatError = { _, _ -> "" },
                openLink = {},
                submitError = {},
                backToMainScreen = {},
                navigateToWeb = {},
                checkIfInWebScreen = { false }
            )
        }
    }
}

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
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.movtery.zalithlauncher.viewmodel.AccountManageUiState
import com.movtery.zalithlauncher.viewmodel.AccountManageViewModel
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.LocalBackgroundViewModel
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel

/**
 * 封装 UI 交互回调，减少参数数量
 */
private data class AccountActions(
    val onUpdateMicrosoftLoginOp: (MicrosoftLoginOperation) -> Unit,
    val onUpdateLocalLoginOp: (LocalLoginOperation) -> Unit,
    val onUpdateOtherLoginOp: (OtherLoginOperation) -> Unit,
    val onUpdateServerOp: (ServerOperation) -> Unit,
    val onUpdateAccountOp: (AccountOperation) -> Unit,
    val onUpdateAccountSkinOp: (String, AccountSkinOperation) -> Unit,
    val onUpdateMicrosoftSkinOp: (MicrosoftChangeSkinOperation) -> Unit,
    val onUpdateMicrosoftCapeOp: (MicrosoftChangeCapeOperation) -> Unit,
    val onPerformMicrosoftLogin: (Context, (String) -> Unit, () -> Unit, () -> Boolean) -> Unit,
    val onImportSkinFile: (Context, Account, Uri) -> Unit,
    val onUploadMicrosoftSkin: (Context, Account, java.io.File, com.movtery.zalithlauncher.game.account.wardrobe.SkinModelType) -> Unit,
    val onFetchMicrosoftCapes: (Context, Account) -> Unit,
    val onApplyMicrosoftCape: (Context, Account, String?, String, Boolean) -> Unit,
    val onCreateLocalAccount: (String, String?) -> Unit,
    val onLoginWithOtherServer: (Context, AuthServer, String, String) -> Unit,
    val onAddServer: (String) -> Unit,
    val onDeleteServer: (AuthServer) -> Unit,
    val onDeleteAccount: (Account) -> Unit,
    val onRefreshAccount: (Context, Account) -> Unit,
    val onSaveLocalSkin: (Context, Account, Uri, () -> Unit) -> Unit,
    val onResetSkin: (Account, () -> Unit) -> Unit,
    val onFormatError: (Context, Throwable) -> String,
    val openLink: (url: String) -> Unit,
    val submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    val backToMainScreen: () -> Unit,
    val navigateToWeb: (url: String) -> Unit,
    val checkIfInWebScreen: () -> Boolean
)

@Composable
fun AccountManageScreen(
    backStackViewModel: ScreenBackStackViewModel,
    backToMainScreen: () -> Unit,
    openLink: (url: String) -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    viewModel: AccountManageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val actions = remember(viewModel, backToMainScreen, openLink, submitError) {
        AccountActions(
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

    BaseScreen(
        screenKey = NormalNavKey.AccountManager,
        currentKey = backStackViewModel.mainScreen.currentKey
    ) { isVisible ->
        AccountManageContent(
            isVisible = isVisible,
            uiState = uiState,
            actions = actions
        )
    }
}

@Composable
private fun AccountManageContent(
    isVisible: Boolean,
    uiState: AccountManageUiState,
    actions: AccountActions
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
            authServers = uiState.authServers,
            actions = actions
        )
        AccountsLayout(
            isVisible = isVisible,
            modifier = Modifier
                .fillMaxHeight()
                .padding(top = 12.dp, end = 12.dp, bottom = 12.dp)
                .weight(7f),
            accounts = uiState.accounts,
            currentAccount = uiState.currentAccount,
            accountOperation = uiState.accountOperation,
            accountSkinOperationMap = uiState.accountSkinOperationMap,
            actions = actions
        )
    }

    // 操作逻辑组件
    MicrosoftLoginOperation(uiState.microsoftLoginOperation, actions)
    MicrosoftChangeSkinOperation(uiState.microsoftChangeSkinOperation, actions)
    MicrosoftChangeCapeOperation(uiState.microsoftChangeCapeOperation, actions)
    LocalLoginOperation(uiState.localLoginOperation, actions)
    OtherLoginOperation(uiState.otherLoginOperation, actions)
    ServerTypeOperation(uiState.serverOperation, actions)
}

@Composable
private fun ServerTypeMenu(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    authServers: List<AuthServer>,
    actions: AccountActions
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
                    actions.onUpdateMicrosoftLoginOp(MicrosoftLoginOperation.Tip)
                }
            }
            LoginItem(
                modifier = Modifier.fillMaxWidth(),
                serverName = stringResource(R.string.account_type_local)
            ) {
                actions.onUpdateLocalLoginOp(LocalLoginOperation.Edit)
            }

            authServers.forEach { server ->
                ServerItem(
                    server = server,
                    onClick = { actions.onUpdateOtherLoginOp(OtherLoginOperation.OnLogin(server)) },
                    onDeleteClick = { actions.onUpdateServerOp(ServerOperation.Delete(server)) }
                )
            }
        }

        ScalingActionButton(
            modifier = Modifier
                .padding(PaddingValues(horizontal = 12.dp, vertical = 8.dp))
                .fillMaxWidth(),
            onClick = { actions.onUpdateServerOp(ServerOperation.AddNew) }
        ) {
            MarqueeText(text = stringResource(R.string.account_add_new_server_button))
        }
    }
}

@Composable
private fun MicrosoftLoginOperation(
    operation: MicrosoftLoginOperation,
    actions: AccountActions
) {
    val context = LocalContext.current
    when (operation) {
        is MicrosoftLoginOperation.None -> {}
        is MicrosoftLoginOperation.Tip -> {
            MicrosoftLoginTipDialog(
                onDismissRequest = { actions.onUpdateMicrosoftLoginOp(MicrosoftLoginOperation.None) },
                onConfirm = { actions.onUpdateMicrosoftLoginOp(MicrosoftLoginOperation.RunTask) },
                openLink = actions.openLink
            )
        }
        is MicrosoftLoginOperation.RunTask -> {
            actions.onPerformMicrosoftLogin(context, actions.navigateToWeb, actions.backToMainScreen, actions.checkIfInWebScreen)
        }
    }
}

@Composable
private fun MicrosoftChangeSkinOperation(
    operation: MicrosoftChangeSkinOperation,
    actions: AccountActions
) {
    val context = LocalContext.current
    when (operation) {
        is MicrosoftChangeSkinOperation.None -> {}
        is MicrosoftChangeSkinOperation.ImportFile -> {
            actions.onImportSkinFile(context, operation.account, operation.uri)
        }
        is MicrosoftChangeSkinOperation.SelectSkinModel -> {
            SelectSkinModelDialog(
                onDismissRequest = { actions.onUpdateMicrosoftSkinOp(MicrosoftChangeSkinOperation.None) },
                onSelected = { modelType ->
                    actions.onUpdateMicrosoftSkinOp(
                        MicrosoftChangeSkinOperation.RunTask(operation.account, operation.file, modelType)
                    )
                }
            )
        }
        is MicrosoftChangeSkinOperation.RunTask -> {
            actions.onUploadMicrosoftSkin(context, operation.account, operation.file, operation.skinModel)
        }
    }
}

@Composable
private fun MicrosoftChangeCapeOperation(
    operation: MicrosoftChangeCapeOperation,
    actions: AccountActions
) {
    val context = LocalContext.current
    when (operation) {
        is MicrosoftChangeCapeOperation.None -> {}
        is MicrosoftChangeCapeOperation.FetchProfiles -> {
            actions.onFetchMicrosoftCapes(context, operation.account)
        }
        is MicrosoftChangeCapeOperation.SelectCape -> {
            val account = operation.account
            val profile = operation.profile
            val capes = remember(profile.capes) { listOf(EmptyCape) + profile.capes }

            SelectCapeDialog(
                capes = capes,
                onSelected = { cape -> actions.onUpdateMicrosoftCapeOp(MicrosoftChangeCapeOperation.RunTask(account, cape)) },
                onDismiss = { actions.onUpdateMicrosoftCapeOp(MicrosoftChangeCapeOperation.None) }
            )
        }
        is MicrosoftChangeCapeOperation.RunTask -> {
            val capeId: String? = operation.cape.takeIf { it != EmptyCape }?.id
            actions.onApplyMicrosoftCape(context, operation.account, capeId, operation.cape.capeTranslatedName(), operation.cape == EmptyCape)
        }
    }
}

@Composable
private fun LocalLoginOperation(
    operation: LocalLoginOperation,
    actions: AccountActions
) {
    when (operation) {
        is LocalLoginOperation.None -> {}
        is LocalLoginOperation.Edit -> {
            LocalLoginDialog(
                onDismissRequest = { actions.onUpdateLocalLoginOp(LocalLoginOperation.None) },
                onConfirm = { isInvalid, name, uuid ->
                    val nextOp = if (isInvalid) LocalLoginOperation.Alert(name, uuid) else LocalLoginOperation.Create(name, uuid)
                    actions.onUpdateLocalLoginOp(nextOp)
                },
                openLink = actions.openLink
            )
        }
        is LocalLoginOperation.Create -> actions.onCreateLocalAccount(operation.userName, operation.userUUID)
        is LocalLoginOperation.Alert -> {
            SimpleAlertDialog(
                title = stringResource(R.string.account_supporting_username_invalid_title),
                text = {
                    Column {
                        Text(text = stringResource(R.string.account_supporting_username_invalid_local_message_hint1))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = stringResource(R.string.account_supporting_username_invalid_local_message_hint2), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = stringResource(R.string.account_supporting_username_invalid_local_message_hint3))
                        Text(text = stringResource(R.string.account_supporting_username_invalid_local_message_hint4))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = stringResource(R.string.account_supporting_username_invalid_local_message_hint5), fontWeight = FontWeight.Bold)
                    }
                },
                confirmText = stringResource(R.string.account_supporting_username_invalid_still_use),
                onConfirm = { actions.onUpdateLocalLoginOp(LocalLoginOperation.Create(operation.userName, operation.userUUID)) },
                onCancel = { actions.onUpdateLocalLoginOp(LocalLoginOperation.None) }
            )
        }
    }
}

@Composable
private fun OtherLoginOperation(
    operation: OtherLoginOperation,
    actions: AccountActions
) {
    val context = LocalContext.current
    when (operation) {
        is OtherLoginOperation.None -> {}
        is OtherLoginOperation.OnLogin -> {
            OtherServerLoginDialog(
                server = operation.server,
                onRegisterClick = { url ->
                    actions.openLink(url)
                    actions.onUpdateOtherLoginOp(OtherLoginOperation.None)
                },
                onDismissRequest = { actions.onUpdateOtherLoginOp(OtherLoginOperation.None) },
                onConfirm = { email, password ->
                    actions.onUpdateOtherLoginOp(OtherLoginOperation.None)
                    actions.onLoginWithOtherServer(context, operation.server, email, password)
                }
            )
        }
        is OtherLoginOperation.OnFailed -> {
            val message = actions.onFormatError(context, operation.th)
            actions.submitError(ErrorViewModel.ThrowableMessage(stringResource(R.string.account_logging_in_failed), message))
            actions.onUpdateOtherLoginOp(OtherLoginOperation.None)
        }
        is OtherLoginOperation.SelectRole -> {
            SimpleListDialog(
                title = stringResource(R.string.account_other_login_select_role),
                items = operation.profiles,
                itemTextProvider = { it.name },
                onItemSelected = { operation.selected(it) },
                onDismissRequest = { actions.onUpdateOtherLoginOp(OtherLoginOperation.None) }
            )
        }
    }
}

@Composable
private fun ServerTypeOperation(
    operation: ServerOperation,
    actions: AccountActions
) {
    when (operation) {
        is ServerOperation.AddNew -> {
            var serverUrl by rememberSaveable { mutableStateOf("") }
            SimpleEditDialog(
                title = stringResource(R.string.account_add_new_server),
                value = serverUrl,
                onValueChange = { serverUrl = it.trim() },
                label = { Text(text = stringResource(R.string.account_label_server_url)) },
                singleLine = true,
                onDismissRequest = { actions.onUpdateServerOp(ServerOperation.None) },
                onConfirm = { if (serverUrl.isNotEmpty()) actions.onUpdateServerOp(ServerOperation.Add(serverUrl)) }
            )
        }
        is ServerOperation.Add -> actions.onAddServer(operation.serverUrl)
        is ServerOperation.Delete -> {
            SimpleAlertDialog(
                title = stringResource(R.string.account_other_login_delete_server_title),
                text = stringResource(R.string.account_other_login_delete_server_message, operation.server.serverName),
                onDismiss = { actions.onUpdateServerOp(ServerOperation.None) },
                onConfirm = { actions.onDeleteServer(operation.server) }
            )
        }
        is ServerOperation.OnThrowable -> {
            actions.submitError(ErrorViewModel.ThrowableMessage(stringResource(R.string.account_other_login_adding_failure), operation.throwable.getMessageOrToString()))
            actions.onUpdateServerOp(ServerOperation.None)
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
    actions: AccountActions
) {
    val yOffset by swapAnimateDpAsState(targetValue = (-40).dp, swapIn = isVisible)
    val context = LocalContext.current

    AccountOperation(accountOperation, actions)

    BackgroundCard(
        modifier = modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
        shape = MaterialTheme.shapes.extraLarge
    ) {
        if (accounts.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().clip(MaterialTheme.shapes.extraLarge),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                items(accounts, key = { it.uniqueUUID }) { account ->
                    var refreshAvatar by remember { mutableStateOf(false) }
                    val skinOp = accountSkinOperationMap[account.uniqueUUID] ?: AccountSkinOperation.None

                    AccountSkinOperation(
                        account = account,
                        accountSkinOperation = skinOp,
                        updateOperation = { actions.onUpdateAccountSkinOp(account.uniqueUUID, it) },
                        onRefreshAvatar = { refreshAvatar = !refreshAvatar },
                        actions = actions
                    )

                    val skinPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                        uri?.let {
                            if (account.isLocalAccount()) actions.onUpdateAccountSkinOp(account.uniqueUUID, AccountSkinOperation.SelectSkinModel(it))
                            else if (account.isMicrosoftAccount()) actions.onUpdateMicrosoftSkinOp(MicrosoftChangeSkinOperation.ImportFile(account, it))
                        }
                    }

                    AccountItem(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        currentAccount = currentAccount,
                        account = account,
                        refreshKey = refreshAvatar,
                        onSelected = { AccountsManager.setCurrentAccount(it) },
                        onChangeSkin = { if (!account.isAuthServerAccount()) skinPicker.launch(arrayOf("image/png")) },
                        onChangeCape = { if (account.isMicrosoftAccount()) actions.onUpdateMicrosoftCapeOp(MicrosoftChangeCapeOperation.FetchProfiles(account)) },
                        onResetSkin = { actions.onUpdateAccountSkinOp(account.uniqueUUID, AccountSkinOperation.PreResetSkin) },
                        onRefreshClick = { actions.onRefreshAccount(context, account) },
                        onCopyUUID = {
                            copyText(COPY_LABEL_ACCOUNT_UUID, account.profileId, context, false)
                            Toast.makeText(context, context.getString(R.string.account_local_uuid_copied, account.username), Toast.LENGTH_SHORT).show()
                        },
                        onDeleteClick = { actions.onUpdateAccountOp(AccountOperation.Delete(account)) }
                    )
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                ScalingLabel(modifier = Modifier.align(Alignment.Center), text = stringResource(R.string.account_no_account))
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
    actions: AccountActions
) {
    val context = LocalContext.current
    when (accountSkinOperation) {
        is AccountSkinOperation.None -> {}
        is AccountSkinOperation.SaveSkin -> actions.onSaveLocalSkin(context, account, accountSkinOperation.uri, onRefreshAvatar)
        is AccountSkinOperation.SelectSkinModel -> {
            SelectSkinModelDialog(
                onDismissRequest = { updateOperation(AccountSkinOperation.None) },
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
        is AccountSkinOperation.ResetSkin -> actions.onResetSkin(account, onRefreshAvatar)
    }
}

@Composable
private fun AccountOperation(
    operation: AccountOperation,
    actions: AccountActions
) {
    val context = LocalContext.current
    when (operation) {
        is AccountOperation.Delete -> {
            SimpleAlertDialog(
                title = stringResource(R.string.account_delete_title),
                text = stringResource(R.string.account_delete_message, operation.account.username),
                onConfirm = { actions.onDeleteAccount(operation.account) },
                onDismiss = { actions.onUpdateAccountOp(AccountOperation.None) }
            )
        }
        is AccountOperation.OnFailed -> {
            val message = actions.onFormatError(context, operation.th)
            actions.submitError(ErrorViewModel.ThrowableMessage(stringResource(R.string.account_logging_in_failed), message))
            actions.onUpdateAccountOp(AccountOperation.None)
        }
        is AccountOperation.None -> {}
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 480)
@Composable
private fun AccountManageContentPreview() {
    CompositionLocalProvider(LocalBackgroundViewModel provides null) {
        MaterialTheme {
            Surface {
                AccountManageContent(
                    isVisible = true,
                    uiState = AccountManageUiState(),
                    actions = AccountActions(
                        onUpdateMicrosoftLoginOp = {}, onUpdateLocalLoginOp = {}, onUpdateOtherLoginOp = {},
                        onUpdateServerOp = {}, onUpdateAccountOp = {}, onUpdateAccountSkinOp = { _, _ -> },
                        onUpdateMicrosoftSkinOp = {}, onUpdateMicrosoftCapeOp = {}, onPerformMicrosoftLogin = { _, _, _, _ -> },
                        onImportSkinFile = { _, _, _ -> }, onUploadMicrosoftSkin = { _, _, _, _ -> },
                        onFetchMicrosoftCapes = { _, _ -> }, onApplyMicrosoftCape = { _, _, _, _, _ -> },
                        onCreateLocalAccount = { _, _ -> }, onLoginWithOtherServer = { _, _, _, _ -> },
                        onAddServer = {}, onDeleteServer = {}, onDeleteAccount = {},
                        onRefreshAccount = { _, _ -> }, onSaveLocalSkin = { _, _, _, _ -> },
                        onResetSkin = { _, _ -> }, onFormatError = { _, _ -> "" },
                        openLink = {}, submitError = {}, backToMainScreen = {},
                        navigateToWeb = {}, checkIfInWebScreen = { false }
                    )
                )
            }
        }
    }
}

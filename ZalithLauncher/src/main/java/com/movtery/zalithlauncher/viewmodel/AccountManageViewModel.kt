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

package com.movtery.zalithlauncher.viewmodel

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.context.copyLocalFile
import com.movtery.zalithlauncher.context.getFileName
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.addOtherServer
import com.movtery.zalithlauncher.game.account.auth_server.AuthServerHelper
import com.movtery.zalithlauncher.game.account.auth_server.data.AuthServer
import com.movtery.zalithlauncher.game.account.localLogin
import com.movtery.zalithlauncher.game.account.microsoft.MINECRAFT_SERVICES_URL
import com.movtery.zalithlauncher.game.account.microsoft.MinecraftProfileException
import com.movtery.zalithlauncher.game.account.microsoft.NotPurchasedMinecraftException
import com.movtery.zalithlauncher.game.account.microsoft.XboxLoginException
import com.movtery.zalithlauncher.game.account.microsoft.toLocal
import com.movtery.zalithlauncher.game.account.microsoftLogin
import com.movtery.zalithlauncher.game.account.refreshMicrosoft
import com.movtery.zalithlauncher.game.account.wardrobe.SkinModelType
import com.movtery.zalithlauncher.game.account.wardrobe.getLocalUUIDWithSkinModel
import com.movtery.zalithlauncher.game.account.wardrobe.validateSkinFile
import com.movtery.zalithlauncher.game.account.yggdrasil.cacheAllCapes
import com.movtery.zalithlauncher.game.account.yggdrasil.changeCape
import com.movtery.zalithlauncher.game.account.yggdrasil.executeWithAuthorization
import com.movtery.zalithlauncher.game.account.yggdrasil.getPlayerProfile
import com.movtery.zalithlauncher.game.account.yggdrasil.uploadSkin
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.ui.screens.content.elements.AccountOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.AccountSkinOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.LocalLoginOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.MicrosoftChangeCapeOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.MicrosoftChangeSkinOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.MicrosoftLoginOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.OtherLoginOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.ServerOperation
import com.movtery.zalithlauncher.utils.logging.Logger.lError
import com.movtery.zalithlauncher.utils.network.safeBodyAsJson
import com.movtery.zalithlauncher.utils.string.getMessageOrToString
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.apache.commons.io.FileUtils
import java.io.File
import java.net.ConnectException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException
import java.util.UUID
import javax.inject.Inject

/**
 * 封装 UI 状态，符合单一数据源原则
 */
data class AccountManageUiState(
    val accounts: List<Account> = emptyList(),
    val currentAccount: Account? = null,
    val authServers: List<AuthServer> = emptyList(),
    val microsoftLoginOperation: MicrosoftLoginOperation = MicrosoftLoginOperation.None,
    val microsoftChangeSkinOperation: MicrosoftChangeSkinOperation = MicrosoftChangeSkinOperation.None,
    val microsoftChangeCapeOperation: MicrosoftChangeCapeOperation = MicrosoftChangeCapeOperation.None,
    val localLoginOperation: LocalLoginOperation = LocalLoginOperation.None,
    val otherLoginOperation: OtherLoginOperation = OtherLoginOperation.None,
    val serverOperation: ServerOperation = ServerOperation.None,
    val accountOperation: AccountOperation = AccountOperation.None,
    val accountSkinOperationMap: Map<String, AccountSkinOperation> = emptyMap()
)

@HiltViewModel
class AccountManageViewModel @Inject constructor() : ViewModel() {

    private val _microsoftLoginOp = MutableStateFlow<MicrosoftLoginOperation>(MicrosoftLoginOperation.None)
    private val _microsoftSkinOp = MutableStateFlow<MicrosoftChangeSkinOperation>(MicrosoftChangeSkinOperation.None)
    private val _microsoftCapeOp = MutableStateFlow<MicrosoftChangeCapeOperation>(MicrosoftChangeCapeOperation.None)
    private val _localLoginOp = MutableStateFlow<LocalLoginOperation>(LocalLoginOperation.None)
    private val _otherLoginOp = MutableStateFlow<OtherLoginOperation>(OtherLoginOperation.None)
    private val _serverOp = MutableStateFlow<ServerOperation>(ServerOperation.None)
    private val _accountOp = MutableStateFlow<AccountOperation>(AccountOperation.None)
    private val _accountSkinOpMap = MutableStateFlow<Map<String, AccountSkinOperation>>(emptyMap())

    val uiState: StateFlow<AccountManageUiState> = combine(
        AccountsManager.accountsFlow,
        AccountsManager.currentAccountFlow,
        AccountsManager.authServersFlow,
        _microsoftLoginOp,
        _microsoftSkinOp,
        _microsoftCapeOp,
        _localLoginOp,
        _otherLoginOp,
        _serverOp,
        _accountOp,
        _accountSkinOpMap
    ) { args ->
        AccountManageUiState(
            accounts = args[0] as List<Account>,
            currentAccount = args[1] as Account?,
            authServers = args[2] as List<AuthServer>,
            microsoftLoginOperation = args[3] as MicrosoftLoginOperation,
            microsoftChangeSkinOperation = args[4] as MicrosoftChangeSkinOperation,
            microsoftChangeCapeOperation = args[5] as MicrosoftChangeCapeOperation,
            localLoginOperation = args[6] as LocalLoginOperation,
            otherLoginOperation = args[7] as OtherLoginOperation,
            serverOperation = args[8] as ServerOperation,
            accountOperation = args[9] as AccountOperation,
            accountSkinOperationMap = args[10] as Map<String, AccountSkinOperation>
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AccountManageUiState()
    )

    fun updateMicrosoftLoginOperation(operation: MicrosoftLoginOperation) { _microsoftLoginOp.value = operation }
    fun updateMicrosoftChangeSkinOperation(operation: MicrosoftChangeSkinOperation) { _microsoftSkinOp.value = operation }
    fun updateMicrosoftChangeCapeOperation(operation: MicrosoftChangeCapeOperation) { _microsoftCapeOp.value = operation }
    fun updateLocalLoginOperation(operation: LocalLoginOperation) { _localLoginOp.value = operation }
    fun updateOtherLoginOperation(operation: OtherLoginOperation) { _otherLoginOp.value = operation }
    fun updateServerOperation(operation: ServerOperation) { _serverOp.value = operation }
    fun updateAccountOperation(operation: AccountOperation) { _accountOp.value = operation }
    fun updateAccountSkinOperation(accountUuid: String, operation: AccountSkinOperation) {
        _accountSkinOpMap.update { it + (accountUuid to operation) }
    }

    // --- 业务方法 ---

    fun performMicrosoftLogin(
        context: Context,
        toWeb: (url: String) -> Unit,
        backToMain: () -> Unit,
        checkIfInWebScreen: () -> Boolean,
        submitError: (ErrorViewModel.ThrowableMessage) -> Unit
    ) {
        microsoftLogin(context, toWeb, backToMain, checkIfInWebScreen, { updateMicrosoftLoginOperation(it) }, submitError)
        updateMicrosoftLoginOperation(MicrosoftLoginOperation.None)
    }

    fun importSkinFile(context: Context, account: Account, uri: Uri, submitError: (ErrorViewModel.ThrowableMessage) -> Unit) {
        val fileName = context.getFileName(uri) ?: UUID.randomUUID().toString().replace("-", "")
        val cacheFile = File(PathManager.DIR_IMAGE_CACHE, fileName)
        TaskSystem.submitTask(Task.runTask(id = account.uniqueUUID, dispatcher = Dispatchers.IO, task = {
            context.copyLocalFile(uri, cacheFile)
            if (validateSkinFile(cacheFile)) updateMicrosoftChangeSkinOperation(MicrosoftChangeSkinOperation.SelectSkinModel(account, cacheFile))
            else {
                submitError(ErrorViewModel.ThrowableMessage(context.getString(R.string.generic_warning), context.getString(R.string.account_change_skin_invalid)))
                updateMicrosoftChangeSkinOperation(MicrosoftChangeSkinOperation.None)
            }
        }, onError = { th ->
            submitError(ErrorViewModel.ThrowableMessage(context.getString(R.string.generic_error), context.getString(R.string.account_change_skin_failed_to_import) + "\r\n" + th.getMessageOrToString()))
            updateMicrosoftChangeSkinOperation(MicrosoftChangeSkinOperation.None)
        }, onCancel = { updateMicrosoftChangeSkinOperation(MicrosoftChangeSkinOperation.None) }))
    }

    fun uploadMicrosoftSkin(context: Context, account: Account, skinFile: File, skinModel: SkinModelType, submitError: (ErrorViewModel.ThrowableMessage) -> Unit) {
        TaskSystem.submitTask(Task.runTask(dispatcher = Dispatchers.IO, task = { task ->
            executeWithAuthorization(block = {
                task.updateProgress(-1f, R.string.account_change_skin_uploading)
                uploadSkin(MINECRAFT_SERVICES_URL, account.accessToken, skinFile, skinModel)
            }, onRefreshRequest = {
                account.refreshMicrosoft(task = task, coroutineContext = coroutineContext)
                AccountsManager.suspendSaveAccount(account)
            })
            task.updateMessage(R.string.account_change_skin_update_local)
            runCatching { account.downloadSkin() }.onFailure { th ->
                submitError(ErrorViewModel.ThrowableMessage(context.getString(R.string.account_logging_in_failed), formatAccountError(context, th)))
            }
            withContext(Dispatchers.Main) { Toast.makeText(context, context.getString(R.string.account_change_skin_update_toast), Toast.LENGTH_LONG).show() }
            updateMicrosoftChangeSkinOperation(MicrosoftChangeSkinOperation.None)
        }, onError = { th ->
            val (title, msg) = if (th is io.ktor.client.plugins.ResponseException) {
                val body = th.response.safeBodyAsJson<JsonObject>()
                context.getString(R.string.account_change_skin_failed_to_upload, th.response.status.value) to (body["errorMessage"]?.jsonPrimitive?.contentOrNull ?: th.getMessageOrToString())
            } else context.getString(R.string.generic_error) to formatAccountError(context, th)
            submitError(ErrorViewModel.ThrowableMessage(title, msg))
            updateMicrosoftChangeSkinOperation(MicrosoftChangeSkinOperation.None)
        }, onCancel = { updateMicrosoftChangeSkinOperation(MicrosoftChangeSkinOperation.None) }))
    }

    fun fetchMicrosoftCapes(context: Context, account: Account, submitError: (ErrorViewModel.ThrowableMessage) -> Unit) {
        TaskSystem.submitTask(Task.runTask(id = account.uniqueUUID, dispatcher = Dispatchers.IO, task = { task ->
            executeWithAuthorization(block = {
                task.updateProgress(-1f, R.string.account_change_cape_fetch_all)
                val profile = getPlayerProfile(MINECRAFT_SERVICES_URL, account.accessToken)
                task.updateProgress(-1f, R.string.account_change_cape_cache_all)
                cacheAllCapes(profile)
                updateMicrosoftChangeCapeOperation(MicrosoftChangeCapeOperation.SelectCape(account, profile))
            }, onRefreshRequest = {
                account.refreshMicrosoft(task = task, coroutineContext = coroutineContext)
                AccountsManager.suspendSaveAccount(account)
            })
        }, onError = { th ->
            submitError(ErrorViewModel.ThrowableMessage(context.getString(R.string.generic_error), context.getString(R.string.account_change_cape_fetch_all_failed) + "\r\n" + th.getMessageOrToString()))
            updateMicrosoftChangeCapeOperation(MicrosoftChangeCapeOperation.None)
        }, onCancel = { updateMicrosoftChangeCapeOperation(MicrosoftChangeCapeOperation.None) }))
    }

    fun applyMicrosoftCape(context: Context, account: Account, capeId: String?, capeName: String, isReset: Boolean, submitError: (ErrorViewModel.ThrowableMessage) -> Unit) {
        TaskSystem.submitTask(Task.runTask(dispatcher = Dispatchers.IO, task = { task ->
            executeWithAuthorization(block = {
                task.updateMessage(R.string.account_change_cape_apply)
                changeCape(MINECRAFT_SERVICES_URL, account.accessToken, capeId)
            }, onRefreshRequest = {
                account.refreshMicrosoft(task = task, coroutineContext = coroutineContext)
                AccountsManager.suspendSaveAccount(account)
            })
            withContext(Dispatchers.Main) {
                val text = if (isReset) context.getString(R.string.account_change_cape_apply_reset) else context.getString(R.string.account_change_cape_apply_success, capeName)
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
            }
            updateMicrosoftChangeCapeOperation(MicrosoftChangeCapeOperation.None)
        }, onError = { th ->
            val (title, msg) = if (th is io.ktor.client.plugins.ResponseException) {
                val body = th.response.safeBodyAsJson<JsonObject>()
                context.getString(R.string.account_change_cape_apply_failed, th.response.status.value) to (body["errorMessage"]?.jsonPrimitive?.contentOrNull ?: th.getMessageOrToString())
            } else context.getString(R.string.generic_error) to formatAccountError(context, th)
            submitError(ErrorViewModel.ThrowableMessage(title, msg))
            updateMicrosoftChangeCapeOperation(MicrosoftChangeCapeOperation.None)
        }, onCancel = { updateMicrosoftChangeCapeOperation(MicrosoftChangeCapeOperation.None) }))
    }

    fun createLocalAccount(userName: String, userUUID: String?) { localLogin(userName, userUUID); updateLocalLoginOperation(LocalLoginOperation.None) }

    fun loginWithOtherServer(context: Context, server: AuthServer, email: String, pass: String, submitError: (ErrorViewModel.ThrowableMessage) -> Unit) {
        AuthServerHelper(server, email, pass, onSuccess = { account, task -> task.updateMessage(R.string.account_logging_in_saving); account.downloadSkin(); AccountsManager.suspendSaveAccount(account) },
            onFailed = { updateOtherLoginOperation(OtherLoginOperation.OnFailed(it)) }).createNewAccount(context) { profiles, select -> updateOtherLoginOperation(OtherLoginOperation.SelectRole(profiles, select)) }
    }

    fun addServer(url: String) { addOtherServer(url) { updateServerOperation(ServerOperation.OnThrowable(it)) }; updateServerOperation(ServerOperation.None) }
    fun deleteServer(server: AuthServer) { AccountsManager.deleteAuthServer(server); updateServerOperation(ServerOperation.None) }
    fun deleteAccount(account: Account) { AccountsManager.deleteAccount(account); updateAccountOperation(AccountOperation.None) }
    fun refreshAccount(context: Context, account: Account) { AccountsManager.refreshAccount(context, account) { updateAccountOperation(AccountOperation.OnFailed(it)) } }

    fun saveLocalSkin(context: Context, account: Account, uri: Uri, onRefresh: () -> Unit, submitError: (ErrorViewModel.ThrowableMessage) -> Unit) {
        val skinFile = account.getSkinFile()
        val cacheFile = File(PathManager.DIR_IMAGE_CACHE, skinFile.name)
        TaskSystem.submitTask(Task.runTask(dispatcher = Dispatchers.IO, task = {
            context.copyLocalFile(uri, cacheFile)
            if (validateSkinFile(cacheFile)) { cacheFile.copyTo(skinFile, true); FileUtils.deleteQuietly(cacheFile); AccountsManager.suspendSaveAccount(account); onRefresh(); updateAccountSkinOperation(account.uniqueUUID, AccountSkinOperation.None) }
            else { submitError(ErrorViewModel.ThrowableMessage(context.getString(R.string.generic_warning), context.getString(R.string.account_change_skin_invalid))); updateAccountSkinOperation(account.uniqueUUID, AccountSkinOperation.None) }
        }, onError = { th -> FileUtils.deleteQuietly(cacheFile); submitError(ErrorViewModel.ThrowableMessage(context.getString(R.string.error_import_image), th.getMessageOrToString())); onRefresh(); updateAccountSkinOperation(account.uniqueUUID, AccountSkinOperation.None) }))
    }

    fun resetSkin(account: Account, onRefresh: () -> Unit) {
        TaskSystem.submitTask(Task.runTask(dispatcher = Dispatchers.IO, task = {
            account.apply { FileUtils.deleteQuietly(getSkinFile()); skinModelType = SkinModelType.NONE; profileId = getLocalUUIDWithSkinModel(username, skinModelType); AccountsManager.suspendSaveAccount(this); onRefresh() }
        }))
        updateAccountSkinOperation(account.uniqueUUID, AccountSkinOperation.None)
    }

    fun formatAccountError(context: Context, th: Throwable): String = when (th) {
        is NotPurchasedMinecraftException -> toLocal(context)
        is MinecraftProfileException -> th.toLocal(context)
        is XboxLoginException -> th.toLocal(context)
        is io.ktor.client.plugins.ResponseException -> {
            val res = when (th.response.status) {
                HttpStatusCode.Unauthorized -> R.string.error_unauthorized
                HttpStatusCode.NotFound -> R.string.error_notfound
                else -> R.string.error_client_error
            }
            context.getString(res, th.response.status.value)
        }
        is HttpRequestTimeoutException -> context.getString(R.string.error_timeout)
        is UnknownHostException, is UnresolvedAddressException -> context.getString(R.string.error_network_unreachable)
        is ConnectException -> context.getString(R.string.error_connection_failed)
        else -> { lError("Exception caught!", th); context.getString(R.string.error_unknown, th.localizedMessage ?: "Unknown") }
    }
}

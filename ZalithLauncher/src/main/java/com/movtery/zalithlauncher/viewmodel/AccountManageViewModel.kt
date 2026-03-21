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
import com.movtery.zalithlauncher.game.account.auth_server.ResponseException
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
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
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

class AccountManageViewModel : ViewModel() {

    private val _microsoftLoginOperation = MutableStateFlow<MicrosoftLoginOperation>(MicrosoftLoginOperation.None)
    val microsoftLoginOperation = _microsoftLoginOperation.asStateFlow()

    private val _microsoftChangeSkinOperation = MutableStateFlow<MicrosoftChangeSkinOperation>(MicrosoftChangeSkinOperation.None)
    val microsoftChangeSkinOperation = _microsoftChangeSkinOperation.asStateFlow()

    private val _microsoftChangeCapeOperation = MutableStateFlow<MicrosoftChangeCapeOperation>(MicrosoftChangeCapeOperation.None)
    val microsoftChangeCapeOperation = _microsoftChangeCapeOperation.asStateFlow()

    private val _localLoginOperation = MutableStateFlow<LocalLoginOperation>(LocalLoginOperation.None)
    val localLoginOperation = _localLoginOperation.asStateFlow()

    private val _otherLoginOperation = MutableStateFlow<OtherLoginOperation>(OtherLoginOperation.None)
    val otherLoginOperation = _otherLoginOperation.asStateFlow()

    private val _serverOperation = MutableStateFlow<ServerOperation>(ServerOperation.None)
    val serverOperation = _serverOperation.asStateFlow()

    private val _accountOperation = MutableStateFlow<AccountOperation>(AccountOperation.None)
    val accountOperation = _accountOperation.asStateFlow()

    private val _accountSkinOperationMap = MutableStateFlow<Map<String, AccountSkinOperation>>(emptyMap())
    val accountSkinOperationMap = _accountSkinOperationMap.asStateFlow()

    val accounts = AccountsManager.accountsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val currentAccount = AccountsManager.currentAccountFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    val authServers = AccountsManager.authServersFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    fun updateMicrosoftLoginOperation(operation: MicrosoftLoginOperation) {
        _microsoftLoginOperation.value = operation
    }

    fun updateMicrosoftChangeSkinOperation(operation: MicrosoftChangeSkinOperation) {
        _microsoftChangeSkinOperation.value = operation
    }

    fun updateMicrosoftChangeCapeOperation(operation: MicrosoftChangeCapeOperation) {
        _microsoftChangeCapeOperation.value = operation
    }

    fun updateLocalLoginOperation(operation: LocalLoginOperation) {
        _localLoginOperation.value = operation
    }

    fun updateOtherLoginOperation(operation: OtherLoginOperation) {
        _otherLoginOperation.value = operation
    }

    fun updateServerOperation(operation: ServerOperation) {
        _serverOperation.value = operation
    }

    fun updateAccountOperation(operation: AccountOperation) {
        _accountOperation.value = operation
    }

    fun updateAccountSkinOperation(accountUuid: String, operation: AccountSkinOperation) {
        _accountSkinOperationMap.value = _accountSkinOperationMap.value + (accountUuid to operation)
    }

    fun performMicrosoftLogin(
        context: Context,
        toWeb: (url: String) -> Unit,
        backToMain: () -> Unit,
        checkIfInWebScreen: () -> Boolean,
        submitError: (ErrorViewModel.ThrowableMessage) -> Unit
    ) {
        microsoftLogin(
            context = context,
            toWeb = toWeb,
            backToMain = backToMain,
            checkIfInWebScreen = checkIfInWebScreen,
            updateOperation = { updateMicrosoftLoginOperation(it) },
            submitError = submitError
        )
        updateMicrosoftLoginOperation(MicrosoftLoginOperation.None)
    }

    fun importSkinFile(
        context: Context,
        account: Account,
        uri: Uri,
        submitError: (ErrorViewModel.ThrowableMessage) -> Unit
    ) {
        val fileName = context.getFileName(uri) ?: UUID.randomUUID().toString().replace("-", "")
        val cacheFile = File(PathManager.DIR_IMAGE_CACHE, fileName)

        val task = Task.runTask(
            id = account.uniqueUUID,
            dispatcher = Dispatchers.IO,
            task = {
                context.copyLocalFile(uri, cacheFile)
                if (validateSkinFile(cacheFile)) {
                    updateMicrosoftChangeSkinOperation(MicrosoftChangeSkinOperation.SelectSkinModel(account, cacheFile))
                } else {
                    submitError(
                        ErrorViewModel.ThrowableMessage(
                            title = context.getString(R.string.generic_warning),
                            message = context.getString(R.string.account_change_skin_invalid)
                        )
                    )
                    updateMicrosoftChangeSkinOperation(MicrosoftChangeSkinOperation.None)
                }
            },
            onError = { th ->
                submitError(
                    ErrorViewModel.ThrowableMessage(
                        title = context.getString(R.string.generic_error),
                        message = context.getString(R.string.account_change_skin_failed_to_import) + "\r\n" + th.getMessageOrToString()
                    )
                )
                updateMicrosoftChangeSkinOperation(MicrosoftChangeSkinOperation.None)
            },
            onCancel = {
                updateMicrosoftChangeSkinOperation(MicrosoftChangeSkinOperation.None)
            }
        )
        TaskSystem.submitTask(task)
    }

    fun uploadMicrosoftSkin(
        context: Context,
        account: Account,
        skinFile: File,
        skinModel: SkinModelType,
        submitError: (ErrorViewModel.ThrowableMessage) -> Unit
    ) {
        val task = Task.runTask(
            dispatcher = Dispatchers.IO,
            task = { task ->
                executeWithAuthorization(
                    block = {
                        task.updateProgress(-1f, R.string.account_change_skin_uploading)
                        uploadSkin(
                            apiUrl = MINECRAFT_SERVICES_URL,
                            accessToken = account.accessToken,
                            file = skinFile,
                            modelType = skinModel
                        )
                    },
                    onRefreshRequest = {
                        account.refreshMicrosoft(task = task, coroutineContext = coroutineContext)
                        AccountsManager.suspendSaveAccount(account)
                    }
                )
                task.updateMessage(R.string.account_change_skin_update_local)
                runCatching {
                    account.downloadSkin()
                }.onFailure { th ->
                    submitError(
                        ErrorViewModel.ThrowableMessage(
                            title = context.getString(R.string.account_logging_in_failed),
                            message = formatAccountError(context, th)
                        )
                    )
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.account_change_skin_update_toast),
                        Toast.LENGTH_LONG
                    ).show()
                }
                updateMicrosoftChangeSkinOperation(MicrosoftChangeSkinOperation.None)
            },
            onError = { th ->
                val (title, message) = when (th) {
                    is io.ktor.client.plugins.ResponseException -> {
                        val response = th.response
                        val code = response.status.value
                        val body = response.safeBodyAsJson<JsonObject>()
                        val message = body["errorMessage"]?.jsonPrimitive?.contentOrNull
                        context.getString(R.string.account_change_skin_failed_to_upload, code) to (message ?: th.getMessageOrToString())
                    }
                    else -> context.getString(R.string.generic_error) to formatAccountError(context, th)
                }
                submitError(ErrorViewModel.ThrowableMessage(title, message))
                updateMicrosoftChangeSkinOperation(MicrosoftChangeSkinOperation.None)
            },
            onCancel = {
                updateMicrosoftChangeSkinOperation(MicrosoftChangeSkinOperation.None)
            }
        )
        TaskSystem.submitTask(task)
    }

    fun fetchMicrosoftCapes(
        context: Context,
        account: Account,
        submitError: (ErrorViewModel.ThrowableMessage) -> Unit
    ) {
        val task = Task.runTask(
            id = account.uniqueUUID,
            dispatcher = Dispatchers.IO,
            task = { task ->
                executeWithAuthorization(
                    block = {
                        task.updateProgress(-1f, R.string.account_change_cape_fetch_all)
                        val profile = getPlayerProfile(
                            apiUrl = MINECRAFT_SERVICES_URL,
                            accessToken = account.accessToken
                        )
                        task.updateProgress(-1f, R.string.account_change_cape_cache_all)
                        cacheAllCapes(profile = profile)
                        updateMicrosoftChangeCapeOperation(MicrosoftChangeCapeOperation.SelectCape(account, profile))
                    },
                    onRefreshRequest = {
                        account.refreshMicrosoft(task = task, coroutineContext = coroutineContext)
                        AccountsManager.suspendSaveAccount(account)
                    }
                )
            },
            onError = { th ->
                submitError(
                    ErrorViewModel.ThrowableMessage(
                        title = context.getString(R.string.generic_error),
                        message = context.getString(R.string.account_change_cape_fetch_all_failed) + "\r\n" + th.getMessageOrToString()
                    )
                )
                updateMicrosoftChangeCapeOperation(MicrosoftChangeCapeOperation.None)
            },
            onCancel = {
                updateMicrosoftChangeCapeOperation(MicrosoftChangeCapeOperation.None)
            }
        )
        TaskSystem.submitTask(task)
    }

    fun applyMicrosoftCape(
        context: Context,
        account: Account,
        capeId: String?,
        capeName: String,
        isReset: Boolean,
        submitError: (ErrorViewModel.ThrowableMessage) -> Unit
    ) {
        val task = Task.runTask(
            dispatcher = Dispatchers.IO,
            task = { task ->
                executeWithAuthorization(
                    block = {
                        task.updateMessage(R.string.account_change_cape_apply)
                        changeCape(
                            apiUrl = MINECRAFT_SERVICES_URL,
                            accessToken = account.accessToken,
                            capeId = capeId
                        )
                    },
                    onRefreshRequest = {
                        account.refreshMicrosoft(task = task, coroutineContext = coroutineContext)
                        AccountsManager.suspendSaveAccount(account)
                    }
                )
                withContext(Dispatchers.Main) {
                    val text = if (isReset) {
                        context.getString(R.string.account_change_cape_apply_reset)
                    } else {
                        context.getString(R.string.account_change_cape_apply_success, capeName)
                    }
                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
                }
                updateMicrosoftChangeCapeOperation(MicrosoftChangeCapeOperation.None)
            },
            onError = { th ->
                val (title, message) = when (th) {
                    is io.ktor.client.plugins.ResponseException -> {
                        val response = th.response
                        val code = response.status.value
                        val body = response.safeBodyAsJson<JsonObject>()
                        val message = body["errorMessage"]?.jsonPrimitive?.contentOrNull
                        context.getString(R.string.account_change_cape_apply_failed, code) to (message ?: th.getMessageOrToString())
                    }
                    else -> context.getString(R.string.generic_error) to formatAccountError(context, th)
                }
                submitError(ErrorViewModel.ThrowableMessage(title, message))
                updateMicrosoftChangeCapeOperation(MicrosoftChangeCapeOperation.None)
            },
            onCancel = {
                updateMicrosoftChangeCapeOperation(MicrosoftChangeCapeOperation.None)
            }
        )
        TaskSystem.submitTask(task)
    }

    fun createLocalAccount(userName: String, userUUID: String?) {
        localLogin(userName = userName, userUUID = userUUID)
        updateLocalLoginOperation(LocalLoginOperation.None)
    }

    fun loginWithOtherServer(
        context: Context,
        server: AuthServer,
        email: String,
        password: String,
        submitError: (ErrorViewModel.ThrowableMessage) -> Unit
    ) {
        AuthServerHelper(
            server, email, password,
            onSuccess = { account, task ->
                task.updateMessage(R.string.account_logging_in_saving)
                account.downloadSkin()
                AccountsManager.suspendSaveAccount(account)
            },
            onFailed = { th ->
                updateOtherLoginOperation(OtherLoginOperation.OnFailed(th))
            }
        ).createNewAccount(context) { availableProfiles, selectedFunction ->
            updateOtherLoginOperation(OtherLoginOperation.SelectRole(availableProfiles, selectedFunction))
        }
    }

    fun addServer(serverUrl: String) {
        addOtherServer(
            serverUrl = serverUrl,
            onThrowable = { updateServerOperation(ServerOperation.OnThrowable(it)) }
        )
        updateServerOperation(ServerOperation.None)
    }

    fun deleteServer(server: AuthServer) {
        AccountsManager.deleteAuthServer(server)
        updateServerOperation(ServerOperation.None)
    }

    fun deleteAccount(account: Account) {
        AccountsManager.deleteAccount(account)
        updateAccountOperation(AccountOperation.None)
    }

    fun refreshAccount(context: Context, account: Account) {
        AccountsManager.refreshAccount(
            context = context,
            account = account,
            onFailed = { th ->
                updateAccountOperation(AccountOperation.OnFailed(th))
            }
        )
    }

    fun saveLocalSkin(
        context: Context,
        account: Account,
        uri: Uri,
        onRefreshAvatar: () -> Unit,
        submitError: (ErrorViewModel.ThrowableMessage) -> Unit
    ) {
        val skinFile = account.getSkinFile()
        val cacheFile = File(PathManager.DIR_IMAGE_CACHE, skinFile.name)
        TaskSystem.submitTask(
            Task.runTask(
                dispatcher = Dispatchers.IO,
                task = {
                    context.copyLocalFile(uri, cacheFile)
                    if (validateSkinFile(cacheFile)) {
                        cacheFile.copyTo(target = skinFile, overwrite = true)
                        FileUtils.deleteQuietly(cacheFile)
                        AccountsManager.suspendSaveAccount(account)
                        onRefreshAvatar()
                        updateAccountSkinOperation(account.uniqueUUID, AccountSkinOperation.None)
                    } else {
                        submitError(
                            ErrorViewModel.ThrowableMessage(
                                title = context.getString(R.string.generic_warning),
                                message = context.getString(R.string.account_change_skin_invalid)
                            )
                        )
                        updateAccountSkinOperation(account.uniqueUUID, AccountSkinOperation.None)
                    }
                },
                onError = { th ->
                    FileUtils.deleteQuietly(cacheFile)
                    submitError(
                        ErrorViewModel.ThrowableMessage(
                            title = context.getString(R.string.error_import_image),
                            message = th.getMessageOrToString()
                        )
                    )
                    onRefreshAvatar()
                    updateAccountSkinOperation(account.uniqueUUID, AccountSkinOperation.None)
                }
            )
        )
    }

    fun resetSkin(account: Account, onRefreshAvatar: () -> Unit) {
        TaskSystem.submitTask(
            Task.runTask(
                dispatcher = Dispatchers.IO,
                task = {
                    account.apply {
                        FileUtils.deleteQuietly(getSkinFile())
                        skinModelType = SkinModelType.NONE
                        profileId = getLocalUUIDWithSkinModel(username, skinModelType)
                        AccountsManager.suspendSaveAccount(this)
                        onRefreshAvatar()
                    }
                }
            )
        )
        updateAccountSkinOperation(account.uniqueUUID, AccountSkinOperation.None)
    }

    fun formatAccountError(context: Context, th: Throwable): String = when (th) {
        is NotPurchasedMinecraftException -> toLocal(context)
        is MinecraftProfileException -> th.toLocal(context)
        is XboxLoginException -> th.toLocal(context)
        is ResponseException -> th.responseMessage
        is HttpRequestTimeoutException -> context.getString(R.string.error_timeout)
        is UnknownHostException -> context.getString(R.string.error_network_unreachable)
        is UnresolvedAddressException -> context.getString(R.string.error_network_unreachable)
        is ConnectException -> context.getString(R.string.error_connection_failed)
        is io.ktor.client.plugins.ResponseException -> {
            val statusCode = th.response.status
            val res = when (statusCode) {
                HttpStatusCode.Unauthorized -> R.string.error_unauthorized
                HttpStatusCode.NotFound -> R.string.error_notfound
                else -> R.string.error_client_error
            }
            context.getString(res, statusCode)
        }
        else -> {
            lError("An unknown exception was caught!", th)
            val errorMessage = th.localizedMessage ?: th.message ?: th::class.qualifiedName ?: "Unknown error"
            context.getString(R.string.error_unknown, errorMessage)
        }
    }
}

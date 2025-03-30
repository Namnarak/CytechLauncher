package com.movtery.zalithlauncher.game.account.otherserver

import android.content.Context
import android.util.Log
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.otherserver.models.Servers.Server
import com.movtery.zalithlauncher.game.account.otherserver.models.AuthResult
import com.movtery.zalithlauncher.task.Task
import com.movtery.zalithlauncher.task.TaskSystem
import java.util.Objects

/**
 * 帮助登录外置账号（创建新的外置账号、仅登录当前外置账号）
 */
class OtherLoginHelper(
    private val baseUrl: String,
    private val serverName: String,
    private val email: String,
    private val password: String,
    private val listener: OnLoginListener
) {
    constructor(
        server: Server,
        email: String,
        password: String,
        listener: OnLoginListener
    ): this(server.baseUrl, server.serverName, email, password, listener)

    private fun login(
        context: Context,
        loginListener: LoginAccountListener,
        taskId: String? = null,
        loggingString: String = serverName
    ) {
        val task = Task.runTask(id = taskId, message = context.getString(R.string.account_logging_in, loggingString)) {
            OtherLoginApi.setBaseUrl(baseUrl)
            OtherLoginApi.login(context, email, password,
                object : OtherLoginApi.Listener {
                    override fun onSuccess(authResult: AuthResult) {
                        if (!Objects.isNull(authResult.selectedProfile)) {
                            loginListener.onlyOneRole(authResult)
                        } else {
                            loginListener.hasMultipleRoles(authResult)
                        }
                    }

                    override fun onFailed(error: String) {
                        listener.onFailed(error)
                    }
                })
        }.onThrowable { e ->
            val message = "An exception was encountered while performing the login task."
            Log.e("OtherLogin", message, e)
            listener.onFailed(e.message ?: message)
        }

        TaskSystem.submitTask(task)
    }

    /**
     * 将账号信息写入到账号对象中（单独区分出来是为了适配仅登录的情况，刷新账号信息）
     * @param account 需要写入的账号
     */
    private fun writeAccount(
        account: Account,
        authResult: AuthResult,
        userName: String,
        profileId: String,
        updateSkin: Boolean = true,
    ) {
        account.apply {
            this.accessToken = authResult.accessToken
            this.clientToken = authResult.clientToken
            this.otherBaseUrl = baseUrl
            this.otherAccount = email
            this.otherPassword = password
            this.accountType = serverName
            this.username = userName
            this.profileId = profileId
        }
        if (updateSkin) account.downloadSkin()
    }

    /**
     * 通过账号密码，登录一个新的账号
     * @param selectRole 当账号拥有多个角色时，需要选择角色
     */
    fun createNewAccount(
        context: Context,
        selectRole: (List<AuthResult.AvailableProfiles>, (AuthResult.AvailableProfiles) -> Unit) -> Unit
    ) {
        login(context, object : LoginAccountListener {
            override fun onlyOneRole(authResult: AuthResult) {
                val profileId = authResult.selectedProfile.id
                val account: Account = AccountsManager.loadFromProfileID(profileId) ?: Account()
                writeAccount(account, authResult, authResult.selectedProfile.name, profileId)
                listener.onSuccess(account)
            }

            override fun hasMultipleRoles(authResult: AuthResult) {
                selectRole(authResult.availableProfiles) { selectedProfile ->
                    val profileId = selectedProfile.id
                    val account: Account = AccountsManager.loadFromProfileID(profileId) ?: Account()
                    writeAccount(account, authResult, selectedProfile.name, profileId, updateSkin = false)
                    refresh(context, account)
                }
            }
        })
    }

    /**
     * 仅仅只是登录外置账号（使用账号密码登录）
     * JUST DO IT!!!
     */
    fun justLogin(context: Context, account: Account) {
        fun roleNotFound() { //未找到匹配的ID
            listener.onFailed(context.getString(R.string.account_other_login_role_not_found))
        }

        login(context, object : LoginAccountListener {
            override fun onlyOneRole(authResult: AuthResult) {
                if (authResult.selectedProfile.id != account.profileId) {
                    roleNotFound()
                    return
                }
                writeAccount(account, authResult, authResult.selectedProfile.name, authResult.selectedProfile.id)
                listener.onSuccess(account)
            }

            override fun hasMultipleRoles(authResult: AuthResult) {
                authResult.availableProfiles.forEach { profile ->
                    if (profile.id == account.profileId) {
                        //匹配当前账号的ID时，那么这个角色就是这个账号
                        writeAccount(account, authResult, profile.name, profile.id)
                        listener.onSuccess(account)
                        return
                    }
                }
                roleNotFound()
            }
        }, taskId = account.uniqueUUID, loggingString = account.username)
    }

    private fun refresh(context: Context, account: Account) {
        val task = Task.runTask(message = context.getString(R.string.account_other_login_select_role_logging, account.username)) {
            OtherLoginApi.setBaseUrl(baseUrl)
            OtherLoginApi.refresh(context, account, true, object : OtherLoginApi.Listener {
                override fun onSuccess(authResult: AuthResult) {
                    account.accessToken = authResult.accessToken
                    account.downloadSkin()
                    listener.onSuccess(account)
                }

                override fun onFailed(error: String) {
                    listener.onFailed(error)
                }
            })
        }.onThrowable { e ->
            val message = "An exception was encountered while performing the refresh task."
            Log.e("Other Login", message, e)
            listener.onFailed(e.message ?: message)
        }

        TaskSystem.submitTask(task)
    }

    interface OnLoginListener {
        fun onSuccess(account: Account)
        fun onFailed(error: String)
    }

    /**
     * 账号拥有的角色数量不同时，所做出的登陆决策
     */
    private interface LoginAccountListener {
        fun onlyOneRole(authResult: AuthResult)

        fun hasMultipleRoles(authResult: AuthResult)
    }
}
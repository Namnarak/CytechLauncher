package com.movtery.zalithlauncher.game.account

import android.util.Log
import com.google.gson.JsonSyntaxException
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.utils.GSON
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList

object AccountsManager {
    private val accountsLock = Any()
    private val _accounts = CopyOnWriteArrayList<Account>()
    private val _accountsFlow = MutableStateFlow<List<Account>>(emptyList())
    val accountsFlow: StateFlow<List<Account>> = _accountsFlow

    /**
     * 刷新当前已登录的账号，已登录的账号保存在 `PathManager.DIR_ACCOUNT` 目录中
     */
    fun reloadAccounts() {
        synchronized(accountsLock) {
            _accounts.clear()

            PathManager.DIR_ACCOUNT
                .takeIf { it.exists() && it.isDirectory }
                ?.listFiles()
                ?.forEach { file ->
                    if (file.isFile) {
                        val account = runCatching {
                            parseAccount(file)
                        }.getOrElse { e ->
                            Log.e("AccountsManager", "Failed to load account", e)
                            null
                        } ?: return@forEach

                        if (!_accounts.contains(account)) {
                            _accounts.add(account)
                        }
                    }
                }

            if (_accounts.isNotEmpty()) {
                val currentAccount = AllSettings.currentAccount.getValue()
                if (currentAccount.isEmpty() || !isAccountExists(currentAccount)) {
                    AllSettings.currentAccount.put(_accounts[0].uniqueUUID).save()
                }
            }

            _accounts.sortWith { o1, o2 -> o1.username.compareTo(o2.username) }
            _accountsFlow.value = _accounts.toList()

            Log.i("AccountsManager", "Loaded ${_accounts.size} accounts")
        }
    }

    /**
     * 获取当前已登录的账号
     */
    fun getCurrentAccount(): Account? {
        return getCurrentAccount(_accounts)
    }

    /**
     * 获取当前已登录的账号
     */
    fun getCurrentAccount(accountList: List<Account>): Account? {
        return synchronized(accountsLock) {
            loadFromUniqueUUID(AllSettings.currentAccount.getValue())
                ?: accountList.firstOrNull()
        }
    }

    /**
     * 设置并保存当前账号
     */
    fun setCurrentAccount(account: Account) {
        AllSettings.currentAccount.put(account.uniqueUUID).save()
    }

    /**
     * 移除账号
     */
    fun deleteAccount(account: Account) {
        val accountFile = File(PathManager.DIR_ACCOUNT, account.uniqueUUID)
        val accountSkinFile = File(PathManager.DIR_ACCOUNT_SKIN, "${account.uniqueUUID}.png")
        FileUtils.deleteQuietly(accountFile)
        FileUtils.deleteQuietly(accountSkinFile)
        reloadAccounts()
    }

    /**
     * 是否已登录过微软账号
     */
    fun hasMicrosoftAccount(): Boolean = synchronized(accountsLock) {
        _accounts.any(::isMicrosoftAccount)
    }

    /**
     * 通过账号信息保存的文件读取账号
     */
    @Throws(JsonSyntaxException::class)
    fun parseAccount(accountFile: File): Account {
        return parseAccount(accountFile.readText())
    }

    /**
     * 通过账号信息字符串读取账号
     */
    @Throws(JsonSyntaxException::class)
    fun parseAccount(account: String): Account {
        return GSON.fromJson(account, Account::class.java)
    }

    /**
     * 通过账号的唯一标识符读取账号
     */
    private fun loadFromUniqueUUID(uniqueUUID: String): Account? {
        if (!isAccountExists(uniqueUUID)) return null
        return try {
            parseAccount(File(PathManager.DIR_ACCOUNT, uniqueUUID).readText())
        } catch (e: IOException) {
            Log.e("AccountsManager", "Caught an exception while loading the profile", e)
            null
        } catch (e: JsonSyntaxException) {
            Log.e("AccountsManager", "Caught an exception while loading the profile", e)
            null
        }
    }

    /**
     * 账号是否存在
     */
    private fun isAccountExists(uniqueUUID: String): Boolean {
        return uniqueUUID.isNotEmpty() && File(PathManager.DIR_ACCOUNT, uniqueUUID).exists()
    }
}
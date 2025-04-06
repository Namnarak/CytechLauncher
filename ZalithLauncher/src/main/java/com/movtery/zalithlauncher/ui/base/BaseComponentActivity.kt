package com.movtery.zalithlauncher.ui.base

import android.os.Bundle
import com.movtery.zalithlauncher.context.Contexts
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.path.GamePathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.utils.StoragePermissionsUtils.Companion.checkPermissionsForInit

open class BaseComponentActivity : FullScreenComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Contexts.refresh(this)
        checkStoragePermissions()

        //刷新游戏目录
        GamePathManager.reloadPath()
    }

    override fun onResume() {
        super.onResume()
        checkStoragePermissions()
        AccountsManager.reloadAccounts()
    }

    private fun checkStoragePermissions() {
        //检查所有文件管理权限
        checkPermissionsForInit(this)
    }
}
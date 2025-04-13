package com.movtery.zalithlauncher.ui.base

import android.os.Bundle
import com.movtery.zalithlauncher.context.refreshContext
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.path.GamePathManager
import com.movtery.zalithlauncher.game.plugin.PluginLoader
import com.movtery.zalithlauncher.game.renderer.Renderers
import com.movtery.zalithlauncher.utils.StoragePermissionsUtils.Companion.checkPermissionsForInit

open class BaseComponentActivity : FullScreenComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        refreshContext(this)
        checkStoragePermissions()

        //加载渲染器
        Renderers.init()
        //加载插件
        PluginLoader.loadAllPlugins(this, false)
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
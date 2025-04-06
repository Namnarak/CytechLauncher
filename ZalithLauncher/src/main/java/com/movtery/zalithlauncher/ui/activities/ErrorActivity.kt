package com.movtery.zalithlauncher.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.movtery.zalithlauncher.state.ColorThemeState
import com.movtery.zalithlauncher.state.LocalColorThemeState
import com.movtery.zalithlauncher.ui.activities.ErrorActivity.Companion.BUNDLE_THROWABLE
import com.movtery.zalithlauncher.ui.base.BaseComponentActivity
import com.movtery.zalithlauncher.ui.screens.main.ErrorScreen
import com.movtery.zalithlauncher.ui.theme.ZalithLauncherTheme

class ErrorActivity : BaseComponentActivity() {

    companion object {
        const val BUNDLE_THROWABLE = "BUNDLE_THROWABLE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val throwable: Throwable = intent.extras?.getSerializable(BUNDLE_THROWABLE) as? Throwable ?: run {
            finish()
            return
        }

        setContent {
            val colorThemeState = remember { ColorThemeState() }

            CompositionLocalProvider(
                LocalColorThemeState provides colorThemeState
            ) {
                ZalithLauncherTheme {
                    ErrorScreen(
                        throwable = throwable,
                        onRestartClick = {
                            startActivity(Intent(this@ErrorActivity, MainActivity::class.java))
                        },
                        onExitClick = { finish() }
                    )
                }
            }
        }
    }
}

/**
 * 启动软件崩溃信息页面
 */
fun showLauncherCrash(context: Context, throwable: Throwable) {
    val intent = Intent(context, ErrorActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        putExtra(BUNDLE_THROWABLE, throwable)
    }
    context.startActivity(intent)
}
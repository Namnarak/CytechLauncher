package com.movtery.zalithlauncher.ui.base

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.annotation.CallSuper
import com.movtery.zalithlauncher.context.GlobalContext

abstract class AbstractComponentActivity : ComponentActivity() {
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlobalContext = this
    }
}
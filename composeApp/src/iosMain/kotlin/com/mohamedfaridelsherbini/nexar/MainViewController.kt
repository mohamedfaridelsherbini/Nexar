package com.mohamedfaridelsherbini.nexar

import androidx.compose.ui.window.ComposeUIViewController
import com.mohamedfaridelsherbini.nexar.di.initKoin
import com.mohamedfaridelsherbini.nexar.di.iosModule
import platform.UIKit.UIViewController

private var koinStarted = false

fun MainViewController(): UIViewController {
    if (!koinStarted) {
        koinStarted = true
        initKoin {
            modules(iosModule)
        }
    }
    return ComposeUIViewController { App() }
}

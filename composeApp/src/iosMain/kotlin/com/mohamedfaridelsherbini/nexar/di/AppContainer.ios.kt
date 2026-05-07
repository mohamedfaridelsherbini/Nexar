package com.mohamedfaridelsherbini.nexar.di

import com.mohamedfaridelsherbini.nexar.provideAppContainer

actual fun getAppContainer(): AppContainer = provideAppContainer()

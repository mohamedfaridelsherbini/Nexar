package com.mohamedfaridelsherbini.nexar

class AndroidPlatform : Platform {
    override val name: String = "Android ${android.os.Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

/** Called from MainActivity so edge-to-edge is set up before setContent. */
fun initPlatform(activity: android.app.Activity) = Unit

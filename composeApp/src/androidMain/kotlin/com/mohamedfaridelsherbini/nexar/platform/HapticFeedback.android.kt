@file:Suppress("StaticFieldLeak")

package com.mohamedfaridelsherbini.nexar.platform

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresPermission

private var vibrator: Vibrator? = null

fun initHaptic(context: Context) {
    vibrator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
}

@RequiresPermission(Manifest.permission.VIBRATE)
actual fun triggerSuccessHaptic() {
    val v = vibrator ?: return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        v.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        v.vibrate(40)
    }
}

@RequiresPermission(Manifest.permission.VIBRATE)
actual fun triggerWarningHaptic() {
    val v = vibrator ?: return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 60, 80, 60), -1))
    } else {
        @Suppress("DEPRECATION")
        v.vibrate(longArrayOf(0, 60, 80, 60), -1)
    }
}

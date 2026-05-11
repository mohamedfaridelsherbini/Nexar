package com.mohamedfaridelsherbini.nexar.platform

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import org.koin.core.context.GlobalContext

private class AndroidPermissionHealthProvider(
    private val context: Context,
) : PermissionHealthProvider {
    override suspend fun getPermissionHealth(): List<PermissionHealth> =
        listOf(
            cameraPermissionHealth(),
            notificationPermissionHealth(),
            filesPermissionHealth(),
        )

    override fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun cameraPermissionHealth(): PermissionHealth {
        val hasCamera = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        if (!hasCamera) {
            return PermissionHealth(
                area = PermissionArea.Camera,
                state = PermissionState.Unavailable,
                detail = "This device does not provide a camera",
            )
        }

        val granted =
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        return PermissionHealth(
            area = PermissionArea.Camera,
            state = if (granted) PermissionState.Granted else PermissionState.Denied,
            detail =
                if (granted) {
                    "Ready to scan with the device camera"
                } else {
                    "Camera access is blocked for document scanning"
                },
            action = if (granted) PermissionAction.None else PermissionAction.OpenSettings,
        )
    }

    private fun notificationPermissionHealth(): PermissionHealth {
        val enabled =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
            } else {
                NotificationManagerCompat.from(context).areNotificationsEnabled()
            }

        return PermissionHealth(
            area = PermissionArea.Notifications,
            state = if (enabled) PermissionState.Granted else PermissionState.Denied,
            detail =
                if (enabled) {
                    "Export reminders and duplicate alerts are allowed"
                } else {
                    "Notifications are blocked for reminders and duplicate alerts"
                },
            action = if (enabled) PermissionAction.None else PermissionAction.OpenSettings,
        )
    }

    private fun filesPermissionHealth(): PermissionHealth =
        PermissionHealth(
            area = PermissionArea.Files,
            state = PermissionState.NotRequired,
            detail = "Uses the system file picker for export and import access",
        )
}

actual fun createPermissionHealthProvider(): PermissionHealthProvider {
    val context: Context = GlobalContext.get().get()
    return AndroidPermissionHealthProvider(context)
}

package com.mohamedfaridelsherbini.nexar.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
private class IOSPermissionHealthProvider : PermissionHealthProvider {
    override suspend fun getPermissionHealth(): List<PermissionHealth> =
        listOf(
            cameraPermissionHealth(),
            notificationPermissionHealth(),
            filesPermissionHealth(),
        )

    override fun openAppSettings() {
        val settingsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        UIApplication.sharedApplication.openURL(settingsUrl)
    }

    private fun cameraPermissionHealth(): PermissionHealth =
        PermissionHealth(
            area = PermissionArea.Camera,
            state = PermissionState.NotRequired,
            detail = "Camera access is handled by the iOS document scanner when scanning starts",
        )

    private suspend fun notificationPermissionHealth(): PermissionHealth {
        val status =
            suspendCancellableCoroutine { continuation ->
                UNUserNotificationCenter.currentNotificationCenter()
                    .getNotificationSettingsWithCompletionHandler { settings ->
                        continuation.resume(settings?.authorizationStatus)
                    }
            }

        return when (status) {
            UNAuthorizationStatusAuthorized,
            UNAuthorizationStatusProvisional,
            UNAuthorizationStatusEphemeral,
                ->
                PermissionHealth(
                    area = PermissionArea.Notifications,
                    state = PermissionState.Granted,
                    detail = "Export reminders and duplicate alerts are allowed",
                )

            UNAuthorizationStatusDenied ->
                PermissionHealth(
                    area = PermissionArea.Notifications,
                    state = PermissionState.Denied,
                    detail = "Notifications are blocked for reminders and duplicate alerts",
                    action = PermissionAction.OpenSettings,
                )

            UNAuthorizationStatusNotDetermined ->
                PermissionHealth(
                    area = PermissionArea.Notifications,
                    state = PermissionState.Denied,
                    detail = "Notification permission has not been granted yet",
                    action = PermissionAction.OpenSettings,
                )

            else ->
                PermissionHealth(
                    area = PermissionArea.Notifications,
                    state = PermissionState.Unavailable,
                    detail = "Notification status could not be determined",
                )
        }
    }

    private fun filesPermissionHealth(): PermissionHealth =
        PermissionHealth(
            area = PermissionArea.Files,
            state = PermissionState.NotRequired,
            detail = "Uses the system document picker for file access",
        )
}

actual fun createPermissionHealthProvider(): PermissionHealthProvider = IOSPermissionHealthProvider()

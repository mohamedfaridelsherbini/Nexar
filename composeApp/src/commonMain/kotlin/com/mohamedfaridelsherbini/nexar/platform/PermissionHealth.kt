package com.mohamedfaridelsherbini.nexar.platform

enum class PermissionArea(
    val title: String,
    val subtitle: String,
) {
    Camera(
        title = "Camera",
        subtitle = "Scan documents with the device camera",
    ),
    Notifications(
        title = "Notifications",
        subtitle = "Receive export reminders and duplicate alerts",
    ),
    Files(
        title = "Files & folders",
        subtitle = "Choose export and import locations with the system picker",
    ),
}

enum class PermissionState(val label: String) {
    Granted("Granted"),
    Denied("Denied"),
    Unavailable("Unavailable"),
    NotRequired("Not required"),
}

enum class PermissionAction {
    None,
    OpenSettings,
}

data class PermissionHealth(
    val area: PermissionArea,
    val state: PermissionState,
    val detail: String,
    val action: PermissionAction = PermissionAction.None,
)

interface PermissionHealthProvider {
    suspend fun getPermissionHealth(): List<PermissionHealth>

    fun openAppSettings()
}

expect fun createPermissionHealthProvider(): PermissionHealthProvider

package com.mohamedfaridelsherbini.nexar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mohamedfaridelsherbini.nexar.platform.PermissionAction
import com.mohamedfaridelsherbini.nexar.platform.PermissionArea
import com.mohamedfaridelsherbini.nexar.platform.PermissionHealth
import com.mohamedfaridelsherbini.nexar.presentation.settings.SettingsUiState
import com.mohamedfaridelsherbini.nexar.ui.components.NexarCircleIconButton
import com.mohamedfaridelsherbini.nexar.ui.theme.NexarExtraTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onBackClick: () -> Unit,
    onThemeClick: () -> Unit,
    onStorageClick: () -> Unit,
    onExportRemindersToggled: (Boolean) -> Unit,
    onDuplicateAlertsToggled: (Boolean) -> Unit,
    onPermissionActionClick: (PermissionArea) -> Unit,
    onScreenShown: () -> Unit,
) {
    LaunchedEffect(Unit) {
        onScreenShown()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    NexarCircleIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        onClick = onBackClick,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SettingsGroup(title = "Appearance") {
                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = "Theme",
                    subtitle = uiState.theme.label,
                    onClick = onThemeClick
                )
            }

            SettingsGroup(title = "Storage") {
                SettingsItem(
                    icon = Icons.Default.Storage,
                    title = "Export location",
                    subtitle = uiState.storageLocation?.substringAfterLast('/') ?: "Not configured",
                    onClick = onStorageClick
                )
            }

            SettingsGroup(title = "Notifications") {
                SettingsToggleItem(
                    icon = Icons.Default.Notifications,
                    title = "Export reminders",
                    subtitle = "Alert me about unsaved scans",
                    checked = uiState.exportRemindersEnabled,
                    onCheckedChange = onExportRemindersToggled
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 52.dp),
                    thickness = 0.5.dp,
                    color = NexarExtraTheme.colors.borderSubtle
                )
                SettingsToggleItem(
                    icon = Icons.Default.Notifications,
                    title = "Duplicate alerts",
                    subtitle = "Notify when a duplicate is found",
                    checked = uiState.duplicateAlertsEnabled,
                    onCheckedChange = onDuplicateAlertsToggled
                )
            }

            SettingsGroup(title = "Permissions") {
                uiState.permissionHealth.forEachIndexed { index, permissionHealth ->
                    PermissionStatusItem(
                        permissionHealth = permissionHealth,
                        onClick = { onPermissionActionClick(permissionHealth.area) },
                    )
                    if (index != uiState.permissionHealth.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 52.dp),
                            thickness = 0.5.dp,
                            color = NexarExtraTheme.colors.borderSubtle
                        )
                    }
                }
            }

            SettingsGroup(title = "About") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Version",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        uiState.version,
                        style = MaterialTheme.typography.bodyLarge,
                        color = NexarExtraTheme.colors.foregroundSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = NexarExtraTheme.colors.foregroundSecondary,
            modifier = Modifier.padding(start = 4.dp)
        )
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = NexarExtraTheme.colors.foregroundSecondary
            )
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = NexarExtraTheme.colors.borderPrimary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun PermissionStatusItem(
    permissionHealth: PermissionHealth,
    onClick: () -> Unit,
) {
    val canOpenSettings = permissionHealth.action == PermissionAction.OpenSettings
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (canOpenSettings) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = permissionIcon(permissionHealth.area),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                permissionHealth.area.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                permissionHealth.detail.ifBlank { permissionHealth.area.subtitle },
                style = MaterialTheme.typography.bodySmall,
                color = NexarExtraTheme.colors.foregroundSecondary
            )
        }
        Text(
            permissionHealth.state.label,
            style = MaterialTheme.typography.labelLarge,
            color = if (canOpenSettings) {
                MaterialTheme.colorScheme.primary
            } else {
                NexarExtraTheme.colors.foregroundSecondary
            }
        )
        if (canOpenSettings) {
            Spacer(Modifier.size(8.dp))
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = NexarExtraTheme.colors.borderPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = NexarExtraTheme.colors.foregroundSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.surface,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = NexarExtraTheme.colors.borderPrimary,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

private fun permissionIcon(area: PermissionArea): ImageVector =
    when (area) {
        PermissionArea.Camera -> Icons.Default.PermMedia
        PermissionArea.Notifications -> Icons.Default.Notifications
        PermissionArea.Files -> Icons.Default.PhotoLibrary
    }

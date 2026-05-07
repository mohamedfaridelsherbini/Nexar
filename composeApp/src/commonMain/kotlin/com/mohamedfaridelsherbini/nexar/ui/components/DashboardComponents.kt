package com.mohamedfaridelsherbini.nexar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedfaridelsherbini.nexar.ui.theme.NexarExtraTheme
import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument

@Composable
fun NexarLogo(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    sheetColor: Color = MaterialTheme.colorScheme.surface,
    accentColor: Color = Color(0xFF0EA5A4) // $accent-primary
) {
    Box(
        modifier = modifier
            .size(30.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        // Simple representation of the logo mark from .pen
        Box(
            modifier = Modifier
                .size(width = 12.dp, height = 18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(sheetColor)
        )
        
        // Scan beam
        Box(
            modifier = Modifier
                .size(width = 16.dp, height = 2.dp)
                .background(accentColor)
        )
    }
}

@Composable
fun NexarTopBar(
    onSettingsClick: () -> Unit,
    onCreateFolderClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NexarLogo()
            
            Column {
                Text(
                    text = "Nexar",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Documents scanned locally",
                    style = MaterialTheme.typography.bodySmall,
                    color = NexarExtraTheme.colors.foregroundSecondary
                )
            }
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (onCreateFolderClick != null) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    onClick = onCreateFolderClick
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CreateNewFolder,
                            contentDescription = "Create Folder",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                onClick = onSettingsClick
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.FolderZip, // Using FolderZip as a proxy for folder-cog
                        contentDescription = "Storage Settings",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun StorageWarningBanner(
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NexarExtraTheme.colors.warning)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "Export folder missing",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Choose a persistent folder before exporting PDFs.",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun NexarSearchInput(
    value: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(27.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, NexarExtraTheme.colors.borderSubtle, RoundedCornerShape(27.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = NexarExtraTheme.colors.foregroundSecondary,
            modifier = Modifier.size(19.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    text = "Search scans, dates, or folders",
                    color = NexarExtraTheme.colors.foregroundSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            // In a real implementation, this would be a BasicTextField
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun DocumentCard(
    document: ScannedDocument,
    onPreviewClick: () -> Unit,
    onRenameClick: () -> Unit,
    onExportClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, NexarExtraTheme.colors.borderSubtle, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // PDF Icon Tile
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = Color(0xFF042F2E), // Based on $accent-primary dark variant in .pen
                modifier = Modifier.size(25.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(14.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = document.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${document.imageUris.size} pages • PDF ready • Today",
                style = MaterialTheme.typography.bodySmall,
                color = NexarExtraTheme.colors.foregroundSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DocumentActionChip(
                    icon = Icons.Default.Visibility,
                    label = "Preview",
                    onClick = onPreviewClick
                )
                DocumentActionChip(
                    icon = Icons.Default.Edit,
                    label = "Rename",
                    onClick = onRenameClick
                )
                DocumentActionChip(
                    icon = Icons.Default.Share,
                    label = "Export",
                    onClick = onExportClick
                )
            }
        }
    }
}

@Composable
fun DocumentActionChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.height(34.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = borderStroke(1.dp, NexarExtraTheme.colors.borderSubtle),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun LocalStorageStatus() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, NexarExtraTheme.colors.borderSubtle, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(NexarExtraTheme.colors.success)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Local scans are saved on this device until export is configured.",
            style = MaterialTheme.typography.bodySmall,
            color = NexarExtraTheme.colors.foregroundSecondary
        )
    }
}

@Composable
fun NexarFAB(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(32.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color(0xFF042F2E)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Scan document",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Helper for border stroke since BorderStroke is not directly available as a simple function in some contexts
@Composable
fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) = androidx.compose.foundation.BorderStroke(width, color)

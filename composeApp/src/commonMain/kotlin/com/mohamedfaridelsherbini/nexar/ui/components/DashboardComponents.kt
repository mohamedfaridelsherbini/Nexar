package com.mohamedfaridelsherbini.nexar.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mohamedfaridelsherbini.nexar.ui.theme.NexarExtraTheme
import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument

// On-accent text color used on top of the teal primary, per Nexar.pen ($on-tertiary).
private val NexarOnAccent = Color(0xFF042F2E)

enum class DocumentFilter { All, NeedsExport }

/**
 * Nexar "Document Aperture" mark from Nexar.pen.
 *
 * Layout is normalized to a 188×188 design field (matching the pen's "Constructed Mark Field"),
 * so all elements stay proportional at any rendered [size].
 *
 * Composition:
 *  - rounded square field
 *  - white document sheet with a folded top-right corner
 *  - horizontal teal scan beam crossing the sheet
 *  - four teal corner focus brackets framing the sheet
 */
@Composable
fun NexarLogo(
    modifier: Modifier = Modifier,
    size: Dp = 30.dp,
    fieldColor: Color = MaterialTheme.colorScheme.onSurface,
    sheetColor: Color = MaterialTheme.colorScheme.surface,
    foldColor: Color = NexarExtraTheme.colors.borderSubtle,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    val fieldRadius = size * (24f / 188f)
    Canvas(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(fieldRadius))
            .background(fieldColor)
    ) {
        val unit = this.size.minDimension / 188f
        fun u(v: Float) = v * unit

        // Document sheet: x=56,y=38, 76×112, r=12
        drawRoundRect(
            color = sheetColor,
            topLeft = Offset(u(56f), u(38f)),
            size = Size(u(76f), u(112f)),
            cornerRadius = CornerRadius(u(12f), u(12f))
        )

        // Folded top-right corner triangle
        val foldPath = Path().apply {
            moveTo(u(112f), u(38f))
            lineTo(u(132f), u(38f))
            lineTo(u(132f), u(58f))
            close()
        }
        drawPath(foldPath, foldColor)

        // Horizontal scan beam: x=44,y=88, 100×12, r=6
        drawRoundRect(
            color = accentColor,
            topLeft = Offset(u(44f), u(88f)),
            size = Size(u(100f), u(12f)),
            cornerRadius = CornerRadius(u(6f), u(6f))
        )

        // Corner focus brackets (top-left + bottom-right pairs), r=3
        // Top edge piece
        drawRoundRect(
            color = accentColor,
            topLeft = Offset(u(30f), u(30f)),
            size = Size(u(36f), u(5f)),
            cornerRadius = CornerRadius(u(3f), u(3f))
        )
        // Left edge piece
        drawRoundRect(
            color = accentColor,
            topLeft = Offset(u(30f), u(30f)),
            size = Size(u(5f), u(36f)),
            cornerRadius = CornerRadius(u(3f), u(3f))
        )
        // Bottom edge piece
        drawRoundRect(
            color = accentColor,
            topLeft = Offset(u(122f), u(153f)),
            size = Size(u(36f), u(5f)),
            cornerRadius = CornerRadius(u(3f), u(3f))
        )
        // Right edge piece
        drawRoundRect(
            color = accentColor,
            topLeft = Offset(u(153f), u(122f)),
            size = Size(u(5f), u(36f)),
            cornerRadius = CornerRadius(u(3f), u(3f))
        )

        // Subtle inside stroke on the field, mirroring the pen's 1pt border-subtle outline.
        val strokeInset = u(0.5f)
        drawRoundRect(
            color = foldColor,
            topLeft = Offset(strokeInset, strokeInset),
            size = Size(this.size.width - strokeInset * 2f, this.size.height - strokeInset * 2f),
            cornerRadius = CornerRadius(u(24f), u(24f)),
            style = Stroke(width = u(1f))
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
            .border(1.dp, NexarExtraTheme.colors.borderSubtle, RoundedCornerShape(16.dp))
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
                    border = BorderStroke(1.dp, NexarExtraTheme.colors.borderSubtle),
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
                border = BorderStroke(1.dp, NexarExtraTheme.colors.borderSubtle),
                onClick = onSettingsClick
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.FolderZip,
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
    onValueChange: (String) -> Unit,
    placeholder: String = "Search scans, dates, or folders"
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
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = NexarExtraTheme.colors.foregroundMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    inner()
                }
            )
        }
        if (value.isNotEmpty()) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Clear search",
                tint = NexarExtraTheme.colors.foregroundSecondary,
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .clickable { onValueChange("") }
            )
        }
    }
}

@Composable
fun QuickFilters(
    selected: DocumentFilter,
    needsExportCount: Int,
    onSelect: (DocumentFilter) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        QuickFilterChip(
            label = "All",
            selected = selected == DocumentFilter.All,
            onClick = { onSelect(DocumentFilter.All) }
        )
        QuickFilterChip(
            label = if (needsExportCount > 0) "Needs export · $needsExportCount" else "Needs export",
            selected = selected == DocumentFilter.NeedsExport,
            accent = NexarExtraTheme.colors.warning,
            onClick = { onSelect(DocumentFilter.NeedsExport) }
        )
    }
}

@Composable
private fun QuickFilterChip(
    label: String,
    selected: Boolean,
    accent: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    val containerColor = if (selected) accent else MaterialTheme.colorScheme.surface
    val contentColor = if (selected) NexarOnAccent else MaterialTheme.colorScheme.onSurface
    Surface(
        modifier = Modifier.height(36.dp),
        shape = CircleShape,
        color = containerColor,
        border = if (selected) null else BorderStroke(1.dp, NexarExtraTheme.colors.borderSubtle),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}

@Composable
fun DocumentCard(
    document: ScannedDocument,
    exportEnabled: Boolean,
    onPreviewClick: () -> Unit,
    onRenameClick: () -> Unit,
    onExportClick: () -> Unit,
    onConfigureExportClick: () -> Unit
) {
    val isExported = document.pdfUri != null
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, NexarExtraTheme.colors.borderSubtle, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
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
                tint = NexarOnAccent,
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
            val statusText = when {
                isExported -> "${document.imageUris.size} pages • Exported"
                else -> "${document.imageUris.size} pages • PDF ready"
            }
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                color = NexarExtraTheme.colors.foregroundMuted
            )
            Spacer(modifier = Modifier.height(10.dp))
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
                if (exportEnabled) {
                    DocumentActionChip(
                        icon = Icons.Default.Share,
                        label = "Export",
                        onClick = onExportClick
                    )
                } else {
                    DocumentActionChip(
                        icon = Icons.Default.FolderOff,
                        label = "Set folder",
                        onClick = onConfigureExportClick,
                        accent = NexarExtraTheme.colors.warning
                    )
                }
            }
        }
    }
}

@Composable
fun DocumentActionChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    accent: Color? = null
) {
    val borderColor = accent ?: NexarExtraTheme.colors.borderSubtle
    val tint = accent ?: MaterialTheme.colorScheme.onSurface
    Surface(
        modifier = Modifier.height(34.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, borderColor),
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
                tint = tint
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = tint
            )
        }
    }
}

@Composable
fun LocalStorageStatus(
    storageLocation: String?
) {
    val configured = storageLocation != null
    val dotColor = if (configured) NexarExtraTheme.colors.success else NexarExtraTheme.colors.warning
    val message = if (configured) {
        "Exports save to ${storageLocation!!.shortStoragePath()}. Originals stay on this device."
    } else {
        "Local scans stay on this device until you choose an export folder."
    }
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
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = NexarExtraTheme.colors.foregroundSecondary
        )
    }
}

private fun String.shortStoragePath(): String {
    val decoded = this.substringAfterLast('/').ifEmpty { this }
    return decoded.take(40)
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
            contentColor = NexarOnAccent
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

@Composable
fun borderStroke(width: Dp, color: Color) = BorderStroke(width, color)

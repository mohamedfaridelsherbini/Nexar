package com.mohamedfaridelsherbini.nexar.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.mohamedfaridelsherbini.nexar.domain.model.DocumentCategory
import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument
import com.mohamedfaridelsherbini.nexar.presentation.dashboard.DashboardFilter
import com.mohamedfaridelsherbini.nexar.presentation.dashboard.NexarError
import com.mohamedfaridelsherbini.nexar.presentation.dashboard.SortOrder
import com.mohamedfaridelsherbini.nexar.ui.theme.NexarExtraTheme

private val NexarOnAccent = Color(0xFF042F2E)

// ─── Logo mark ────────────────────────────────────────────────────────────────

@Composable
fun NexarLogo(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
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
        val u = this.size.minDimension / 188f

        drawRoundRect(
            color = sheetColor,
            topLeft = Offset(56 * u, 38 * u),
            size = Size(76 * u, 112 * u),
            cornerRadius = CornerRadius(12 * u, 12 * u)
        )
        drawPath(
            Path().apply {
                moveTo(112 * u, 38 * u)
                lineTo(132 * u, 38 * u)
                lineTo(132 * u, 58 * u)
                close()
            },
            foldColor
        )
        drawRoundRect(
            color = accentColor,
            topLeft = Offset(44 * u, 88 * u),
            size = Size(100 * u, 12 * u),
            cornerRadius = CornerRadius(6 * u, 6 * u)
        )
        drawRoundRect(color = accentColor, topLeft = Offset(30 * u, 30 * u), size = Size(36 * u, 5 * u), cornerRadius = CornerRadius(3 * u, 3 * u))
        drawRoundRect(color = accentColor, topLeft = Offset(30 * u, 30 * u), size = Size(5 * u, 36 * u), cornerRadius = CornerRadius(3 * u, 3 * u))
        drawRoundRect(color = accentColor, topLeft = Offset(122 * u, 153 * u), size = Size(36 * u, 5 * u), cornerRadius = CornerRadius(3 * u, 3 * u))
        drawRoundRect(color = accentColor, topLeft = Offset(153 * u, 122 * u), size = Size(5 * u, 36 * u), cornerRadius = CornerRadius(3 * u, 3 * u))

        val inset = 0.5f * u
        drawRoundRect(
            color = foldColor,
            topLeft = Offset(inset, inset),
            size = Size(this.size.width - inset * 2, this.size.height - inset * 2),
            cornerRadius = CornerRadius(24 * u, 24 * u),
            style = Stroke(width = u)
        )
    }
}

// ─── Top bar ──────────────────────────────────────────────────────────────────

@Composable
fun NexarTopBar(
    onSettingsClick: () -> Unit,
    onCreateFolderClick: (() -> Unit)? = null,
    currentSort: SortOrder = SortOrder.Newest,
    onSortChanged: (SortOrder) -> Unit = {},
    needsExportCount: Int = 0,
    storageConfigured: Boolean = false,
    onBatchExportClick: (() -> Unit)? = null
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, NexarExtraTheme.colors.borderSubtle, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NexarLogo(size = 32.dp)
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = "Nexar",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Documents scanned locally",
                    style = MaterialTheme.typography.bodySmall,
                    color = NexarExtraTheme.colors.foregroundSecondary
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (storageConfigured && needsExportCount > 0 && onBatchExportClick != null) {
                Box {
                    TopBarIconButton(
                        icon = Icons.Default.CloudUpload,
                        contentDescription = "Export all ($needsExportCount)",
                        onClick = onBatchExportClick
                    )
                    if (needsExportCount > 0) {
                        Badge(
                            modifier = Modifier.align(Alignment.TopEnd),
                            containerColor = NexarExtraTheme.colors.warning,
                            contentColor = Color.White
                        ) {
                            Text(
                                text = needsExportCount.toString(),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
            if (onCreateFolderClick != null) {
                TopBarIconButton(
                    icon = Icons.Default.CreateNewFolder,
                    contentDescription = "Create folder",
                    onClick = onCreateFolderClick
                )
            }
            Box {
                TopBarIconButton(
                    icon = Icons.Default.Sort,
                    contentDescription = "Sort",
                    onClick = { showSortMenu = true }
                )
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    SortOrder.entries.forEach { order ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = order.label,
                                    fontWeight = if (order == currentSort) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = if (order == currentSort) {
                                { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            onClick = {
                                onSortChanged(order)
                                showSortMenu = false
                            }
                        )
                    }
                }
            }
            TopBarIconButton(
                icon = Icons.Default.Settings,
                contentDescription = "Storage settings",
                onClick = onSettingsClick
            )
        }
    }
}

@Composable
private fun TopBarIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, NexarExtraTheme.colors.borderSubtle),
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = NexarExtraTheme.colors.foregroundSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ─── Warning banner ───────────────────────────────────────────────────────────

@Composable
fun StorageWarningBanner(onClick: () -> Unit) {
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
        Spacer(Modifier.width(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Export folder missing",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Choose a persistent folder before exporting PDFs.",
                color = Color.White.copy(alpha = 0.88f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// ─── Search ───────────────────────────────────────────────────────────────────

@Composable
fun NexarSearchInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "Search name, text, category…"
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, NexarExtraTheme.colors.borderSubtle, RoundedCornerShape(25.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = NexarExtraTheme.colors.foregroundSecondary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
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
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Clear",
                tint = NexarExtraTheme.colors.foregroundSecondary,
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .clickable { onValueChange("") }
            )
        }
    }
}

// ─── Quick filters ────────────────────────────────────────────────────────────

@Composable
fun QuickFilters(
    selected: DashboardFilter,
    needsExportCount: Int,
    onSelect: (DashboardFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickFilterChip(
            label = "All",
            selected = selected == DashboardFilter.All,
            onClick = { onSelect(DashboardFilter.All) }
        )
        QuickFilterChip(
            label = if (needsExportCount > 0) "Needs export · $needsExportCount" else "Needs export",
            selected = selected == DashboardFilter.NeedsExport,
            accent = NexarExtraTheme.colors.warning,
            onClick = { onSelect(DashboardFilter.NeedsExport) }
        )
        QuickFilterChip(
            label = "⭐ Starred",
            selected = selected == DashboardFilter.Starred,
            onClick = { onSelect(DashboardFilter.Starred) }
        )
        QuickFilterChip(
            label = "Receipts",
            selected = selected == DashboardFilter.Receipt,
            onClick = { onSelect(DashboardFilter.Receipt) }
        )
        QuickFilterChip(
            label = "Invoices",
            selected = selected == DashboardFilter.Invoice,
            onClick = { onSelect(DashboardFilter.Invoice) }
        )
        QuickFilterChip(
            label = "IDs",
            selected = selected == DashboardFilter.IdDocument,
            onClick = { onSelect(DashboardFilter.IdDocument) }
        )
        QuickFilterChip(
            label = "Contracts",
            selected = selected == DashboardFilter.Contract,
            onClick = { onSelect(DashboardFilter.Contract) }
        )
        QuickFilterChip(
            label = "Medical",
            selected = selected == DashboardFilter.Medical,
            onClick = { onSelect(DashboardFilter.Medical) }
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
    val containerColor = if (selected) accent else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (selected) NexarOnAccent else NexarExtraTheme.colors.foregroundSecondary
    Surface(
        modifier = Modifier.height(34.dp),
        shape = CircleShape,
        color = containerColor,
        border = if (selected) null else BorderStroke(1.dp, NexarExtraTheme.colors.borderSubtle),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
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

// ─── Category pill ────────────────────────────────────────────────────────────

@Composable
fun CategoryPill(category: DocumentCategory) {
    if (category == DocumentCategory.Other) return
    val (bg, fg) = when (category) {
        DocumentCategory.Receipt -> Pair(Color(0xFFFFF3CD), Color(0xFF856404))
        DocumentCategory.Invoice -> Pair(Color(0xFFD1ECF1), Color(0xFF0C5460))
        DocumentCategory.IdDocument -> Pair(Color(0xFFD4EDDA), Color(0xFF155724))
        DocumentCategory.Contract -> Pair(Color(0xFFE2D9F3), Color(0xFF4A235A))
        DocumentCategory.Medical -> Pair(Color(0xFFFFE0E0), Color(0xFF7B1818))
        DocumentCategory.Other -> Pair(MaterialTheme.colorScheme.surfaceVariant, NexarExtraTheme.colors.foregroundMuted)
    }
    Surface(
        shape = CircleShape,
        color = bg,
        modifier = Modifier.height(22.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = fg
            )
        }
    }
}

// ─── Swipeable document card wrapper ─────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableDocumentCard(
    document: ScannedDocument,
    exportEnabled: Boolean,
    isProcessing: Boolean = false,
    isExporting: Boolean = false,
    onPreviewClick: () -> Unit,
    onRenameClick: () -> Unit,
    onExportClick: () -> Unit,
    onConfigureExportClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onStarClick: () -> Unit,
    onOcrViewClick: (() -> Unit)?,
    onShareClick: (() -> Unit)?,
    onDetailClick: (() -> Unit)?
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDeleteClick()
                    false
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    if (exportEnabled) onExportClick() else onConfigureExportClick()
                    false
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val isStart = direction == SwipeToDismissBoxValue.StartToEnd
            val bgColor = if (isStart) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            else Color(0xFFFFE0E0)
            val icon = if (isStart) Icons.Default.CloudUpload else Icons.Default.Delete
            val iconTint = if (isStart) MaterialTheme.colorScheme.primary else Color(0xFFB00020)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(bgColor)
                    .padding(horizontal = 20.dp),
                contentAlignment = if (isStart) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            }
        }
    ) {
        DocumentCard(
            document = document,
            exportEnabled = exportEnabled,
            isProcessing = isProcessing,
            isExporting = isExporting,
            onPreviewClick = onPreviewClick,
            onRenameClick = onRenameClick,
            onExportClick = onExportClick,
            onConfigureExportClick = onConfigureExportClick,
            onStarClick = onStarClick,
            onOcrViewClick = onOcrViewClick,
            onShareClick = onShareClick,
            onDetailClick = onDetailClick
        )
    }
}

// ─── Document card ────────────────────────────────────────────────────────────

@Composable
fun DocumentCard(
    document: ScannedDocument,
    exportEnabled: Boolean,
    isProcessing: Boolean = false,
    isExporting: Boolean = false,
    onPreviewClick: () -> Unit,
    onRenameClick: () -> Unit,
    onExportClick: () -> Unit,
    onConfigureExportClick: () -> Unit,
    onStarClick: () -> Unit = {},
    onOcrViewClick: (() -> Unit)? = null,
    onShareClick: (() -> Unit)? = null,
    onDetailClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, NexarExtraTheme.colors.borderSubtle, RoundedCornerShape(16.dp))
            .then(if (onDetailClick != null) Modifier.clickable { onDetailClick() } else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        NexarLogo(
            size = 52.dp,
            fieldColor = MaterialTheme.colorScheme.primary,
            sheetColor = NexarOnAccent.copy(alpha = 0.18f),
            foldColor = NexarOnAccent.copy(alpha = 0.28f),
            accentColor = Color.White
        )

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = document.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onStarClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (document.isStarred) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = if (document.isStarred) "Unstar" else "Star",
                        tint = if (document.isStarred) Color(0xFFF59E0B) else NexarExtraTheme.colors.foregroundMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(Modifier.height(3.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val pages = document.imageUris.size
                val exportStatus = if (document.isExportedToStorage) "Exported" else "Ready to export"
                Text(
                    text = "${pages} ${if (pages == 1) "page" else "pages"} · $exportStatus",
                    style = MaterialTheme.typography.bodySmall,
                    color = NexarExtraTheme.colors.foregroundMuted
                )
            }

            if (document.duplicateOfId != null) {
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = null,
                        tint = NexarExtraTheme.colors.warning,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Possible duplicate",
                        style = MaterialTheme.typography.labelSmall,
                        color = NexarExtraTheme.colors.warning
                    )
                }
            }

            if (isProcessing) {
                Spacer(Modifier.height(5.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 1.5.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Analysing document…",
                        style = MaterialTheme.typography.labelSmall,
                        color = NexarExtraTheme.colors.foregroundSecondary
                    )
                }
            } else if (document.category != DocumentCategory.Other || document.ocrProcessed) {
                Spacer(Modifier.height(5.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    if (document.category != DocumentCategory.Other) {
                        CategoryPill(document.category)
                    }
                    if (document.extractedAmount != null) {
                        AmountChip(document.extractedAmount)
                    }
                    OcrStatusChip(document)
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
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
                when {
                    isExporting -> DocumentActionChip(
                        icon = Icons.Default.CloudUpload,
                        label = "Exporting…",
                        onClick = {},
                        accent = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        showSpinner = true
                    )
                    exportEnabled -> DocumentActionChip(
                        icon = if (document.isExportedToStorage) Icons.Default.CheckCircle else Icons.Default.CloudUpload,
                        label = if (document.isExportedToStorage) "Re-export" else "Export",
                        onClick = onExportClick
                    )
                    else -> DocumentActionChip(
                        icon = Icons.Default.FolderOff,
                        label = "Set folder",
                        onClick = onConfigureExportClick,
                        accent = NexarExtraTheme.colors.warning
                    )
                }
                if (onOcrViewClick != null && document.ocrProcessed && document.ocrText.isNotBlank()) {
                    DocumentActionChip(
                        icon = Icons.Default.TextSnippet,
                        label = "OCR",
                        onClick = onOcrViewClick
                    )
                }
                if (onShareClick != null && document.pdfUri != null) {
                    DocumentActionChip(
                        icon = Icons.Default.Share,
                        label = "Share",
                        onClick = onShareClick
                    )
                }
            }
        }
    }
}

@Composable
private fun AmountChip(amount: String) {
    Surface(
        shape = CircleShape,
        color = Color(0xFFD4EDDA),
        modifier = Modifier.height(22.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = amount,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF155724)
            )
        }
    }
}

@Composable
private fun OcrStatusChip(document: ScannedDocument) {
    val (label, color) = when {
        !document.ocrProcessed -> return
        document.ocrText.isNotBlank() -> Pair("Text recognized", NexarExtraTheme.colors.success)
        else -> Pair("No text found", NexarExtraTheme.colors.foregroundMuted)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
fun DocumentActionChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    accent: Color? = null,
    showSpinner: Boolean = false
) {
    val borderColor = accent ?: NexarExtraTheme.colors.borderSubtle
    val tint = accent ?: NexarExtraTheme.colors.foregroundSecondary
    Surface(
        modifier = Modifier.height(32.dp),
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
            if (showSpinner) {
                CircularProgressIndicator(
                    modifier = Modifier.size(13.dp),
                    strokeWidth = 1.5.dp,
                    color = tint
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = tint
                )
            }
            Spacer(Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = tint
            )
        }
    }
}

// ─── Empty state ──────────────────────────────────────────────────────────────

@Composable
fun NexarEmptyState(
    filter: DashboardFilter,
    searchQuery: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        NexarLogo(
            size = 72.dp,
            fieldColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
            sheetColor = MaterialTheme.colorScheme.surfaceVariant,
            foldColor = NexarExtraTheme.colors.borderSubtle,
            accentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
        )
        val heading = when {
            searchQuery.isNotBlank() -> "No matches found"
            filter == DashboardFilter.NeedsExport -> "Everything exported"
            filter == DashboardFilter.Starred -> "No starred documents"
            filter != DashboardFilter.All -> "No ${filter.label.lowercase()} found"
            else -> "No documents yet"
        }
        val body = when {
            searchQuery.isNotBlank() -> "Try a different name or search term. OCR text and categories are also searched."
            filter == DashboardFilter.NeedsExport -> "All your scans have been exported."
            filter == DashboardFilter.Starred -> "Tap the star on any document to mark it as a favourite."
            filter != DashboardFilter.All -> "Scan a document and Nexar will auto-detect its category."
            else -> "Tap Scan document below to capture your first document."
        }
        Text(
            text = heading,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = NexarExtraTheme.colors.foregroundSecondary
        )
    }
}

// ─── Storage status ───────────────────────────────────────────────────────────

@Composable
fun LocalStorageStatus(storageLocation: String?) {
    val configured = storageLocation != null
    val dotColor = if (configured) NexarExtraTheme.colors.success else NexarExtraTheme.colors.warning
    val message = if (configured) {
        val path = storageLocation?.shortPath() ?: ""
        "Exports save to $path (category/year folders). Originals stay on this device."
    } else {
        "Local scans stay on this device until you choose an export folder."
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, NexarExtraTheme.colors.borderSubtle, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = NexarExtraTheme.colors.foregroundSecondary,
            lineHeight = MaterialTheme.typography.bodySmall.fontSize * 1.35f
        )
    }
}

private fun String.shortPath() = substringAfterLast('/').ifEmpty { this }.take(36)

// ─── FAB ──────────────────────────────────────────────────────────────────────

@Composable
fun NexarFAB(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = Color(0x330F172A),
                spotColor = Color(0x330F172A)
            ),
        shape = RoundedCornerShape(32.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = NexarOnAccent
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        Icon(
            imageVector = Icons.Default.QrCodeScanner,
            contentDescription = null,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "Scan document",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

// ─── Skeleton loading card ────────────────────────────────────────────────────

@Composable
fun SkeletonDocumentCard() {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton_alpha"
    )
    val shimmer = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, NexarExtraTheme.colors.borderSubtle, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(shimmer)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(shimmer)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.35f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(shimmer)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(28.dp)
                            .clip(CircleShape)
                            .background(shimmer)
                    )
                }
            }
        }
    }
}

// ─── Error banner ─────────────────────────────────────────────────────────────

@Composable
fun NexarErrorBanner(
    error: NexarError,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFFE0E0))
            .border(1.dp, Color(0xFFFFB3B3), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = Color(0xFFB00020),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = error.userMessage(),
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF7B0000),
            modifier = Modifier.weight(1f),
            lineHeight = MaterialTheme.typography.bodySmall.fontSize * 1.4f
        )
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Dismiss",
                tint = Color(0xFFB00020),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ─── Batch exporting banner ───────────────────────────────────────────────────

@Composable
fun BatchExportingBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Exporting documents to storage…",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ─── Utility ──────────────────────────────────────────────────────────────────

@Composable
fun borderStroke(width: Dp, color: Color) = BorderStroke(width, color)

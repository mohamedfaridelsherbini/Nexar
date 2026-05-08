package com.mohamedfaridelsherbini.nexar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mohamedfaridelsherbini.nexar.domain.model.DocumentCategory
import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument
import com.mohamedfaridelsherbini.nexar.ui.components.CategoryPill
import com.mohamedfaridelsherbini.nexar.ui.components.NexarLogo
import com.mohamedfaridelsherbini.nexar.ui.theme.NexarExtraTheme

@Composable
fun DocumentDetailScreen(
    document: ScannedDocument,
    onBack: () -> Unit,
    onSave: (ScannedDocument) -> Unit,
    onExport: (ScannedDocument) -> Unit,
    onShare: ((ScannedDocument) -> Unit)?,
    onPreview: ((ScannedDocument) -> Unit)?,
    exportEnabled: Boolean
) {
    var name by remember(document.id) { mutableStateOf(document.name) }
    var category by remember(document.id) { mutableStateOf(document.category) }
    var tagInput by remember { mutableStateOf("") }
    var tags by remember(document.id) { mutableStateOf(document.tags.toMutableList()) }
    var showCategoryPicker by remember { mutableStateOf(false) }

    val isDirty = name != document.name || category != document.category || tags != document.tags

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        "Document Detail",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (isDirty) {
                        TextButton(
                            onClick = {
                                onSave(
                                    document.copy(
                                        name = name.trim().ifBlank { document.name },
                                        category = category,
                                        tags = tags.toList()
                                    )
                                )
                            }
                        ) {
                            Text("Save", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        Spacer(Modifier.width(64.dp))
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Page thumbnail strip — tap any page to open the full viewer
            val pageCount = document.imageUris.size.coerceAtLeast(1)
            val canPreview = onPreview != null && (document.pdfUri != null || document.imageUris.isNotEmpty())

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                repeat(pageCount) { index ->
                    val currentDocument = document.copy(
                        name = document.name,
                        category = document.category,
                        tags = document.tags
                    )
                    Box(
                        modifier = Modifier
                            .size(width = 88.dp, height = 120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, NexarExtraTheme.colors.borderSubtle, RoundedCornerShape(12.dp))
                            .then(
                                if (canPreview) Modifier.clickable { onPreview.invoke(currentDocument) }
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (canPreview) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = "View document",
                                    modifier = Modifier.size(28.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Page ${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NexarExtraTheme.colors.foregroundMuted
                                )
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                NexarLogo(
                                    size = 36.dp,
                                    fieldColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    accentColor = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Page ${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NexarExtraTheme.colors.foregroundMuted
                                )
                            }
                        }
                    }
                }
            }

            // Name field
            DetailSection(title = "Document name") {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BasicTextField(
                        value = name,
                        onValueChange = { name = it },
                        singleLine = true,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, NexarExtraTheme.colors.borderSubtle, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    )
                }
            }

            // Category picker
            DetailSection(title = "Category") {
                Box {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, NexarExtraTheme.colors.borderSubtle, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        onClick = { showCategoryPicker = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CategoryPill(category)
                                if (category == DocumentCategory.Other) {
                                    Text(
                                        "Other",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = NexarExtraTheme.colors.foregroundSecondary
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = showCategoryPicker,
                        onDismissRequest = { showCategoryPicker = false }
                    ) {
                        DocumentCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.displayName) },
                                leadingIcon = if (cat == category) {
                                    { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                onClick = {
                                    category = cat
                                    showCategoryPicker = false
                                }
                            )
                        }
                    }
                }
            }

            // Tags
            DetailSection(title = "Tags") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, NexarExtraTheme.colors.borderSubtle, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            BasicTextField(
                                value = tagInput,
                                onValueChange = { tagInput = it },
                                singleLine = true,
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                decorationBox = { inner ->
                                    if (tagInput.isEmpty()) {
                                        Text(
                                            "Add a tag…",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = NexarExtraTheme.colors.foregroundMuted
                                        )
                                    }
                                    inner()
                                }
                            )
                        }
                        IconButton(
                            onClick = {
                                val t = tagInput.trim()
                                if (t.isNotBlank() && !tags.contains(t)) {
                                    tags = (tags + t).toMutableList()
                                    tagInput = ""
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add tag", tint = Color.White)
                        }
                    }
                    if (tags.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            tags.forEach { tag ->
                                InputChip(
                                    selected = false,
                                    onClick = { tags = tags.filter { it != tag }.toMutableList() },
                                    label = { Text(tag, style = MaterialTheme.typography.labelSmall) },
                                    trailingIcon = {
                                        Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // OCR summary
            if (document.ocrProcessed) {
                DetailSection(title = "Extracted data") {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (document.extractedAmount != null) {
                            InfoRow(label = "Amount", value = document.extractedAmount)
                        }
                        if (document.extractedDate != null) {
                            InfoRow(label = "Date", value = document.extractedDate)
                        }
                        val exportPath = "${document.category.folderName} / ${epochMillisToYear(document.dateMillis)}"
                        InfoRow(label = "Export path", value = exportPath)
                    }
                }
            }

            // Action buttons
            val currentDocument = document.copy(
                name = name.trim().ifBlank { document.name },
                category = category,
                tags = tags.toList()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (exportEnabled) {
                    Button(
                        onClick = { onExport(currentDocument) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            if (document.isExportedToStorage) Icons.Default.CheckCircle else Icons.Default.CloudUpload,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (document.isExportedToStorage) "Re-export" else "Export",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                if (onShare != null && document.pdfUri != null) {
                    OutlinedButton(
                        onClick = { onShare(currentDocument) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Share", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

private fun epochMillisToYear(millis: Long): String {
    var remaining = millis / 86_400_000L
    var year = 1970
    while (remaining >= 0) {
        val daysInYear = if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 366L else 365L
        if (remaining < daysInYear) break
        remaining -= daysInYear
        year++
    }
    return year.toString()
}

@Composable
private fun DetailSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = NexarExtraTheme.colors.foregroundSecondary
        )
        content()
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, NexarExtraTheme.colors.borderSubtle, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = NexarExtraTheme.colors.foregroundSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

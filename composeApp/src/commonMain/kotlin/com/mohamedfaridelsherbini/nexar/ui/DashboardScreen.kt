package com.mohamedfaridelsherbini.nexar.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument
import com.mohamedfaridelsherbini.nexar.presentation.dashboard.DashboardFilter
import com.mohamedfaridelsherbini.nexar.ui.components.*
import com.mohamedfaridelsherbini.nexar.ui.theme.NexarExtraTheme


@Composable
fun DashboardScreen(
    documents: List<ScannedDocument>,
    storageLocation: String?,
    onScanClick: () -> Unit,
    onRenameClick: (ScannedDocument) -> Unit,
    onDocumentClick: (ScannedDocument) -> Unit,
    onSetStorageClick: () -> Unit,
    onSaveToStorageClick: (ScannedDocument) -> Unit,
    onCreateFolderClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(DashboardFilter.All) }

    val needsExportCount = remember(documents) { documents.count { !it.isExportedToStorage } }

    val visibleDocuments = remember(documents, searchQuery, filter) {
        val byFilter = when (filter) {
            DashboardFilter.All -> documents
            DashboardFilter.NeedsExport -> documents.filter { !it.isExportedToStorage }
            else -> {
                val cat = filter.toCategory()
                if (cat != null) documents.filter { it.category == cat } else documents
            }
        }
        if (searchQuery.isBlank()) byFilter
        else {
            val q = searchQuery.trim().lowercase()
            byFilter.filter { doc ->
                doc.name.lowercase().contains(q) ||
                    doc.ocrText.lowercase().contains(q) ||
                    doc.category.displayName.lowercase().contains(q) ||
                    doc.tags.any { it.lowercase().contains(q) }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 18.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            NexarTopBar(
                onSettingsClick = onSetStorageClick,
                onCreateFolderClick = if (storageLocation != null) onCreateFolderClick else null
            )

            if (storageLocation == null) {
                StorageWarningBanner(onClick = onSetStorageClick)
            }

            NexarSearchInput(
                value = searchQuery,
                onValueChange = { searchQuery = it }
            )

            QuickFilters(
                selected = filter,
                needsExportCount = needsExportCount,
                onSelect = { filter = it }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent documents",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, NexarExtraTheme.colors.borderSubtle)
                ) {
                    Text(
                        text = "${visibleDocuments.size} scans",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = NexarExtraTheme.colors.foregroundSecondary
                    )
                }
            }

            if (visibleDocuments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    NexarEmptyState(filter = filter, searchQuery = searchQuery)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(visibleDocuments) { doc ->
                        DocumentCard(
                            document = doc,
                            exportEnabled = storageLocation != null,
                            onPreviewClick = { onDocumentClick(doc) },
                            onRenameClick = { onRenameClick(doc) },
                            onExportClick = { onSaveToStorageClick(doc) },
                            onConfigureExportClick = onSetStorageClick
                        )
                    }
                }
            }

            LocalStorageStatus(storageLocation = storageLocation)
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            NexarFAB(onClick = onScanClick)
        }
    }
}

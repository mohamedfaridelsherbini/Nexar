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
import com.mohamedfaridelsherbini.nexar.platform.sharePdf
import com.mohamedfaridelsherbini.nexar.presentation.dashboard.DashboardFilter
import com.mohamedfaridelsherbini.nexar.presentation.dashboard.DashboardUiState
import com.mohamedfaridelsherbini.nexar.presentation.dashboard.SortOrder
import com.mohamedfaridelsherbini.nexar.ui.components.BatchExportingBanner
import com.mohamedfaridelsherbini.nexar.ui.components.LocalStorageStatus
import com.mohamedfaridelsherbini.nexar.ui.components.NexarEmptyState
import com.mohamedfaridelsherbini.nexar.ui.components.NexarErrorBanner
import com.mohamedfaridelsherbini.nexar.ui.components.NexarFAB
import com.mohamedfaridelsherbini.nexar.ui.components.NexarSearchInput
import com.mohamedfaridelsherbini.nexar.ui.components.NexarTopBar
import com.mohamedfaridelsherbini.nexar.ui.components.QuickFilters
import com.mohamedfaridelsherbini.nexar.ui.components.SkeletonDocumentCard
import com.mohamedfaridelsherbini.nexar.ui.components.StorageAnalyticsCard
import com.mohamedfaridelsherbini.nexar.ui.components.StorageWarningBanner
import com.mohamedfaridelsherbini.nexar.ui.components.SwipeableDocumentCard
import com.mohamedfaridelsherbini.nexar.ui.theme.NexarExtraTheme

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onScanClick: () -> Unit,
    onRenameClick: (ScannedDocument) -> Unit,
    onDocumentClick: (ScannedDocument) -> Unit,
    onSetStorageClick: () -> Unit,
    onSaveToStorageClick: (ScannedDocument) -> Unit,
    onCreateFolderClick: () -> Unit,
    onDeleteClick: (ScannedDocument) -> Unit,
    onStarClick: (ScannedDocument) -> Unit,
    onSortChanged: (SortOrder) -> Unit,
    onBatchExportClick: () -> Unit,
    onDetailClick: (ScannedDocument) -> Unit,
    onBatchResultDismissed: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onFilterChanged: (DashboardFilter) -> Unit,
    onOcrSheetOpen: (String) -> Unit,
    onOcrSheetDismissed: () -> Unit,
    onErrorDismissed: () -> Unit,
) {
    val storageLocation = uiState.storageLocation
    val needsExportCount = remember(uiState.documents) {
        uiState.documents.count { !it.isExportedToStorage }
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
                onCreateFolderClick = if (storageLocation != null) onCreateFolderClick else null,
                currentSort = uiState.sort,
                onSortChanged = onSortChanged,
                needsExportCount = needsExportCount,
                storageConfigured = storageLocation != null,
                onBatchExportClick = if (storageLocation != null && needsExportCount > 0) onBatchExportClick else null
            )

            if (storageLocation == null) {
                StorageWarningBanner(onClick = onSetStorageClick)
            }

            NexarSearchInput(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChanged
            )

            QuickFilters(
                selected = uiState.activeFilter,
                needsExportCount = needsExportCount,
                onSelect = onFilterChanged
            )

            StorageAnalyticsCard(analytics = uiState.analytics)

            if (uiState.isBatchExporting) {
                BatchExportingBanner()
            }

            uiState.error?.let { error ->
                NexarErrorBanner(error = error, onDismiss = onErrorDismissed)
            }

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
                        text = "${uiState.visibleDocuments.size} scans",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = NexarExtraTheme.colors.foregroundSecondary
                    )
                }
            }

            when {
                uiState.isLoadingDocuments -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(3) { SkeletonDocumentCard() }
                    }
                }
                uiState.visibleDocuments.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        NexarEmptyState(
                            filter = uiState.activeFilter,
                            searchQuery = uiState.searchQuery
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.visibleDocuments, key = { it.id }) { doc ->
                            SwipeableDocumentCard(
                                document = doc,
                                exportEnabled = storageLocation != null,
                                isProcessing = uiState.processingDocumentId == doc.id,
                                isExporting = uiState.exportingDocumentId == doc.id,
                                onPreviewClick = { onDocumentClick(doc) },
                                onRenameClick = { onRenameClick(doc) },
                                onExportClick = { onSaveToStorageClick(doc) },
                                onConfigureExportClick = onSetStorageClick,
                                onDeleteClick = { onDeleteClick(doc) },
                                onStarClick = { onStarClick(doc) },
                                onOcrViewClick = if (doc.ocrProcessed && doc.ocrText.isNotBlank()) {
                                    { onOcrSheetOpen(doc.id) }
                                } else null,
                                onShareClick = if (doc.pdfUri != null) {
                                    { sharePdf(doc.pdfUri, doc.name) }
                                } else null,
                                onDetailClick = { onDetailClick(doc) }
                            )
                        }
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

    // OCR text bottom sheet — driven by ViewModel state
    val ocrDoc = uiState.documents.find { it.id == uiState.ocrSheetDocumentId }
    if (ocrDoc != null) {
        OcrTextSheet(document = ocrDoc, onDismiss = onOcrSheetDismissed)
    }

    // Batch export result dialog
    uiState.batchExportResult?.let { (success, failed) ->
        AlertDialog(
            onDismissRequest = onBatchResultDismissed,
            title = { Text("Batch export complete") },
            text = {
                Text(
                    "$success document${if (success != 1) "s" else ""} exported successfully" +
                        if (failed > 0) ", $failed failed." else "."
                )
            },
            confirmButton = {
                TextButton(onClick = onBatchResultDismissed) { Text("OK") }
            }
        )
    }
}

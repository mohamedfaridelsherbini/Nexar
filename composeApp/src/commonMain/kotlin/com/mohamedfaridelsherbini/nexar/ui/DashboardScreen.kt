package com.mohamedfaridelsherbini.nexar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 18.dp, bottom = 100.dp), // Extra bottom padding for FAB
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Top Bar
            NexarTopBar(
                onSettingsClick = onSetStorageClick,
                onCreateFolderClick = if (storageLocation != null) onCreateFolderClick else null
            )

            // Storage Warning
            if (storageLocation == null) {
                StorageWarningBanner(onClick = onSetStorageClick)
            }

            // Search Input
            NexarSearchInput(
                value = searchQuery,
                onValueChange = { searchQuery = it }
            )

            // Documents Header
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
                    border = borderStroke(1.dp, NexarExtraTheme.colors.borderSubtle)
                ) {
                    Text(
                        text = "${documents.size} scans",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = NexarExtraTheme.colors.foregroundSecondary
                    )
                }
            }

            // Document List
            if (documents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No documents scanned yet.",
                        color = NexarExtraTheme.colors.foregroundSecondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(documents) { doc ->
                        DocumentCard(
                            document = doc,
                            onPreviewClick = { onDocumentClick(doc) },
                            onRenameClick = { onRenameClick(doc) },
                            onExportClick = { onSaveToStorageClick(doc) }
                        )
                    }
                }
            }

            // Local Storage Status
            LocalStorageStatus()
        }

        // Dominant Scan FAB
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            NexarFAB(onClick = onScanClick)
        }
    }
}

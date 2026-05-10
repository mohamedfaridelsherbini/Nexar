package com.mohamedfaridelsherbini.nexar

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument
import com.mohamedfaridelsherbini.nexar.navigation.Dashboard
import com.mohamedfaridelsherbini.nexar.navigation.DocumentDetail
import com.mohamedfaridelsherbini.nexar.navigation.Scanner
import com.mohamedfaridelsherbini.nexar.platform.NexarPrefs
import com.mohamedfaridelsherbini.nexar.platform.sharePdf
import com.mohamedfaridelsherbini.nexar.presentation.dashboard.DashboardViewModel
import com.mohamedfaridelsherbini.nexar.storage.StoragePickerBridge
import com.mohamedfaridelsherbini.nexar.ui.DashboardScreen
import com.mohamedfaridelsherbini.nexar.ui.DocumentDetailScreen
import com.mohamedfaridelsherbini.nexar.ui.OnboardingScreen
import com.mohamedfaridelsherbini.nexar.ui.theme.NexarExtraTheme
import com.mohamedfaridelsherbini.nexar.ui.theme.NexarTheme
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Composable
fun App() {
    NexarTheme(dynamicColor = false) {
        var onboardingComplete by remember { mutableStateOf(NexarPrefs.isOnboardingComplete) }

        if (!onboardingComplete) {
            OnboardingScreen(
                onComplete = {
                    NexarPrefs.isOnboardingComplete = true
                    onboardingComplete = true
                },
            )
            return@NexarTheme
        }
        val navStateConfiguration =
            remember {
                SavedStateConfiguration {
                    serializersModule =
                        SerializersModule {
                            polymorphic(NavKey::class) {
                                subclass(Dashboard::class, Dashboard.serializer())
                                subclass(Scanner::class, Scanner.serializer())
                                subclass(DocumentDetail::class, DocumentDetail.serializer())
                            }
                        }
                }
            }
        val backStack = rememberNavBackStack(navStateConfiguration, Dashboard)
        val dashboardViewModel: DashboardViewModel = koinViewModel()
        val uiState by dashboardViewModel.uiState.collectAsState()

        var documentToRename by remember { mutableStateOf<ScannedDocument?>(null) }
        var documentToPreview by remember { mutableStateOf<ScannedDocument?>(null) }
        var showStoragePicker by remember { mutableStateOf(false) }
        var showCreateFolderDialog by remember { mutableStateOf(false) }

        NavDisplay(
            backStack = backStack,
            modifier = Modifier.fillMaxSize(),
            onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) },
            entryProvider =
                entryProvider {
                    entry<Dashboard> {
                        DashboardScreen(
                            uiState = uiState,
                            onScanClick = { backStack.add(Scanner()) },
                            onRenameClick = { documentToRename = it },
                            onDocumentClick = { doc -> documentToPreview = doc },
                            onSetStorageClick = { showStoragePicker = true },
                            onSaveToStorageClick = { dashboardViewModel.onSaveDocumentToStorage(it) },
                            onCreateFolderClick = { showCreateFolderDialog = true },
                            onDeleteClick = { dashboardViewModel.onDeleteDocument(it) },
                            onStarClick = { dashboardViewModel.onToggleStar(it) },
                            onSortChanged = { dashboardViewModel.onSortChanged(it) },
                            onBatchExportClick = { dashboardViewModel.onBatchExport() },
                            onDetailClick = { backStack.add(DocumentDetail(it.id)) },
                            onBatchResultDismissed = { dashboardViewModel.onBatchResultDismissed() },
                            onSearchQueryChanged = { dashboardViewModel.onSearchQueryChanged(it) },
                            onFilterChanged = { dashboardViewModel.onFilterChanged(it) },
                            onOcrSheetOpen = { dashboardViewModel.onOcrSheetOpen(it) },
                            onOcrSheetDismissed = { dashboardViewModel.onOcrSheetDismissed() },
                            onErrorDismissed = { dashboardViewModel.onErrorDismissed() },
                        )
                    }
                    entry<Scanner> {
                        ScannerBridge(
                            onResult = { doc ->
                                dashboardViewModel.onDocumentScanned(doc)
                                backStack.removeAt(backStack.size - 1)
                            },
                            onCancel = {
                                backStack.removeAt(backStack.size - 1)
                            },
                        )
                    }
                    entry<DocumentDetail> { key ->
                        val doc = uiState.documents.find { it.id == key.documentId }
                        if (doc != null) {
                            DocumentDetailScreen(
                                document = doc,
                                onBack = { backStack.removeAt(backStack.size - 1) },
                                onSave = { updated -> dashboardViewModel.onSaveDocumentUpdate(updated) },
                                onExport = { dashboardViewModel.onSaveDocumentToStorage(it) },
                                onShare =
                                    if (doc.pdfUri != null) {
                                        { d -> sharePdf(d.pdfUri!!, d.name) }
                                    } else {
                                        null
                                    },
                                onPreview =
                                    if (doc.pdfUri != null || doc.imageUris.isNotEmpty()) {
                                        { d -> documentToPreview = d }
                                    } else {
                                        null
                                    },
                                exportEnabled = uiState.storageLocation != null,
                            )
                        }
                    }
                },
        )

        documentToPreview?.let { doc ->
            com.mohamedfaridelsherbini.nexar.ui.PreviewBridge(
                document = doc,
                onDismiss = { documentToPreview = null },
            )
        }

        documentToRename?.let { doc ->
            RenameDialog(
                currentName = doc.name,
                onConfirm = { newName ->
                    dashboardViewModel.onRenameDocument(doc.id, newName)
                    documentToRename = null
                },
                onDismiss = { documentToRename = null },
            )
        }

        if (showStoragePicker) {
            StoragePickerBridge(
                onResult = { uri: String ->
                    dashboardViewModel.onStorageLocationSelected(uri)
                    showStoragePicker = false
                },
                onCancel = { showStoragePicker = false },
            )
        }

        if (showCreateFolderDialog) {
            CreateFolderDialog(
                onConfirm = { folderName ->
                    dashboardViewModel.onCreateFolder(folderName)
                    showCreateFolderDialog = false
                },
                onDismiss = { showCreateFolderDialog = false },
            )
        }
    }
}

@Composable
fun CreateFolderDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    NexarDialog(
        title = "New folder",
        confirmLabel = "Create",
        confirmEnabled = name.isNotBlank(),
        onConfirm = { onConfirm(name) },
        onDismiss = onDismiss,
    ) {
        NexarDialogField(
            value = name,
            onValueChange = { name = it },
            placeholder = "Folder name",
        )
    }
}

@Composable
fun RenameDialog(
    currentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(currentName) }
    NexarDialog(
        title = "Rename document",
        confirmLabel = "Rename",
        confirmEnabled = name.isNotBlank() && name != currentName,
        onConfirm = { onConfirm(name) },
        onDismiss = onDismiss,
    ) {
        NexarDialogField(
            value = name,
            onValueChange = { name = it },
            placeholder = "Document name",
        )
    }
}

@Composable
private fun NexarDialog(
    title: String,
    confirmLabel: String,
    confirmEnabled: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = { content() },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = confirmEnabled,
                shape = RoundedCornerShape(999.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.outline,
                    ),
            ) {
                Text(confirmLabel, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(999.dp),
            ) {
                Text(
                    "Cancel",
                    color = NexarExtraTheme.colors.foregroundSecondary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
    )
}

@Composable
private fun NexarDialogField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border =
            androidx.compose.foundation.BorderStroke(
                1.dp,
                NexarExtraTheme.colors.borderSubtle,
            ),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            textStyle =
                MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = NexarExtraTheme.colors.foregroundMuted,
                    )
                }
                inner()
            },
        )
    }
}

@Composable
@Suppress("FunctionName")
expect fun ScannerBridge(
    onResult: (ScannedDocument) -> Unit,
    onCancel: () -> Unit,
)

package com.mohamedfaridelsherbini.nexar

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.mohamedfaridelsherbini.nexar.navigation.Dashboard
import com.mohamedfaridelsherbini.nexar.navigation.Scanner
import com.mohamedfaridelsherbini.nexar.di.getAppContainer
import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument
import com.mohamedfaridelsherbini.nexar.presentation.dashboard.DashboardViewModel
import com.mohamedfaridelsherbini.nexar.ui.DashboardScreen
import com.mohamedfaridelsherbini.nexar.ui.theme.NexarTheme
import com.mohamedfaridelsherbini.nexar.storage.StoragePickerBridge
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Composable
fun App() {
    NexarTheme {
        val appContainer = remember { getAppContainer() }
        val navStateConfiguration = remember {
            SavedStateConfiguration {
                serializersModule =
                    SerializersModule {
                        polymorphic(NavKey::class) {
                            subclass(Dashboard::class, Dashboard.serializer())
                            subclass(Scanner::class, Scanner.serializer())
                        }
                    }
            }
        }
        val backStack = rememberNavBackStack(navStateConfiguration, Dashboard)
        val dashboardViewModel: DashboardViewModel = viewModel {
            DashboardViewModel(appContainer.dashboardUseCases)
        }
        val uiState by dashboardViewModel.uiState.collectAsState()

        var documentToRename by remember { mutableStateOf<ScannedDocument?>(null) }
        var documentToPreview by remember { mutableStateOf<ScannedDocument?>(null) }
        var showStoragePicker by remember { mutableStateOf(false) }
        var showCreateFolderDialog by remember { mutableStateOf(false) }

        NavDisplay(
            backStack = backStack,
            modifier = Modifier.fillMaxSize(),
            onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) },
            entryProvider = entryProvider {
                entry<Dashboard> {
                    DashboardScreen(
                        documents = uiState.documents,
                        storageLocation = uiState.storageLocation,
                        onScanClick = { backStack.add(Scanner()) },
                        onRenameClick = { documentToRename = it },
                        onDocumentClick = { documentToPreview = it },
                        onSetStorageClick = { showStoragePicker = true },
                        onSaveToStorageClick = { dashboardViewModel.onSaveDocumentToStorage(it) },
                        onCreateFolderClick = { showCreateFolderDialog = true }
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
                        }
                    )
                }
            }
        )

        if (documentToRename != null) {
            RenameDialog(
                currentName = documentToRename!!.name,
                onConfirm = { newName ->
                    dashboardViewModel.onRenameDocument(documentToRename!!.id, newName)
                    documentToRename = null
                },
                onDismiss = { documentToRename = null }
            )
        }

        if (documentToPreview != null) {
            com.mohamedfaridelsherbini.nexar.ui.PreviewBridge(
                document = documentToPreview!!,
                onDismiss = { documentToPreview = null }
            )
        }

        if (showStoragePicker) {
            StoragePickerBridge(
                onResult = { uri: String ->
                    dashboardViewModel.onStorageLocationSelected(uri)
                    showStoragePicker = false
                },
                onCancel = { showStoragePicker = false }
            )
        }

        if (showCreateFolderDialog) {
            CreateFolderDialog(
                onConfirm = { folderName ->
                    dashboardViewModel.onCreateFolder(folderName)
                    showCreateFolderDialog = false
                },
                onDismiss = { showCreateFolderDialog = false }
            )
        }
    }
}

@Composable
fun CreateFolderDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Folder") },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Folder name") }
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(name) }) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RenameDialog(
    currentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Document") },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(name) }) {
                Text("Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
expect fun ScannerBridge(
    onResult: (ScannedDocument) -> Unit,
    onCancel: () -> Unit
)

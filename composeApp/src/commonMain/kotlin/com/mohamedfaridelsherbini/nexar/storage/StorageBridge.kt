package com.mohamedfaridelsherbini.nexar.storage

import androidx.compose.runtime.Composable

@Composable
expect fun StoragePickerBridge(
    onResult: (String) -> Unit,
    onCancel: () -> Unit,
)

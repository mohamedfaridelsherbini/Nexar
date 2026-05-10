package com.mohamedfaridelsherbini.nexar.storage

import androidx.compose.runtime.Composable

@Composable
@Suppress("FunctionName")
expect fun StoragePickerBridge(
    onResult: (String) -> Unit,
    onCancel: () -> Unit,
)

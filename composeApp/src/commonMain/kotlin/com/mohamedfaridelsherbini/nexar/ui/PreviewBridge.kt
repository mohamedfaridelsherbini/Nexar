package com.mohamedfaridelsherbini.nexar.ui

import androidx.compose.runtime.Composable
import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument

@Composable
expect fun PreviewBridge(
    document: ScannedDocument,
    onDismiss: () -> Unit,
)

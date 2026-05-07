package com.mohamedfaridelsherbini.nexar.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument

@Composable
actual fun PreviewBridge(
    document: ScannedDocument,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(document) {
        val uriString = document.pdfUri ?: document.imageUris.firstOrNull()
        if (uriString != null) {
            val uri = Uri.parse(uriString)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, if (document.pdfUri != null) "application/pdf" else "image/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            try {
                context.startActivity(Intent.createChooser(intent, "Open Document"))
            } catch (e: Exception) {
                // Fallback or error toast
            }
        }
        onDismiss()
    }
}

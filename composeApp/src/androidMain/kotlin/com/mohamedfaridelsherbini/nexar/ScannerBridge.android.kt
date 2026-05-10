package com.mohamedfaridelsherbini.nexar

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument

@Composable
actual fun ScannerBridge(
    onResult: (ScannedDocument) -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current
    val scannerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult(),
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val gmsResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
                if (gmsResult != null) {
                    val document =
                        ScannedDocument(
                            id = java.util.UUID.randomUUID().toString(),
                            name =
                                "Scan ${
                                    java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)
                                        .format(java.util.Date())
                                }",
                            dateMillis = System.currentTimeMillis(),
                            imageUris = gmsResult.pages?.map { it.imageUri.toString() } ?: emptyList(),
                            pdfUri = gmsResult.pdf?.uri?.toString(),
                        )
                    onResult(document)
                } else {
                    onCancel()
                }
            } else {
                onCancel()
            }
        }

    val options =
        remember {
            GmsDocumentScannerOptions.Builder()
                .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
                .setGalleryImportAllowed(true)
                .setResultFormats(
                    GmsDocumentScannerOptions.RESULT_FORMAT_JPEG,
                    GmsDocumentScannerOptions.RESULT_FORMAT_PDF,
                )
                .build()
        }

    val scanner = remember { GmsDocumentScanning.getClient(options) }

    LaunchedEffect(Unit) {
        scanner.getStartScanIntent(context as Activity)
            .addOnSuccessListener { intentSender ->
                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener {
                onCancel()
            }
    }
}

package com.mohamedfaridelsherbini.nexar.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument
import platform.Foundation.NSURL
import platform.PDFKit.PDFDocument
import platform.PDFKit.PDFView
import platform.UIKit.UIImage
import platform.UIKit.UIImageView
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PreviewBridge(
    document: ScannedDocument,
    onDismiss: () -> Unit,
) {
    when {
        document.pdfUri != null ->
            PdfViewerDialog(
                pdfUriString = document.pdfUri,
                title = document.name,
                onDismiss = onDismiss,
            )
        document.imageUris.isNotEmpty() ->
            ImageViewerDialog(
                imageUriStrings = document.imageUris,
                title = document.name,
                onDismiss = onDismiss,
            )
        else -> LaunchedEffect(Unit) { onDismiss() }
    }
}

// ── PDF viewer — uses PDFKit.PDFView via UIKitView ────────────────────────────

@OptIn(ExperimentalForeignApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun PdfViewerDialog(
    pdfUriString: String,
    title: String,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Scaffold(
            topBar = { ViewerTopBar(title = title, onDismiss = onDismiss) },
        ) { padding ->
            UIKitView(
                factory = {
                    val pdfView = PDFView()
                    val url = pdfUriString.toNSURL()
                    if (url != null) {
                        val doc = PDFDocument(url)
                        pdfView.document = doc
                    }
                    pdfView.autoScales = true
                    pdfView
                },
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
            )
        }
    }
}

// ── Image viewer — UIImageView for image-only documents (fallback) ────────────

@OptIn(ExperimentalForeignApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ImageViewerDialog(
    imageUriStrings: List<String>,
    title: String,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Scaffold(
            topBar = {
                ViewerTopBar(
                    title = title,
                    pageCount = imageUriStrings.size,
                    onDismiss = onDismiss,
                )
            },
        ) { padding ->
            UIKitView(
                factory = {
                    val imageView = UIImageView()
                    // Multipage image-only documents are rare (scanner normally creates a PDF).
                    // Show the first page; UIKitView fills the available modifier space.
                    val path = imageUriStrings.firstOrNull()?.toLocalFilePath()
                    if (path != null) {
                        imageView.image = UIImage.imageWithContentsOfFile(path)
                    }
                    imageView
                },
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
            )
        }
    }
}

// ── Shared top bar ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ViewerTopBar(
    title: String,
    pageCount: Int = 0,
    onDismiss: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        },
        navigationIcon = {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close viewer")
            }
        },
        actions = {
            if (pageCount > 1) {
                Text(
                    text = "$pageCount pages",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 16.dp),
                )
            }
        },
    )
}

// ── URI helpers ───────────────────────────────────────────────────────────────

/**
 * Converts a URI string to an [NSURL] suitable for [PDFDocument].
 * Handles bare paths, `file:///path`, and other URL strings.
 */
private fun String.toNSURL(): NSURL? =
    when {
        startsWith("file://") -> NSURL.fileURLWithPath(removePrefix("file://"))
        startsWith("/") -> NSURL.fileURLWithPath(this)
        else -> NSURL.URLWithString(this)
    }

/**
 * Returns a raw filesystem path for use with [UIImage.imageWithContentsOfFile].
 * Returns null for non-local URIs (e.g. content://).
 */
private fun String.toLocalFilePath(): String? =
    when {
        startsWith("file:///") -> removePrefix("file://")
        startsWith("file://") -> removePrefix("file://")
        startsWith("/") -> this
        else -> null
    }

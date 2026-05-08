package com.mohamedfaridelsherbini.nexar.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
actual fun PreviewBridge(
    document: ScannedDocument,
    onDismiss: () -> Unit
) {
    when {
        document.pdfUri != null -> PdfViewerDialog(
            pdfUriString = document.pdfUri,
            title = document.name,
            onDismiss = onDismiss
        )
        document.imageUris.isNotEmpty() -> ImageViewerDialog(
            imageUriStrings = document.imageUris,
            title = document.name,
            onDismiss = onDismiss
        )
        else -> LaunchedEffect(Unit) { onDismiss() }
    }
}

// ── PDF viewer ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PdfViewerDialog(pdfUriString: String, title: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        val context = LocalContext.current
        val pages by produceState<List<Bitmap>>(initialValue = emptyList(), pdfUriString) {
            value = withContext(Dispatchers.IO) { renderPdfPages(context, pdfUriString) }
        }

        Scaffold(
            topBar = {
                ViewerTopBar(
                    title = title,
                    pageCount = pages.size,
                    onDismiss = onDismiss
                )
            }
        ) { padding ->
            ViewerContent(
                items = pages,
                modifier = Modifier.padding(padding),
                loadingCondition = pages.isEmpty()
            ) { index, bitmap ->
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Page ${index + 1}",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (pages.size > 1) {
                            PageLabel(index = index, total = pages.size)
                        }
                    }
                }
            }
        }
    }
}

private fun renderPdfPages(context: Context, uriString: String): List<Bitmap> {
    return try {
        val pfd = openParcelFileDescriptor(context, uriString) ?: return emptyList()
        pfd.use { descriptor ->
            PdfRenderer(descriptor).use { renderer ->
                val screenWidth = context.resources.displayMetrics.widthPixels
                (0 until renderer.pageCount).map { index ->
                    renderer.openPage(index).use { page ->
                        val scale = screenWidth.toFloat() / page.width
                        val bitmapWidth = screenWidth
                        val bitmapHeight = (page.height * scale).toInt()
                        Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
                            .also { bmp ->
                                bmp.eraseColor(Color.WHITE)
                                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            }
                    }
                }
            }
        }
    } catch (e: Exception) {
        emptyList()
    }
}

// ── Image viewer ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImageViewerDialog(
    imageUriStrings: List<String>,
    title: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        val context = LocalContext.current
        val bitmaps by produceState<List<Bitmap>>(initialValue = emptyList(), imageUriStrings) {
            value = withContext(Dispatchers.IO) {
                imageUriStrings.mapNotNull { uriString ->
                    try { loadBitmap(context, uriString) } catch (e: Exception) { null }
                }
            }
        }

        Scaffold(
            topBar = {
                ViewerTopBar(
                    title = title,
                    pageCount = bitmaps.size,
                    onDismiss = onDismiss
                )
            }
        ) { padding ->
            ViewerContent(
                items = bitmaps,
                modifier = Modifier.padding(padding),
                loadingCondition = bitmaps.isEmpty()
            ) { index, bitmap ->
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Page ${index + 1}",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (bitmaps.size > 1) {
                            PageLabel(index = index, total = bitmaps.size)
                        }
                    }
                }
            }
        }
    }
}

private fun loadBitmap(context: Context, uriString: String): Bitmap? {
    val uri = Uri.parse(uriString)
    return if (uri.scheme == "content") {
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
    } else {
        val path = if (uri.scheme == "file") uri.path ?: uriString else uriString
        BitmapFactory.decodeFile(path)
    }
}

// ── Shared helpers ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ViewerTopBar(title: String, pageCount: Int, onDismiss: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close viewer")
            }
        },
        actions = {
            if (pageCount > 0) {
                Text(
                    text = "$pageCount page${if (pageCount != 1) "s" else ""}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }
        }
    )
}

@Composable
private fun <T> ViewerContent(
    items: List<T>,
    modifier: Modifier = Modifier,
    loadingCondition: Boolean,
    itemContent: @Composable (index: Int, item: T) -> Unit
) {
    if (loadingCondition) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            itemsIndexed(items) { index, item ->
                itemContent(index, item)
            }
        }
    }
}

@Composable
private fun PageLabel(index: Int, total: Int) {
    Text(
        text = "Page ${index + 1} of $total",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

private fun openParcelFileDescriptor(context: Context, uriString: String): ParcelFileDescriptor? {
    return try {
        val uri = Uri.parse(uriString)
        when (uri.scheme) {
            "content" -> context.contentResolver.openFileDescriptor(uri, "r")
            "file" -> ParcelFileDescriptor.open(
                File(uri.path ?: return null),
                ParcelFileDescriptor.MODE_READ_ONLY
            )
            else -> ParcelFileDescriptor.open(File(uriString), ParcelFileDescriptor.MODE_READ_ONLY)
        }
    } catch (e: Exception) {
        null
    }
}

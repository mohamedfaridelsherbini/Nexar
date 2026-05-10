@file:Suppress("StaticFieldLeak")

package com.mohamedfaridelsherbini.nexar.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri

private var shareContext: Context? = null

fun initShare(context: Context) {
    shareContext = context.applicationContext
}

actual fun sharePdf(
    pdfUri: String,
    documentName: String,
) {
    val ctx = shareContext ?: return
    val uri = pdfUri.toUri()
    val intent =
        Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, documentName)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    ctx.startActivity(
        Intent.createChooser(intent, "Share $documentName").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        },
    )
}

package com.mohamedfaridelsherbini.nexar.domain.usecase

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private lateinit var ocrContext: Context

fun initOcr(context: Context) {
    ocrContext = context.applicationContext
}

actual fun createOcrProcessor(): OcrProcessor = AndroidOcrProcessor()

private class AndroidOcrProcessor : OcrProcessor {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun extractText(imageUris: List<String>): String {
        val parts = imageUris.mapNotNull { uriStr ->
            runCatching {
                val image = InputImage.fromFilePath(ocrContext, Uri.parse(uriStr))
                suspendCancellableCoroutine<String> { cont ->
                    recognizer.process(image)
                        .addOnSuccessListener { result -> cont.resume(result.text) }
                        .addOnFailureListener { cont.resume("") }
                }
            }.getOrNull()
        }
        return parts.filter { it.isNotBlank() }.joinToString("\n").trim()
    }
}

@file:Suppress("StaticFieldLeak")

package com.mohamedfaridelsherbini.nexar.domain.usecase

import android.content.Context
import androidx.core.net.toUri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

private lateinit var ocrContext: Context

fun initOcr(context: Context) {
    ocrContext = context.applicationContext
}

actual fun createOcrProcessor(): OcrProcessor = AndroidOcrProcessor()

private class AndroidOcrProcessor : OcrProcessor {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun extractText(imageUris: List<String>): String {
        val parts =
            imageUris.mapNotNull { uriStr ->
                runCatching {
                    val image = InputImage.fromFilePath(ocrContext, uriStr.toUri())
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

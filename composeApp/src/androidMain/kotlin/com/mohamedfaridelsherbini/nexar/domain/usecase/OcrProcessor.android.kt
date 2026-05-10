@file:Suppress("StaticFieldLeak")

package com.mohamedfaridelsherbini.nexar.domain.usecase

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import androidx.core.net.toUri

import org.koin.core.context.GlobalContext

actual fun createOcrProcessor(): OcrProcessor = AndroidOcrProcessor(GlobalContext.get().get())

private class AndroidOcrProcessor(private val context: Context) : OcrProcessor {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun extractText(imageUris: List<String>): String {
        val parts =
            imageUris.mapNotNull { uriStr ->
                runCatching {
                    val image = InputImage.fromFilePath(context, uriStr.toUri())
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

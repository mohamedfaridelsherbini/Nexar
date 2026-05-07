package com.mohamedfaridelsherbini.nexar.domain.usecase

interface OcrProcessor {
    /** Extract concatenated text from the given list of image file URIs/paths. */
    suspend fun extractText(imageUris: List<String>): String
}

expect fun createOcrProcessor(): OcrProcessor

package com.mohamedfaridelsherbini.nexar.domain.usecase

// The KMP iOS Compose target delegates OCR to the native Swift DocumentStore
// (which runs VNRecognizeTextRequest on UIImages directly). Documents saved via
// the Compose app start with ocrProcessed=false; OCR text can be added in a
// future release through a dedicated background service bridge.
actual fun createOcrProcessor(): OcrProcessor =
    object : OcrProcessor {
        override suspend fun extractText(imageUris: List<String>): String = ""
    }

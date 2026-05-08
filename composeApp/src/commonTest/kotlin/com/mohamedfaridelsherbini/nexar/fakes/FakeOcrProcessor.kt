package com.mohamedfaridelsherbini.nexar.fakes

import com.mohamedfaridelsherbini.nexar.domain.usecase.OcrProcessor

class FakeOcrProcessor(private val text: String = "") : OcrProcessor {
    var callCount = 0
        private set

    override suspend fun extractText(imageUris: List<String>): String {
        callCount++
        return text
    }
}

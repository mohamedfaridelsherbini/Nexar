package com.mohamedfaridelsherbini.nexar.domain.classifier

import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument

object DuplicateDetector : DuplicateDetectionService {

    private const val SIMILARITY_THRESHOLD = 0.75

    private fun tokenize(text: String): Set<String> =
        text.lowercase()
            .split(Regex("""\s+"""))
            .filter { it.length > 2 }
            .toSet()

    private fun jaccard(a: Set<String>, b: Set<String>): Double {
        if (a.isEmpty() && b.isEmpty()) return 0.0
        val intersection = a.intersect(b).size
        val union = a.union(b).size
        return if (union == 0) 0.0 else intersection.toDouble() / union.toDouble()
    }

    override fun findDuplicate(newDoc: ScannedDocument, existingDocs: List<ScannedDocument>): String? {
        if (newDoc.ocrText.isBlank()) return null
        val newTokens = tokenize(newDoc.ocrText)
        if (newTokens.isEmpty()) return null

        var bestId: String? = null
        var bestScore = SIMILARITY_THRESHOLD

        for (doc in existingDocs) {
            if (doc.id == newDoc.id || doc.ocrText.isBlank()) continue
            val score = jaccard(newTokens, tokenize(doc.ocrText))
            if (score > bestScore) {
                bestScore = score
                bestId = doc.id
            }
        }
        return bestId
    }
}

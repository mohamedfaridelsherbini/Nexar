package com.mohamedfaridelsherbini.nexar.domain.classifier

import com.mohamedfaridelsherbini.nexar.domain.model.DocumentCategory

object DocumentClassifier {
    private val receiptKeywords = setOf(
        "receipt", "subtotal", "cashier", "store", "thank you for shopping",
        "change", "loyalty", "supermarket", "hypermarket", "carrefour", "walmart",
        "grocery", "amount due", "amount paid", "your total"
    )
    private val invoiceKeywords = setOf(
        "invoice", "bill to", "billing address", "due date", "payment due",
        "invoice number", "inv #", "invoice #", "vat", "net amount",
        "line item", "unit price", "qty", "quantity", "subtotal", "balance due"
    )
    private val idDocumentKeywords = setOf(
        "passport", "national id", "identity card", "driver's license",
        "driving licence", "date of birth", "nationality", "place of birth",
        "license no", "id number", "personal no", "sex", "issuing authority",
        "expiry date", "expiration date", "mrz", "bearer"
    )
    private val contractKeywords = setOf(
        "agreement", "contract", "terms and conditions", "hereby agrees",
        "parties agree", "binding agreement", "obligation", "whereas",
        "in witness whereof", "hereinafter", "indemnification", "arbitration",
        "governing law", "clause", "schedule", "exhibit", "addendum"
    )
    private val medicalKeywords = setOf(
        "patient", "diagnosis", "prescription", "doctor", "physician",
        "hospital", "clinic", "medication", "dosage", "mg", "ml",
        "treatment", "referral", "lab result", "blood pressure", "test result",
        "medical record", "healthcare"
    )

    fun classify(ocrText: String): DocumentCategory {
        if (ocrText.isBlank()) return DocumentCategory.Other
        val lower = ocrText.lowercase()
        val scores = mapOf(
            DocumentCategory.IdDocument to idDocumentKeywords.count { lower.contains(it) },
            DocumentCategory.Contract to contractKeywords.count { lower.contains(it) },
            DocumentCategory.Medical to medicalKeywords.count { lower.contains(it) },
            DocumentCategory.Invoice to invoiceKeywords.count { lower.contains(it) },
            DocumentCategory.Receipt to receiptKeywords.count { lower.contains(it) }
        )
        val best = scores.maxByOrNull { it.value }
        return if ((best?.value ?: 0) >= 2) best!!.key else DocumentCategory.Other
    }

    fun extractTags(ocrText: String): List<String> {
        if (ocrText.isBlank()) return emptyList()
        val lower = ocrText.lowercase()
        val tags = mutableListOf<String>()
        val allKeywords = mapOf(
            "receipt" to receiptKeywords,
            "invoice" to invoiceKeywords,
            "id" to idDocumentKeywords,
            "contract" to contractKeywords,
            "medical" to medicalKeywords
        )
        for ((label, keywords) in allKeywords) {
            if (keywords.count { lower.contains(it) } >= 1) tags.add(label)
        }
        return tags.distinct()
    }
}

object DocumentNamer {
    fun suggest(ocrText: String, category: DocumentCategory, dateMillis: Long): String? {
        if (ocrText.isBlank()) return null
        val dateStr = formatDate(dateMillis)
        val excerpt = extractMeaningfulExcerpt(ocrText)
        return when (category) {
            DocumentCategory.Receipt -> if (excerpt != null) "Receipt - $excerpt - $dateStr" else "Receipt - $dateStr"
            DocumentCategory.Invoice -> if (excerpt != null) "Invoice - $excerpt - $dateStr" else "Invoice - $dateStr"
            DocumentCategory.IdDocument -> if (excerpt != null) "ID - $excerpt - $dateStr" else "ID Document - $dateStr"
            DocumentCategory.Contract -> if (excerpt != null) "Contract - $excerpt - $dateStr" else "Contract - $dateStr"
            DocumentCategory.Medical -> if (excerpt != null) "Medical - $excerpt - $dateStr" else "Medical - $dateStr"
            DocumentCategory.Other -> null
        }
    }

    private fun extractMeaningfulExcerpt(text: String): String? {
        val lines = text.lines().map { it.trim() }.filter { it.length in 3..40 }
        return lines.firstOrNull { line ->
            line.any { it.isLetter() } &&
                !line.all { it.isDigit() || it.isWhitespace() || it == '/' || it == '-' }
        }?.take(30)?.trimEnd()
    }

    private fun formatDate(millis: Long): String {
        val secs = millis / 1000
        val days = secs / 86400
        val epoch = 719162L
        val z = days + epoch
        val era = (if (z >= 0) z else z - 146096) / 146097
        val doe = z - era * 146097
        val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
        val y = yoe + era * 400
        val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
        val mp = (5 * doy + 2) / 153
        val d = doy - (153 * mp + 2) / 5 + 1
        val m = mp + (if (mp < 10) 3 else -9)
        val yr = y + (if (m <= 2) 1 else 0)
        return "${yr.toString().padStart(4, '0')}-${m.toString().padStart(2, '0')}-${d.toString().padStart(2, '0')}"
    }
}

package com.mohamedfaridelsherbini.nexar.domain.classifier

import com.mohamedfaridelsherbini.nexar.domain.model.DocumentCategory

private data class ClassificationRule(
    val category: DocumentCategory,
    val label: String,
    val keywords: Set<String>
)

private val classificationRules = listOf(
    ClassificationRule(
        DocumentCategory.IdDocument, "id", setOf(
            "passport", "national id", "identity card", "driver's license",
            "driving licence", "date of birth", "nationality", "place of birth",
            "license no", "id number", "personal no", "sex", "issuing authority",
            "expiry date", "expiration date", "mrz", "bearer"
        )
    ),
    ClassificationRule(
        DocumentCategory.Contract, "contract", setOf(
            "agreement", "contract", "terms and conditions", "hereby agrees",
            "parties agree", "binding agreement", "obligation", "whereas",
            "in witness whereof", "hereinafter", "indemnification", "arbitration",
            "governing law", "clause", "schedule", "exhibit", "addendum"
        )
    ),
    ClassificationRule(
        DocumentCategory.Medical, "medical", setOf(
            "patient", "diagnosis", "prescription", "doctor", "physician",
            "hospital", "clinic", "medication", "dosage", "mg", "ml",
            "treatment", "referral", "lab result", "blood pressure", "test result",
            "medical record", "healthcare"
        )
    ),
    ClassificationRule(
        DocumentCategory.Invoice, "invoice", setOf(
            "invoice", "bill to", "billing address", "due date", "payment due",
            "invoice number", "inv #", "invoice #", "vat", "net amount",
            "line item", "unit price", "qty", "quantity", "subtotal", "balance due"
        )
    ),
    ClassificationRule(
        DocumentCategory.Receipt, "receipt", setOf(
            "receipt", "subtotal", "cashier", "store", "thank you for shopping",
            "change", "loyalty", "supermarket", "hypermarket", "carrefour", "walmart",
            "grocery", "amount due", "amount paid", "your total"
        )
    )
)

object DocumentClassifier : ClassifierService {

    override fun classify(ocrText: String): DocumentCategory {
        if (ocrText.isBlank()) return DocumentCategory.Other
        val lower = ocrText.lowercase()
        val best = classificationRules.maxByOrNull { rule ->
            rule.keywords.count { lower.contains(it) }
        } ?: return DocumentCategory.Other
        val score = best.keywords.count { lower.contains(it) }
        return if (score >= 2) best.category else DocumentCategory.Other
    }

    override fun extractTags(ocrText: String): List<String> {
        if (ocrText.isBlank()) return emptyList()
        val lower = ocrText.lowercase()
        return classificationRules
            .filter { rule -> rule.keywords.count { lower.contains(it) } >= 1 }
            .map { it.label }
            .distinct()
    }
}

object DocumentNamer : NamingService {

    override fun suggest(ocrText: String, category: DocumentCategory, dateMillis: Long): String? {
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

    private fun extractMeaningfulExcerpt(text: String): String? =
        text.lines()
            .map { it.trim() }
            .filter { it.length in 3..40 }
            .firstOrNull { line ->
                line.any { it.isLetter() } &&
                    !line.all { it.isDigit() || it.isWhitespace() || it == '/' || it == '-' }
            }?.take(30)?.trimEnd()

    private fun formatDate(millis: Long): String {
        val secs = millis / 1000
        val days = secs / 86400
        // Days from proleptic Gregorian epoch (March 1, 0000) to Unix epoch (Jan 1, 1970).
        // Correct Hinnant constant; using 719162 (off by 306) caused a wrong year.
        val epoch = 719468L
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

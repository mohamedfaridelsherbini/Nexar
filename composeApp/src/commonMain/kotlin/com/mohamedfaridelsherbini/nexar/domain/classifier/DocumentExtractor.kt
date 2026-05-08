package com.mohamedfaridelsherbini.nexar.domain.classifier

object DocumentExtractor : ExtractionService {

    private val amountPatterns = listOf(
        Regex("""[$£€]\s*[\d,]+\.?\d*"""),
        Regex("""[\d,]+\.\d{2}\s*[$£€]"""),
        Regex("""(?i)total[:\s]+[$£€]?\s*([\d,]+\.?\d*)"""),
        Regex("""(?i)amount[:\s]+[$£€]?\s*([\d,]+\.?\d*)"""),
        Regex("""(?i)subtotal[:\s]+[$£€]?\s*([\d,]+\.?\d*)"""),
        Regex("""[\d]{1,3}(?:,\d{3})*\.\d{2}""")
    )

    private val datePatterns = listOf(
        Regex("""\d{1,2}/\d{1,2}/\d{2,4}"""),
        Regex("""\d{4}-\d{2}-\d{2}"""),
        Regex("""\d{1,2}-\d{1,2}-\d{2,4}"""),
        Regex("""(?i)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*\.?\s+\d{1,2},?\s+\d{4}"""),
        Regex("""(?i)\d{1,2}\s+(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*\.?\s+\d{4}""")
    )

    override fun extractAmount(ocrText: String): String? {
        if (ocrText.isBlank()) return null
        val candidates = mutableListOf<Pair<String, Double>>()
        for (pattern in amountPatterns) {
            for (match in pattern.findAll(ocrText)) {
                val raw = match.value
                val numeric = raw.replace(Regex("""[^0-9.]"""), "")
                val value = numeric.toDoubleOrNull() ?: continue
                candidates.add(raw.trim() to value)
            }
        }
        return candidates.maxByOrNull { it.second }?.first
    }

    override fun extractDate(ocrText: String): String? {
        if (ocrText.isBlank()) return null
        for (pattern in datePatterns) {
            val match = pattern.find(ocrText)
            if (match != null) return match.value.trim()
        }
        return null
    }
}

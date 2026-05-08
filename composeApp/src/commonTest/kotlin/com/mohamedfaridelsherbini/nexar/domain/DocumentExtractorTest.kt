package com.mohamedfaridelsherbini.nexar.domain

import com.mohamedfaridelsherbini.nexar.domain.classifier.DocumentExtractor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DocumentExtractorTest {

    // ── extractAmount ─────────────────────────────────────────────────────────

    @Test
    fun `extractAmount returns dollar amount`() {
        assertNotNull(DocumentExtractor.extractAmount("Total: \$42.50"))
    }

    @Test
    fun `extractAmount returns euro amount`() {
        assertNotNull(DocumentExtractor.extractAmount("Amount due: €99.00"))
    }

    @Test
    fun `extractAmount returns the largest amount`() {
        val text = "Subtotal \$10.00\nTax \$2.50\nTotal \$42.50"
        val amount = DocumentExtractor.extractAmount(text)
        assertNotNull(amount)
        assertTrue(amount.contains("42"), "Expected largest amount (42.50), got: $amount")
    }

    @Test
    fun `extractAmount returns null for blank text`() {
        assertNull(DocumentExtractor.extractAmount(""))
    }

    @Test
    fun `extractAmount returns null when no amount present`() {
        assertNull(DocumentExtractor.extractAmount("No amounts here, just plain text"))
    }

    @Test
    fun `extractAmount recognises total keyword`() {
        val amount = DocumentExtractor.extractAmount("Total: 1234.56")
        assertNotNull(amount)
    }

    // ── extractDate ───────────────────────────────────────────────────────────

    @Test
    fun `extractDate returns ISO date`() {
        assertEquals("2024-03-15", DocumentExtractor.extractDate("Date: 2024-03-15"))
    }

    @Test
    fun `extractDate returns slash-separated date`() {
        assertNotNull(DocumentExtractor.extractDate("Issued 03/15/2024"))
    }

    @Test
    fun `extractDate returns dash-separated date`() {
        assertNotNull(DocumentExtractor.extractDate("Date 15-03-2024"))
    }

    @Test
    fun `extractDate returns month-name date`() {
        assertNotNull(DocumentExtractor.extractDate("Invoice date: March 15, 2024"))
    }

    @Test
    fun `extractDate returns abbreviated month date`() {
        assertNotNull(DocumentExtractor.extractDate("Due: Jan 5, 2025"))
    }

    @Test
    fun `extractDate returns null for blank text`() {
        assertNull(DocumentExtractor.extractDate(""))
    }

    @Test
    fun `extractDate returns null when no date present`() {
        assertNull(DocumentExtractor.extractDate("No dates here at all, just text"))
    }
}

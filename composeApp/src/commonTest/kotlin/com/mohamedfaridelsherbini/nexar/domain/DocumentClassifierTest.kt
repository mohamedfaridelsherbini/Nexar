package com.mohamedfaridelsherbini.nexar.domain

import com.mohamedfaridelsherbini.nexar.domain.classifier.DocumentClassifier
import com.mohamedfaridelsherbini.nexar.domain.model.DocumentCategory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DocumentClassifierTest {

    // ── classify ──────────────────────────────────────────────────────────────

    @Test
    fun `classify returns Receipt for receipt keywords`() {
        val text = "receipt subtotal cashier thank you for shopping amount paid"
        assertEquals(DocumentCategory.Receipt, DocumentClassifier.classify(text))
    }

    @Test
    fun `classify returns Invoice for invoice keywords`() {
        val text = "invoice bill to payment due invoice number vat subtotal balance due"
        assertEquals(DocumentCategory.Invoice, DocumentClassifier.classify(text))
    }

    @Test
    fun `classify returns IdDocument for ID keywords`() {
        val text = "passport nationality date of birth expiry date mrz bearer"
        assertEquals(DocumentCategory.IdDocument, DocumentClassifier.classify(text))
    }

    @Test
    fun `classify returns Contract for contract keywords`() {
        val text = "agreement terms and conditions hereby agrees binding agreement whereas"
        assertEquals(DocumentCategory.Contract, DocumentClassifier.classify(text))
    }

    @Test
    fun `classify returns Medical for medical keywords`() {
        val text = "patient diagnosis prescription doctor medication dosage"
        assertEquals(DocumentCategory.Medical, DocumentClassifier.classify(text))
    }

    @Test
    fun `classify returns Other for blank text`() {
        assertEquals(DocumentCategory.Other, DocumentClassifier.classify(""))
    }

    @Test
    fun `classify returns Other when score below threshold`() {
        // Only one keyword match — threshold requires at least 2
        val text = "some random text with only one invoice keyword"
        assertEquals(DocumentCategory.Other, DocumentClassifier.classify(text))
    }

    @Test
    fun `classify is case-insensitive`() {
        val text = "INVOICE BILL TO PAYMENT DUE INVOICE NUMBER VAT SUBTOTAL"
        assertEquals(DocumentCategory.Invoice, DocumentClassifier.classify(text))
    }

    @Test
    fun `classify picks highest-scoring category`() {
        // Only receipt keywords score well here
        val text = "receipt subtotal cashier store thank you for shopping amount paid your total"
        assertEquals(DocumentCategory.Receipt, DocumentClassifier.classify(text))
    }

    // ── extractTags ───────────────────────────────────────────────────────────

    @Test
    fun `extractTags returns matching category labels`() {
        val text = "invoice bill to payment due invoice number"
        val tags = DocumentClassifier.extractTags(text)
        assertTrue(tags.contains("invoice"), "Expected 'invoice' tag in $tags")
    }

    @Test
    fun `extractTags returns multiple labels for mixed text`() {
        val text = "invoice bill to payment due doctor patient prescription"
        val tags = DocumentClassifier.extractTags(text)
        assertTrue(tags.contains("invoice"), "Expected 'invoice' in $tags")
        assertTrue(tags.contains("medical"), "Expected 'medical' in $tags")
    }

    @Test
    fun `extractTags returns empty list for blank text`() {
        assertTrue(DocumentClassifier.extractTags("").isEmpty())
    }

    @Test
    fun `extractTags returns distinct labels`() {
        val text = "invoice invoice invoice bill to payment due invoice number"
        val tags = DocumentClassifier.extractTags(text)
        assertEquals(tags.distinct(), tags)
    }
}

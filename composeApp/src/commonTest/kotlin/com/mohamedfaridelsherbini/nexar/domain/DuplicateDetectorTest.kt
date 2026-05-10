package com.mohamedfaridelsherbini.nexar.domain

import com.mohamedfaridelsherbini.nexar.domain.classifier.DuplicateDetector
import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DuplicateDetectorTest {
    private fun doc(
        id: String,
        ocrText: String,
    ) = ScannedDocument(
        id = id,
        name = "Test",
        dateMillis = 0L,
        imageUris = emptyList(),
        ocrText = ocrText,
    )

    private val richText =
        "invoice number 12345 bill to john doe payment due march 2024 " +
            "vat net amount line item unit price qty quantity subtotal balance due"

    @Test
    fun `returns null when no existing documents`() {
        assertNull(DuplicateDetector.findDuplicate(doc("1", richText), emptyList()))
    }

    @Test
    fun `returns null when documents are clearly different`() {
        val newDoc = doc("1", "invoice payment due vat balance")
        val existing = listOf(doc("2", "patient diagnosis prescription doctor medication"))
        assertNull(DuplicateDetector.findDuplicate(newDoc, existing))
    }

    @Test
    fun `returns id for identical OCR text`() {
        val newDoc = doc("1", richText)
        val existing = listOf(doc("2", richText))
        assertEquals("2", DuplicateDetector.findDuplicate(newDoc, existing))
    }

    @Test
    fun `returns id for highly similar text`() {
        val newDoc = doc("1", richText)
        val slightlyDifferent = doc("2", "$richText with just one extra phrase")
        assertEquals("2", DuplicateDetector.findDuplicate(newDoc, listOf(slightlyDifferent)))
    }

    @Test
    fun `skips the document itself`() {
        val newDoc = doc("1", richText)
        assertNull(DuplicateDetector.findDuplicate(newDoc, listOf(newDoc)))
    }

    @Test
    fun `returns null for blank OCR text`() {
        val newDoc = doc("1", "")
        val existing = listOf(doc("2", richText))
        assertNull(DuplicateDetector.findDuplicate(newDoc, existing))
    }

    @Test
    fun `skips existing documents with blank OCR`() {
        val newDoc = doc("1", richText)
        val blankOcr = doc("2", "")
        assertNull(DuplicateDetector.findDuplicate(newDoc, listOf(blankOcr)))
    }

    @Test
    fun `returns the best match among multiple candidates`() {
        val newDoc = doc("1", richText)
        val closerMatch = doc("2", "$richText extra extension text")
        val weaker = doc("3", "invoice payment due")
        val result = DuplicateDetector.findDuplicate(newDoc, listOf(weaker, closerMatch))
        assertEquals("2", result)
    }
}

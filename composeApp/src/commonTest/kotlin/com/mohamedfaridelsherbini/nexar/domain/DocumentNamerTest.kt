package com.mohamedfaridelsherbini.nexar.domain

import com.mohamedfaridelsherbini.nexar.domain.classifier.DocumentNamer
import com.mohamedfaridelsherbini.nexar.domain.model.DocumentCategory
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DocumentNamerTest {

    // Fixed epoch for 2024-01-01 (1704067200000 ms)
    private val jan2024 = 1_704_067_200_000L

    @Test
    fun `suggest returns null for blank text`() {
        assertNull(DocumentNamer.suggest("", DocumentCategory.Receipt, jan2024))
    }

    @Test
    fun `suggest returns null for Other category`() {
        assertNull(DocumentNamer.suggest("some text", DocumentCategory.Other, jan2024))
    }

    @Test
    fun `suggest returns name prefixed with Receipt`() {
        val name = DocumentNamer.suggest("receipt subtotal cashier", DocumentCategory.Receipt, jan2024)
        assertNotNull(name)
        assertTrue(name.startsWith("Receipt"), "Expected 'Receipt' prefix, got: $name")
    }

    @Test
    fun `suggest returns name prefixed with Invoice`() {
        val name = DocumentNamer.suggest("invoice bill to payment due", DocumentCategory.Invoice, jan2024)
        assertNotNull(name)
        assertTrue(name.startsWith("Invoice"), "Expected 'Invoice' prefix, got: $name")
    }

    @Test
    fun `suggest returns name prefixed with ID`() {
        val name = DocumentNamer.suggest("passport nationality mrz bearer", DocumentCategory.IdDocument, jan2024)
        assertNotNull(name)
        assertTrue(name.startsWith("ID"), "Expected 'ID' prefix, got: $name")
    }

    @Test
    fun `suggest returns name prefixed with Contract`() {
        val name = DocumentNamer.suggest("agreement terms and conditions", DocumentCategory.Contract, jan2024)
        assertNotNull(name)
        assertTrue(name.startsWith("Contract"), "Expected 'Contract' prefix, got: $name")
    }

    @Test
    fun `suggest returns name prefixed with Medical`() {
        val name = DocumentNamer.suggest("patient diagnosis prescription", DocumentCategory.Medical, jan2024)
        assertNotNull(name)
        assertTrue(name.startsWith("Medical"), "Expected 'Medical' prefix, got: $name")
    }

    @Test
    fun `suggest includes date in name`() {
        val name = DocumentNamer.suggest("receipt cashier store", DocumentCategory.Receipt, jan2024)
        assertNotNull(name)
        assertTrue(name.contains("2024"), "Expected year 2024 in name, got: $name")
    }

    @Test
    fun `suggest falls back gracefully when text has no meaningful excerpt`() {
        // All digits / punctuation — no meaningful excerpt
        val name = DocumentNamer.suggest("123 456 789 --- ///", DocumentCategory.Receipt, jan2024)
        assertNotNull(name)
        assertTrue(name.startsWith("Receipt"), "Expected fallback 'Receipt - date', got: $name")
    }
}

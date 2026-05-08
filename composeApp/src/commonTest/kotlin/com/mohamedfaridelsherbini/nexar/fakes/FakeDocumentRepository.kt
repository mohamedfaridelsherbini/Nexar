package com.mohamedfaridelsherbini.nexar.fakes

import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument
import com.mohamedfaridelsherbini.nexar.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeDocumentRepository : DocumentRepository {

    private val _documents = MutableStateFlow<List<ScannedDocument>>(emptyList())

    /** The most recent argument passed to [updateDocument]. */
    var lastUpdated: ScannedDocument? = null
        private set

    /** All documents ever passed to [saveDocument]. */
    val saved = mutableListOf<ScannedDocument>()

    fun setDocuments(docs: List<ScannedDocument>) {
        _documents.value = docs
    }

    override fun observeDocuments(): Flow<List<ScannedDocument>> = _documents

    override suspend fun saveDocument(document: ScannedDocument) {
        saved.add(document)
        _documents.update { it + document }
    }

    override suspend fun updateDocument(document: ScannedDocument) {
        lastUpdated = document
        _documents.update { docs -> docs.map { if (it.id == document.id) document else it } }
    }

    override suspend fun renameDocument(id: String, newName: String) {
        _documents.update { docs ->
            docs.map { if (it.id == id) it.copy(name = newName) else it }
        }
    }

    override suspend fun deleteDocument(document: ScannedDocument) {
        _documents.update { it.filter { doc -> doc.id != document.id } }
    }

    override suspend fun markExported(id: String) {
        _documents.update { docs ->
            docs.map { if (it.id == id) it.copy(isExportedToStorage = true) else it }
        }
    }

    /**
     * In-memory FTS simulation: strips the trailing `*` from each token and does a
     * case-insensitive contains check across name, ocrText, category, and tags.
     */
    override fun searchDocuments(ftsQuery: String): Flow<List<ScannedDocument>> =
        _documents.map { docs ->
            val terms = ftsQuery.replace("*", "").trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
            if (terms.isEmpty()) docs
            else docs.filter { doc ->
                terms.any { term ->
                    doc.name.contains(term, ignoreCase = true) ||
                        doc.ocrText.contains(term, ignoreCase = true) ||
                        doc.category.displayName.contains(term, ignoreCase = true) ||
                        doc.tags.any { it.contains(term, ignoreCase = true) }
                }
            }
        }
}

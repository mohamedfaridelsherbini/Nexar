package com.mohamedfaridelsherbini.nexar.domain.repository

import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    fun observeDocuments(): Flow<List<ScannedDocument>>

    suspend fun saveDocument(document: ScannedDocument)

    suspend fun updateDocument(document: ScannedDocument)

    suspend fun renameDocument(
        id: String,
        newName: String,
    )

    suspend fun deleteDocument(document: ScannedDocument)

    suspend fun markExported(id: String)

    /**
     * Full-text search backed by the FTS4 index. [ftsQuery] must already be in
     * Room MATCH syntax — see [SearchDocumentsUseCase.sanitizeFtsQuery].
     */
    fun searchDocuments(ftsQuery: String): Flow<List<ScannedDocument>>
}

package com.mohamedfaridelsherbini.nexar.domain.repository

import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    fun observeDocuments(): Flow<List<ScannedDocument>>
    suspend fun saveDocument(document: ScannedDocument)
    suspend fun renameDocument(id: String, newName: String)
    suspend fun deleteDocument(document: ScannedDocument)
}

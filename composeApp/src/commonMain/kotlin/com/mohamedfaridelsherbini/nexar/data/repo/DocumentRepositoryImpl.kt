package com.mohamedfaridelsherbini.nexar.data.repo

import com.mohamedfaridelsherbini.nexar.data.db.DocumentDao
import com.mohamedfaridelsherbini.nexar.data.db.DocumentEntity
import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument
import com.mohamedfaridelsherbini.nexar.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.map

class DocumentRepositoryImpl(private val dao: DocumentDao) : DocumentRepository {
    override fun observeDocuments() =
        dao.getAllDocuments().map { entities -> entities.map { it.toDomain() } }

    override suspend fun saveDocument(document: ScannedDocument) {
        dao.insertDocument(DocumentEntity.fromDomain(document))
    }

    override suspend fun renameDocument(id: String, newName: String) {
        dao.renameDocument(id, newName)
    }

    override suspend fun deleteDocument(document: ScannedDocument) {
        dao.deleteDocument(DocumentEntity.fromDomain(document))
    }
}

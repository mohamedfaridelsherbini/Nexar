package com.mohamedfaridelsherbini.nexar.data.repo

import com.mohamedfaridelsherbini.nexar.data.db.DocumentDao
import com.mohamedfaridelsherbini.nexar.data.db.DocumentEntity
import com.mohamedfaridelsherbini.nexar.data.db.DocumentFtsDao
import com.mohamedfaridelsherbini.nexar.data.db.DocumentFtsEntity
import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument
import com.mohamedfaridelsherbini.nexar.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DocumentRepositoryImpl(
    private val dao: DocumentDao,
    private val ftsDao: DocumentFtsDao,
) : DocumentRepository {
    override fun observeDocuments() = dao.getAllDocuments().map { entities -> entities.map { it.toDomain() } }

    override suspend fun saveDocument(document: ScannedDocument) {
        dao.insertDocument(DocumentEntity.fromDomain(document))
        syncFts(document)
    }

    override suspend fun updateDocument(document: ScannedDocument) {
        dao.updateDocument(DocumentEntity.fromDomain(document))
        syncFts(document)
    }

    override suspend fun renameDocument(
        id: String,
        newName: String,
    ) {
        dao.renameDocument(id, newName)
        // Re-sync the FTS row using the freshly updated entity from the DB
        dao.getDocumentById(id)?.toDomain()?.let { syncFts(it) }
    }

    override suspend fun deleteDocument(document: ScannedDocument) {
        dao.deleteDocument(DocumentEntity.fromDomain(document))
        ftsDao.deleteById(document.id)
    }

    override suspend fun markExported(id: String) {
        dao.markExported(id)
        // isExported is not searchable; no FTS update required
    }

    override fun searchDocuments(ftsQuery: String): Flow<List<ScannedDocument>> =
        dao.searchDocuments(ftsQuery).map { entities -> entities.map { it.toDomain() } }

    // ── FTS helpers ──────────────────────────────────────────────────────────

    private suspend fun syncFts(document: ScannedDocument) {
        ftsDao.deleteById(document.id)
        ftsDao.insert(DocumentFtsEntity.fromDomain(document))
    }
}

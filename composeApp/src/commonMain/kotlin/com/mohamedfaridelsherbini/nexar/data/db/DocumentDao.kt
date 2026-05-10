package com.mohamedfaridelsherbini.nexar.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY dateMillis DESC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity)

    @Update
    suspend fun updateDocument(document: DocumentEntity)

    @Delete
    suspend fun deleteDocument(document: DocumentEntity)

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getDocumentById(id: String): DocumentEntity?

    @Query("UPDATE documents SET name = :newName WHERE id = :id")
    suspend fun renameDocument(
        id: String,
        newName: String,
    )

    @Query(
        "UPDATE documents SET ocrText = :ocrText, category = :category, tagsJson = :tagsJson, ocrProcessed = 1 WHERE id = :id",
    )
    suspend fun updateOcr(
        id: String,
        ocrText: String,
        category: String,
        tagsJson: String,
    )

    @Query("UPDATE documents SET isExportedToStorage = 1 WHERE id = :id")
    suspend fun markExported(id: String)

    /**
     * FTS4 JOIN search — Room tracks both tables so this Flow re-emits on any write.
     * The [query] must already be in FTS MATCH syntax (e.g. `"invoice* jan*"`).
     */
    @Query(
        """
        SELECT d.* FROM documents d
        JOIN documents_fts ON d.id = documents_fts.documentId
        WHERE documents_fts MATCH :query
        ORDER BY d.dateMillis DESC
    """,
    )
    fun searchDocuments(query: String): Flow<List<DocumentEntity>>
}

@Dao
interface DocumentFtsDao {
    @Insert
    suspend fun insert(entity: DocumentFtsEntity)

    @Query("DELETE FROM documents_fts WHERE documentId = :id")
    suspend fun deleteById(id: String)
}

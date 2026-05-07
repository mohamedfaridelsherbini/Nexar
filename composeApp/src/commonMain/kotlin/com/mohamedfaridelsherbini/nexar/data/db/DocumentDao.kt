package com.mohamedfaridelsherbini.nexar.data.db

import androidx.room.*
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

    @Query("UPDATE documents SET name = :newName WHERE id = :id")
    suspend fun renameDocument(id: String, newName: String)

    @Query("UPDATE documents SET ocrText = :ocrText, category = :category, tagsJson = :tagsJson, ocrProcessed = 1 WHERE id = :id")
    suspend fun updateOcr(id: String, ocrText: String, category: String, tagsJson: String)

    @Query("UPDATE documents SET isExportedToStorage = 1 WHERE id = :id")
    suspend fun markExported(id: String)
}

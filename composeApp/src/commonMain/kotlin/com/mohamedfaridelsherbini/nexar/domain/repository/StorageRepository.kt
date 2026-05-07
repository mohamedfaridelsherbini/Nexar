package com.mohamedfaridelsherbini.nexar.domain.repository

import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument
import kotlinx.coroutines.flow.Flow

interface StorageRepository {
    fun observeStorageLocation(): Flow<String?>
    fun setStorageLocation(uri: String)
    suspend fun saveDocument(document: ScannedDocument): Boolean
    suspend fun createFolder(folderName: String): Boolean
}

package com.mohamedfaridelsherbini.nexar.domain.repository

import kotlinx.coroutines.flow.Flow

interface StorageRepository {
    fun observeStorageLocation(): Flow<String?>
    fun setStorageLocation(uri: String)
    suspend fun saveDocument(fileName: String, sourceUri: String): Boolean
    suspend fun createFolder(folderName: String): Boolean
}

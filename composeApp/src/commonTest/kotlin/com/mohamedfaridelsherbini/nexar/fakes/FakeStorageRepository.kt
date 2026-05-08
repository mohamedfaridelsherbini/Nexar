package com.mohamedfaridelsherbini.nexar.fakes

import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument
import com.mohamedfaridelsherbini.nexar.domain.repository.StorageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeStorageRepository(
    private val saveResult: Boolean = true,
    private val createFolderResult: Boolean = true
) : StorageRepository {

    private val _location = MutableStateFlow<String?>(null)

    val savedDocuments = mutableListOf<ScannedDocument>()
    var currentLocation: String? get() = _location.value
        set(value) { _location.value = value }

    override fun observeStorageLocation(): Flow<String?> = _location

    override fun setStorageLocation(uri: String) {
        _location.update { uri }
    }

    override suspend fun saveDocument(document: ScannedDocument): Boolean {
        if (saveResult) savedDocuments.add(document)
        return saveResult
    }

    override suspend fun createFolder(folderName: String): Boolean = createFolderResult
}

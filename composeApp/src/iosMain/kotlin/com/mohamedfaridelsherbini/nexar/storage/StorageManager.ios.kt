package com.mohamedfaridelsherbini.nexar.storage

import com.mohamedfaridelsherbini.nexar.domain.repository.StorageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL

@OptIn(ExperimentalForeignApi::class)
class IOSStorageRepository : StorageRepository {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val _storageLocation = MutableStateFlow(userDefaults.stringForKey("storage_uri"))

    override fun observeStorageLocation(): Flow<String?> = _storageLocation.asStateFlow()

    override fun setStorageLocation(uri: String) {
        userDefaults.setObject(uri, "storage_uri")
        _storageLocation.value = uri
    }

    override suspend fun saveDocument(fileName: String, sourceUri: String): Boolean {
        val rootUriString = _storageLocation.value ?: return false
        val rootUrl = NSURL.URLWithString(rootUriString) ?: return false
        val sourceUrl = NSURL.URLWithString(sourceUri) ?: return false
        
        val destUrl = rootUrl.URLByAppendingPathComponent(fileName) ?: return false
        
        if (rootUrl.startAccessingSecurityScopedResource()) {
            return try {
                val success = NSFileManager.defaultManager.copyItemAtURL(
                    srcURL = sourceUrl,
                    toURL = destUrl,
                    error = null
                )
                success
            } finally {
                rootUrl.stopAccessingSecurityScopedResource()
            }
        }
        return false
    }

    override suspend fun createFolder(folderName: String): Boolean {
        val rootUriString = _storageLocation.value ?: return false
        val rootUrl = NSURL.URLWithString(rootUriString) ?: return false
        val folderUrl = rootUrl.URLByAppendingPathComponent(folderName) ?: return false
        
        if (rootUrl.startAccessingSecurityScopedResource()) {
            return try {
                NSFileManager.defaultManager.createDirectoryAtURL(
                    url = folderUrl,
                    withIntermediateDirectories = true,
                    attributes = null,
                    error = null
                )
            } finally {
                rootUrl.stopAccessingSecurityScopedResource()
            }
        }
        return false
    }
}

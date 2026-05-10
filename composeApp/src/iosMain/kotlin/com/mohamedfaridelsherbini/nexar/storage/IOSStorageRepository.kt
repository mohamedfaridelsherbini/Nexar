package com.mohamedfaridelsherbini.nexar.storage

import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument
import com.mohamedfaridelsherbini.nexar.domain.repository.StorageRepository
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDefaults
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@OptIn(ExperimentalForeignApi::class)
class IOSStorageRepository : StorageRepository {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val _storageLocation = MutableStateFlow(userDefaults.stringForKey("storage_uri"))

    override fun observeStorageLocation(): Flow<String?> = _storageLocation.asStateFlow()

    override fun setStorageLocation(uri: String) {
        userDefaults.setObject(uri, "storage_uri")
        _storageLocation.value = uri
    }

    override suspend fun saveDocument(document: ScannedDocument): Boolean {
        val sourceUri = document.pdfUri ?: return false
        val rootUriString = _storageLocation.value ?: return false
        val rootUrl = NSURL.URLWithString(rootUriString) ?: return false
        val sourceUrl = NSURL.URLWithString(sourceUri) ?: return false

        // Category/year subfolder structure
        val categoryFolder = rootUrl.URLByAppendingPathComponent(document.category.folderName) ?: return false
        val year = formatYear(document.dateMillis)
        val yearFolder = categoryFolder.URLByAppendingPathComponent(year) ?: categoryFolder

        if (rootUrl.startAccessingSecurityScopedResource()) {
            return try {
                NSFileManager.defaultManager.createDirectoryAtURL(
                    url = yearFolder,
                    withIntermediateDirectories = true,
                    attributes = null,
                    error = null,
                )
                val fileName = document.exportFileName()
                val destUrl = yearFolder.URLByAppendingPathComponent(fileName) ?: return false
                NSFileManager.defaultManager.copyItemAtURL(
                    srcURL = sourceUrl,
                    toURL = destUrl,
                    error = null,
                )
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
                    error = null,
                )
            } finally {
                rootUrl.stopAccessingSecurityScopedResource()
            }
        }
        return false
    }

    private fun formatYear(millis: Long): String {
        // Days since Unix epoch → Gregorian year via civil-calendar algorithm
        val days = millis / 86_400_000L
        val z = days + 719_468L
        val era = (if (z >= 0) z else z - 146_096L) / 146_097L
        val doe = z - era * 146_097L
        val yoe = (doe - doe / 1460L + doe / 36_524L - doe / 146_096L) / 365L
        val y = yoe + era * 400L
        val doy = doe - (365L * yoe + yoe / 4L - yoe / 100L)
        val mp = (5L * doy + 2L) / 153L
        val m = mp + (if (mp < 10L) 3L else -9L)
        return (y + (if (m <= 2L) 1L else 0L)).toString()
    }
}

private fun ScannedDocument.exportFileName(): String {
    val sanitizedName = name.trim().ifBlank { "Document" }
    return if (sanitizedName.endsWith(".pdf", ignoreCase = true)) sanitizedName else "$sanitizedName.pdf"
}

package com.mohamedfaridelsherbini.nexar.storage

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument
import com.mohamedfaridelsherbini.nexar.domain.repository.StorageRepository
import com.mohamedfaridelsherbini.nexar.widget.NexarWidgetProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AndroidStorageRepository(private val context: Context) : StorageRepository {
    private val prefs = context.getSharedPreferences("nexar_storage", Context.MODE_PRIVATE)
    private val _storageLocation = MutableStateFlow(prefs.getString("storage_uri", null))

    override fun observeStorageLocation(): Flow<String?> = _storageLocation.asStateFlow()

    override fun setStorageLocation(uri: String) {
        prefs.edit().putString("storage_uri", uri).apply()
        _storageLocation.value = uri
    }

    override suspend fun saveDocument(document: ScannedDocument): Boolean {
        val sourceUri = document.pdfUri ?: return false
        val rootUriString = _storageLocation.value ?: return false
        val rootUri = Uri.parse(rootUriString)
        val rootDoc = DocumentFile.fromTreeUri(context, rootUri) ?: return false
        if (!rootDoc.canWrite()) {
            _storageLocation.value = null
            prefs.edit().remove("storage_uri").apply()
            return false
        }

        val categoryFolder = rootDoc.findOrCreate(document.category.folderName, isDir = true) ?: return false
        val year = SimpleDateFormat("yyyy", Locale.US).format(Date(document.dateMillis))
        val yearFolder = categoryFolder.findOrCreate(year, isDir = true) ?: categoryFolder

        val fileName = document.exportFileName()
        val file = yearFolder.createFile("application/pdf", fileName) ?: return false

        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(Uri.parse(sourceUri))
            val outputStream: OutputStream? = context.contentResolver.openOutputStream(file.uri)
            if (inputStream != null && outputStream != null) {
                inputStream.use { input -> outputStream.use { output -> input.copyTo(output) } }
                NexarWidgetProvider.notifyUpdate(context)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun createFolder(folderName: String): Boolean {
        val rootUriString = _storageLocation.value ?: return false
        val rootUri = Uri.parse(rootUriString)
        val rootDoc = DocumentFile.fromTreeUri(context, rootUri) ?: return false
        if (!rootDoc.canWrite()) {
            _storageLocation.value = null
            prefs.edit().remove("storage_uri").apply()
            return false
        }
        return rootDoc.createDirectory(folderName) != null
    }

    private fun DocumentFile.findOrCreate(name: String, isDir: Boolean): DocumentFile? {
        return findFile(name) ?: if (isDir) createDirectory(name) else createFile("application/pdf", name)
    }
}

private fun ScannedDocument.exportFileName(): String {
    val sanitizedName = name.trim().ifBlank { "Document" }
    return if (sanitizedName.endsWith(".pdf", ignoreCase = true)) sanitizedName else "$sanitizedName.pdf"
}

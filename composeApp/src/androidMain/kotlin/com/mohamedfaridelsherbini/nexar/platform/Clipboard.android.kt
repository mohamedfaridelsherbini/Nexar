package com.mohamedfaridelsherbini.nexar.platform

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.toClipEntry

actual fun createClipEntry(text: String): ClipEntry = ClipData.newPlainText(null, text).toClipEntry()

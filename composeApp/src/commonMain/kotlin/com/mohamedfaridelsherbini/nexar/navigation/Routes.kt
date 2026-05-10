package com.mohamedfaridelsherbini.nexar.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey

@Serializable
data object Dashboard : Route

@Serializable
data class Scanner(val folderId: String? = null) : Route

@Serializable
data class DocumentDetail(val documentId: String) : Route

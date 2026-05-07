package com.mohamedfaridelsherbini.nexar.navigation

import kotlinx.serialization.Serializable
import androidx.navigation3.runtime.NavKey

@Serializable
sealed interface Route : NavKey

@Serializable
data object Dashboard : Route

@Serializable
data class Scanner(val folderId: String? = null) : Route

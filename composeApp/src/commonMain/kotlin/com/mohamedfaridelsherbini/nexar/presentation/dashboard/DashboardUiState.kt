package com.mohamedfaridelsherbini.nexar.presentation.dashboard

import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument

data class DashboardUiState(
    val documents: List<ScannedDocument> = emptyList(),
    val storageLocation: String? = null
)

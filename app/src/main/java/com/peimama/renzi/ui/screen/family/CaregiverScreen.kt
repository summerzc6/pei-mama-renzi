package com.peimama.renzi.ui.screen.family

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.peimama.renzi.ui.viewmodel.FamilyUiState
import kotlinx.coroutines.flow.StateFlow

@Composable
fun CaregiverScreen(
    uiState: StateFlow<FamilyUiState>,
    contentPadding: PaddingValues,
) {
    FamilyCompanionScreen(
        uiState = uiState,
        contentPadding = contentPadding,
    )
}

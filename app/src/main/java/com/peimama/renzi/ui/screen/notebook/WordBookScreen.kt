package com.peimama.renzi.ui.screen.notebook

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.peimama.renzi.ui.viewmodel.NotebookUiState
import kotlinx.coroutines.flow.StateFlow

@Composable
fun WordBookScreen(
    uiState: StateFlow<NotebookUiState>,
    contentPadding: PaddingValues,
    onToggleFavorite: (String, Boolean) -> Unit,
    onOpenLesson: (String) -> Unit,
) {
    NotebookScreen(
        uiState = uiState,
        contentPadding = contentPadding,
        onToggleFavorite = onToggleFavorite,
        onOpenLesson = onOpenLesson,
    )
}

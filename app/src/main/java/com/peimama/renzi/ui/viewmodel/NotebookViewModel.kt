package com.peimama.renzi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.peimama.renzi.data.model.NotebookBuckets
import com.peimama.renzi.data.repository.LearningRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotebookUiState(
    val buckets: NotebookBuckets? = null,
    val loading: Boolean = true,
)

class NotebookViewModel(
    private val repository: LearningRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotebookUiState())
    val uiState: StateFlow<NotebookUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeNotebookBuckets().collect { buckets ->
                _uiState.value = NotebookUiState(
                    buckets = buckets,
                    loading = false,
                )
            }
        }
    }

    fun toggleFavorite(wordId: String, favorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(wordId, favorite)
        }
    }

    companion object {
        fun factory(repository: LearningRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { NotebookViewModel(repository) }
        }
    }
}

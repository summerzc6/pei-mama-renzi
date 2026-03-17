package com.peimama.renzi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.peimama.renzi.data.model.ReviewBuckets
import com.peimama.renzi.data.repository.LearningRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReviewUiState(
    val buckets: ReviewBuckets? = null,
    val loading: Boolean = true,
)

class ReviewViewModel(
    private val repository: LearningRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeReviewBuckets().collect { buckets ->
                _uiState.value = ReviewUiState(
                    buckets = buckets,
                    loading = false,
                )
            }
        }
    }

    companion object {
        fun factory(repository: LearningRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { ReviewViewModel(repository) }
        }
    }
}

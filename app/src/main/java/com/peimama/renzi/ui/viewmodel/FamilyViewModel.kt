package com.peimama.renzi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.peimama.renzi.data.model.FamilyDashboard
import com.peimama.renzi.data.repository.LearningRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FamilyUiState(
    val dashboard: FamilyDashboard? = null,
    val loading: Boolean = true,
)

class FamilyViewModel(
    private val repository: LearningRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FamilyUiState())
    val uiState: StateFlow<FamilyUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeFamilyDashboard().collect { dashboard ->
                _uiState.value = FamilyUiState(
                    dashboard = dashboard,
                    loading = false,
                )
            }
        }
    }

    companion object {
        fun factory(repository: LearningRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { FamilyViewModel(repository) }
        }
    }
}

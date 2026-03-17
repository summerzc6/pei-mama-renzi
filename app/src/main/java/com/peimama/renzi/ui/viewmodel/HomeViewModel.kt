package com.peimama.renzi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.peimama.renzi.data.local.entity.SceneEntity
import com.peimama.renzi.data.model.HomeDashboard
import com.peimama.renzi.data.model.LessonHighlight
import com.peimama.renzi.data.repository.LearningRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val dashboard: HomeDashboard? = null,
    val scenes: List<SceneEntity> = emptyList(),
    val continueLesson: LessonHighlight? = null,
    val recommendedLessons: List<LessonHighlight> = emptyList(),
    val loading: Boolean = true,
)

class HomeViewModel(
    private val repository: LearningRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureSeedData()
            repository.observeHomeDashboard().collect { dashboard ->
                _uiState.value = HomeUiState(
                    dashboard = dashboard,
                    scenes = dashboard.quickScenes,
                    continueLesson = dashboard.continueLesson,
                    recommendedLessons = dashboard.recommendedLessons,
                    loading = false,
                )
            }
        }
    }

    companion object {
        fun factory(repository: LearningRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { HomeViewModel(repository) }
        }
    }
}

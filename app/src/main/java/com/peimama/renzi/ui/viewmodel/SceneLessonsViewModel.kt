package com.peimama.renzi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.peimama.renzi.data.local.entity.SceneEntity
import com.peimama.renzi.data.model.LessonWithStatus
import com.peimama.renzi.data.repository.LearningRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class SceneLessonsUiState(
    val scene: SceneEntity? = null,
    val lessons: List<LessonWithStatus> = emptyList(),
    val loading: Boolean = true,
)

class SceneLessonsViewModel(
    private val sceneId: String,
    private val repository: LearningRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SceneLessonsUiState())
    val uiState: StateFlow<SceneLessonsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureSeedData()
            combine(
                repository.observeScene(sceneId),
                repository.observeLessonsWithStatus(sceneId),
            ) { scene, lessons ->
                SceneLessonsUiState(
                    scene = scene,
                    lessons = lessons,
                    loading = false,
                )
            }.collect { _uiState.value = it }
        }
    }

    fun startLesson(lessonId: String) {
        viewModelScope.launch {
            repository.markLessonInProgress(lessonId)
        }
    }

    companion object {
        fun factory(
            sceneId: String,
            repository: LearningRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { SceneLessonsViewModel(sceneId = sceneId, repository = repository) }
        }
    }
}

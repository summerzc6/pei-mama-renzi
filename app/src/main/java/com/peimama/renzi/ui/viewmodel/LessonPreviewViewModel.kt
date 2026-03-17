package com.peimama.renzi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.peimama.renzi.audio.AudioManager
import com.peimama.renzi.data.local.entity.WordItemEntity
import com.peimama.renzi.data.repository.LearningRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class LessonPreviewUiState(
    val lessonTitle: String = "",
    val words: List<WordItemEntity> = emptyList(),
    val currentWordIndex: Int = 0,
)

class LessonPreviewViewModel(
    private val lessonId: String,
    private val repository: LearningRepository,
    private val audioManager: AudioManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LessonPreviewUiState())
    val uiState: StateFlow<LessonPreviewUiState> = _uiState.asStateFlow()

    private val _messageFlow = MutableSharedFlow<String>()
    val messageFlow = _messageFlow.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.ensureSeedData()
            combine(
                repository.observeLesson(lessonId),
                repository.observeWordsByLesson(lessonId),
            ) { lesson, words ->
                _uiState.value.copy(
                    lessonTitle = lesson?.title ?: "课程字卡",
                    words = words,
                    currentWordIndex = _uiState.value.currentWordIndex.coerceIn(0, (words.lastIndex).coerceAtLeast(0)),
                )
            }.collect { _uiState.value = it }
        }
    }

    fun nextWord() {
        val state = _uiState.value
        if (state.words.isEmpty()) return
        _uiState.value = state.copy(
            currentWordIndex = (state.currentWordIndex + 1).coerceAtMost(state.words.lastIndex),
        )
    }

    fun previousWord() {
        val state = _uiState.value
        if (state.words.isEmpty()) return
        _uiState.value = state.copy(
            currentWordIndex = (state.currentWordIndex - 1).coerceAtLeast(0),
        )
    }

    fun playCurrentWord() {
        val word = _uiState.value.words.getOrNull(_uiState.value.currentWordIndex) ?: return
        viewModelScope.launch {
            _messageFlow.emit(audioManager.playWord(word.text, word.audioResName))
        }
    }

    fun startLesson() {
        viewModelScope.launch {
            repository.markLessonInProgress(lessonId)
        }
    }

    companion object {
        fun factory(
            lessonId: String,
            repository: LearningRepository,
            audioManager: AudioManager,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                LessonPreviewViewModel(
                    lessonId = lessonId,
                    repository = repository,
                    audioManager = audioManager,
                )
            }
        }
    }
}

